package ch.heigvd.dai.network;

import ch.heigvd.dai.Display;
import ch.heigvd.dai.GameBoard;
import ch.heigvd.dai.util.UserIO;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.TimeUnit;

public class TcpServerFixedThreadPool {

  private static final int PORT = 6433;
  private static final int SERVER_ID = (int) (Math.random() * 1000000);
  private static final int NUMBER_OF_THREADS = 2;

  private static final int ERROR_INV_COMMAND  = -1;
  private static final int ERROR_NOT_A_NUMBER = 1;
  private static final int ERROR_OUT_OF_INDEX = 2;
  private static final int ERROR_COLUMN_FULL  = 3;

  /**
   * Creates a new server instance.
   *
   * @param height     The height of the game board.
   * @param width      The width of the game board.
   * @param winLength  The number of consecutive pieces required to win.
   *
   * <p>This constructor initializes the server, sets up the game board, and manages
   * client connections and game sessions in a main loop. When a game ends, the server
   * resets to allow a new session to start.</p>
   */
  public TcpServerFixedThreadPool(int height, int width, int winLength) {

    // Track the number of connected players
    int numberOfPlayerConnected = 0;

    // Array to store the connected player handlers
    ClientHandler[] playerConnected = new ClientHandler[NUMBER_OF_THREADS];

    // Create a new game board with specified dimensions and win length
    GameBoard game = new GameBoard(height, width, winLength);

    // Display object to handle the graphical representation of the game
    Display display = new Display(game);

    // The first player's turn is set to RED
    GameBoard.Slot gamePlayerTurn = GameBoard.Slot.RED;

    // Shared input utility to synchronize input between players
    UserIO sharedInput = new UserIO();

    // Create a server socket and a fixed thread pool for handling clients
    try (ServerSocket serverSocket = new ServerSocket(PORT);
         ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS)) {

      System.out.println("[Server " + SERVER_ID + "] starting with id " + SERVER_ID);
      System.out.println("[Server " + SERVER_ID + "] listening on port " + PORT);

      // Main server loop: handles player connections and game lifecycle
      while (!serverSocket.isClosed()) {

        if (numberOfPlayerConnected == 2) { // Both players are connected
          // Indicate to both players that the game is starting
          playerConnected[0].isGameStarting = true;
          playerConnected[1].isGameStarting = true;
          System.out.println("Waiting until the game finishes...");

          // Wait until the game is finished
          while (!playerConnected[0].isGameFinished) {
            try {
              TimeUnit.SECONDS.sleep(1); // Delay to prevent busy-waiting
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }

          System.out.println("Game finished, server restarting...");

          // Reset game-related variables for the next session
          gamePlayerTurn = GameBoard.Slot.RED; // Reset the turn to RED
          numberOfPlayerConnected = 0; // Reset player count
          playerConnected = new ClientHandler[NUMBER_OF_THREADS]; // Clear player handlers
          game = new GameBoard(height, width, winLength); // Reset the game board
          display = new Display(game); // Reset the display

        } else { // Less than two players are connected

          // Accept a new client connection
          Socket clientSocket = serverSocket.accept();

          // Create a new ClientHandler for the connected player
          playerConnected[numberOfPlayerConnected] =
                  new ClientHandler(clientSocket, game, display, gamePlayerTurn, sharedInput);

          // Submit the client handler task to the thread pool for execution
          executor.submit(playerConnected[numberOfPlayerConnected]);

          // Increment the player count
          ++numberOfPlayerConnected;

          // Alternate the turn for the next player
          gamePlayerTurn = gamePlayerTurn == GameBoard.Slot.BLUE ? GameBoard.Slot.RED : GameBoard.Slot.BLUE;
        }
      }

    } catch (IOException e) { // Handle exceptions related to I/O operations
      System.out.println("[Server " + SERVER_ID + "] exception: " + e);
    }
  }


  static class ClientHandler implements Runnable {

    private final Socket socket;
    private final GameBoard game;
    private final Display display;
    private final GameBoard.Slot playerColor;
    private final UserIO sharedInput;
    private boolean isGameStarting;
    private boolean isGameFinished;


    /**
     * Creates a new instance for a client.
     *
     * @param socket       The socket associated with the client.
     * @param game         An instance of {@link GameBoard} to manage the game board.
     * @param display      An instance of {@link Display} to show the game state.
     * @param playerColor  The current player's color (RED or BLUE).
     * @param sharedInput  A shared input instance for player commands.
     *
     * <p>This class implements {@link Runnable} to be executed in a separate thread.
     * It listens for client commands and updates the game state accordingly.</p>
     */
    public ClientHandler(Socket socket, GameBoard game, Display display, GameBoard.Slot playerColor, UserIO sharedInput) {
      this.socket = socket;
      this.game = game;
      this.display = display;
      this.playerColor = playerColor;
      this.sharedInput = sharedInput;
      this.isGameStarting = false;
      this.isGameFinished = false;
    }

    /**
     * Entry point of the thread for handling client interactions.
     *
     * This method performs the following steps:
     *
     * Synchronizes the game state and sends initial commands to the client.
     * Receives commands from the client and updates the game board.
     * Handles command errors and checks for win/draw conditions.
     * Transmits results or the current state to the other player.
     *
     * Communication is handled via {@link BufferedReader} and {@link BufferedWriter}
     * for reliable, UTF-8 encoded exchanges.
     */
    @Override
    public void run() {

      // Using try-with-resources to ensure the socket and streams are closed properly
      try (socket;
           BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
           BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

        // Log the connection of a new client
        System.out.println(
                "[Server " + SERVER_ID + "] new client connected from "
                        + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

        // Send initialization message with game rules to the client
        System.out.println("Sending instructions to the client...");
        String initMessage = "INIT " + game.getHeight() + " " + game.getWidth() + " " + game.getWinLength() + "\n";
        out.write(initMessage);
        out.flush();

        // Wait until both players are connected and ready to start the game
        while (!this.isGameStarting) {
          TimeUnit.MILLISECONDS.sleep(100); // Avoids busy-waiting
        }

        // Notify the client whether it's their turn to play or wait
        System.out.println("Sending initial turn info to client...");
        out.write((this.playerColor == GameBoard.Slot.RED ? "PLAY" : "WAIT") + "\n");
        out.flush();

        String message;  // Holds incoming messages from the client
        boolean quit = false; // Indicates whether the client session should end

        while (!quit) {
          // Process commands only if it's the client's turn
          if (game.getPlayerTurn() == this.playerColor) {
            boolean errorOccurred;
            do {
              // Waiting for a valid command from the client
              System.out.println("Waiting for PLACE command...");
              message = in.readLine(); // Read the command from the client
              String[] parses = message.split(" "); // Parse the command into parts

              // Handle the PLACE command to drop a token in a specific column
              if (parses[0].equals("PLACE") && parses.length == 2) {
                try {
                  int column = Integer.parseInt(parses[1]); // Parse the column index
                  // Validate the column and attempt to place the token
                  if (UserIO.isInputValid(column, 0, game.getWidth() - 1)) {
                    sharedInput.setChosenColum(column);
                    int row = game.addSlot(sharedInput.getChosenColum());
                    if (row != -1) { // Successfully placed token
                      sharedInput.setChosenRow(row);
                      out.write("TOKEN_PLACED\n"); // Notify success
                      out.flush();
                      errorOccurred = false;
                    } else { // Column is full
                      System.out.println("ERROR: Column full.");
                      out.write("ERROR " + ERROR_COLUMN_FULL + "\n");
                      out.flush();
                      errorOccurred = true;
                    }
                  } else { // Invalid column index
                    System.out.println("ERROR: Out of index.");
                    out.write("ERROR " + ERROR_OUT_OF_INDEX + "\n");
                    out.flush();
                    errorOccurred = true;
                  }
                } catch (NumberFormatException e) { // Handle invalid number format
                  System.out.println("ERROR: Not a valid number.");
                  out.write("ERROR " + ERROR_NOT_A_NUMBER + "\n");
                  out.flush();
                  errorOccurred = true;
                }
              } else if (parses[0].equals("FF15")) { // Handle player forfeit
                System.out.println("Player chose to forfeit.");
                out.write("OK_FF15\n");
                out.flush();
                // Set the game status to indicate the opponent wins
                game.setGameStatus(this.playerColor == GameBoard.Slot.RED
                        ? GameBoard.GameStatus.BLUE_WINS
                        : GameBoard.GameStatus.RED_WINS);
                errorOccurred = false;
              } else { // Invalid command
                System.out.println("ERROR: Invalid command.");
                out.write("ERROR " + ERROR_INV_COMMAND + "\n");
                out.flush();
                errorOccurred = true;
              }
            } while (errorOccurred); // Repeat until a valid command is received

            // Update the game board and display after a valid move
            display.showGameBoard();

            // Check game state after the player's move
            if (!message.equals("FF15")) {
              if (!game.checkDrawCondition()) { // Check for a draw
                game.checkWinCondition(sharedInput.getChosenColum(), sharedInput.getChosenRow()); // Check for a win
              }
            }

            // Pass the turn to the other player
            game.invertPlayerTurn();

          } else { // Wait for the other player's turn
            System.out.println("Waiting for the other player...");
            while (game.getPlayerTurn() != this.playerColor) {
              TimeUnit.MILLISECONDS.sleep(500); // Avoid busy-waiting
            }
          }

          // Handle game status notifications
          if (game.getGameStatus() == GameBoard.GameStatus.GAME_CONTINUE && game.getPlayerTurn() == this.playerColor) {
            // Notify the client that the game is ongoing and the token placement
            out.write("GAME_CONTINUE\n");
            out.flush();
            out.write("INSERTED " + sharedInput.getChosenColum() + "\n");
            out.flush();
          } else if (game.getGameStatus() == GameBoard.GameStatus.DRAW) {
            // Notify the client of a draw
            System.out.println("Game ended in a draw.");
            out.write("DRAW\n");
            out.flush();
            quit = true;
          } else if (game.getGameStatus() == GameBoard.GameStatus.BLUE_WINS) {
            // Notify the client of a win or loss based on their color
            System.out.println("Blue wins the game.");
            out.write((playerColor == GameBoard.Slot.BLUE ? "WIN" : "LOSE") + "\n");
            out.flush();
            quit = true;
          } else if (game.getGameStatus() == GameBoard.GameStatus.RED_WINS) {
            System.out.println("Red wins the game.");
            out.write((playerColor == GameBoard.Slot.RED ? "WIN" : "LOSE") + "\n");
            out.flush();
            quit = true;
          }

          if (quit && game.getPlayerTurn() == this.playerColor) {
            // Notify the client of the final move if quitting
            out.write("INSERTED " + sharedInput.getChosenColum() + "\n");
            out.flush();
          }
        }

        // Mark the game session as finished
        isGameFinished = true;
        System.out.println("[Server " + SERVER_ID + "] Closing connection...");

      } catch (IOException | InterruptedException e) {
        // Log exceptions occurring during the session
        System.out.println("[Server " + SERVER_ID + "] Exception: " + e);
      }
    }
  }
}