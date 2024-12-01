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

  public TcpServerFixedThreadPool(int height, int width, int winLenght) {


    int numberOfPlayerConnected = 0;
    ClientHandler[] playerConnected = new ClientHandler[NUMBER_OF_THREADS];
    GameBoard game = new GameBoard(height, width, winLenght);
    Display display = new Display(game);
    GameBoard.Slot gamePlayerTurn = GameBoard.Slot.RED;
    UserIO sharedInput = new UserIO();

    try (ServerSocket serverSocket = new ServerSocket(PORT);
         ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS) ) {

      System.out.println("[Server " + SERVER_ID + "] starting with id " + SERVER_ID);
      System.out.println("[Server " + SERVER_ID + "] listening on port " + PORT);

      while (!serverSocket.isClosed()) {
        // Attente de deux joueurs
        if(numberOfPlayerConnected == 2) {
          // We are indicating that the game is starting
          playerConnected[0].isGameStarting = true;
          playerConnected[1].isGameStarting = true;
          System.out.println("Waiting until the game finished ...");
          // Attente que la partie se finisse
          while(!playerConnected[0].isGameFinished) {
            try {
              TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
          System.out.println("Game finished, server restarting ...");
          // Reseting local variable, to restart a new Connect 4 game
          gamePlayerTurn = GameBoard.Slot.RED;
          numberOfPlayerConnected = 0;
          playerConnected = new ClientHandler[NUMBER_OF_THREADS];
          // Reseting the board and resetupping the display
          game = new GameBoard(height, width, winLenght);
          display = new Display(game);

        } else {
          // Tant qu'il n'y a pas deux joueurs
          Socket clientSocket = serverSocket.accept();
          // Création d'un nouveau joueur
          playerConnected[numberOfPlayerConnected] =
                  new ClientHandler(clientSocket, game, display, gamePlayerTurn, sharedInput);
          executor.submit(playerConnected[numberOfPlayerConnected]);
          ++numberOfPlayerConnected;
          // Si le premier joeuur commence à jouer alors l'autre commence par attendre
          gamePlayerTurn = gamePlayerTurn == GameBoard.Slot.BLUE ? GameBoard.Slot.RED : GameBoard.Slot.BLUE;
        }
      }
    } catch (IOException e) {
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

    public ClientHandler(Socket socket, GameBoard game, Display display, GameBoard.Slot playerColor, UserIO sharedInput) {
      this.socket = socket;
      this.game = game;
      this.display = display;
      this.playerColor = playerColor;
      this.sharedInput = sharedInput;
      this.isGameStarting = false;
      this.isGameFinished = false;
    }

    @Override
    public void run() {

      try (socket; // This allows to use try-with-resources with the socket
           BufferedReader in =
                   new BufferedReader(
                           new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
           BufferedWriter out =
                   new BufferedWriter(
                           new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

        System.out.println(
                "[Server "
                        + SERVER_ID
                        + "] new client connected from "
                        + socket.getInetAddress().getHostAddress()
                        + ":"
                        + socket.getPort());

        // Send the ruleset to the client
        System.out.println("Sending him instructions of the game ...");
        String initMessage = "INIT " + game.getHeight() + " " + game.getWidth() + " " + game.getWinLength() + "\n";
        out.write(initMessage);
        out.flush();

        // Synchronise the start of the two client
        while (!this.isGameStarting) {
          // I had to put a sleep here to avoid the compiler optimisation of this while
          TimeUnit.MILLISECONDS.sleep(100);
        }

        System.out.println("Sending ... " + SERVER_ID);
        // Sending WAIT or PLAY
        out.write((this.playerColor == GameBoard.Slot.RED ? "PLAY" : "WAIT") + "\n");
        out.flush();

        String message;
        String[] parses;
        boolean quit = false;

        while(!quit) {

          if(game.getPlayerTurn() == this.playerColor) {

            boolean errorOccurred;
            do {

              System.out.println("Waiting PLACE command ...");
              message = in.readLine();
              parses = message.split(" ");

              if (parses[0].equals("PLACE") && parses.length == 2) {
                try {
                  // Check if we can convert the string to a number
                  int number = Integer.parseInt(parses[1]);
                  // Check if the index sent is valid to enter the game board
                  if(UserIO.isInputValid(number, 0, game.getWidth() - 1)) {
                    sharedInput.setChosenColum(number);
                    // The addSlot method return the row, where the token was placed
                    number = game.addSlot(sharedInput.getChosenColum());
                    if(number != -1) {
                      sharedInput.setChosenRow(number);
                      out.write("TOKEN_PLACED" + "\n");
                      out.flush();
                      errorOccurred = false;
                    } else {
                      System.out.println("ERROR OUT OF INDEX");
                      out.write("ERROR " + ERROR_COLUMN_FULL + "\n");
                      out.flush();
                      errorOccurred = true;
                    }
                  } else {
                    System.out.println("ERROR OUT OF INDEX");
                    out.write("ERROR " + ERROR_OUT_OF_INDEX + "\n");
                    out.flush();
                    errorOccurred = true;
                  }
                } catch (NumberFormatException e) {
                  // The string can't be converted to an int
                  System.out.println("ERROR : Player has to put a number.");
                  out.write("ERROR " + ERROR_NOT_A_NUMBER + "\n");
                  out.flush();
                  errorOccurred = true;
                }
              } else if(parses[0].equals("FF")) {
                System.out.println("User want to ff !!");
                // TODO send back a cmd
                errorOccurred = false;
              }
              else {
                out.write("ERROR " + ERROR_INV_COMMAND + "\n");
                out.flush();
                errorOccurred = true;
                System.out.println("ERROR : Player did not send PLACE command");
              }
            } while (errorOccurred);

            display.showGameBoard();

            if(!game.checkDrawCondition()) {
              game.checkWinCondition(sharedInput.getChosenColum(), sharedInput.getChosenRow());
            }

            game.invertPlayerTurn();

          } else {
            System.out.println("Waiting for player turn ...");
            while(game.getPlayerTurn() != this.playerColor) {
              TimeUnit.MILLISECONDS.sleep(500);
            }
          }

          // Condition de jeu, afin de savoir s'il est terminé ou s'il continue
          if(game.getGameStatus() == GameBoard.GameStatus.GAME_CONTINUE && game.getPlayerTurn() == this.playerColor) {
            System.out.println("Sending to the other player : game continue and inserted cmd");
            out.write("GAME_CONTINUE" + "\n");
            out.flush();
            out.write("INSERTED " + sharedInput.getChosenColum() + "\n");
            out.flush();
          }  else if(game.getGameStatus() == GameBoard.GameStatus.DRAW) {
            System.out.println("GAME DRAW !");
            out.write("DRAW" + "\n");
            out.flush();
            quit = true;
          } else if(game.getGameStatus() == GameBoard.GameStatus.BLUE_WINS) {
            System.out.println("GAME BLUE_WINS !");
            out.write((playerColor == GameBoard.Slot.BLUE ? "WIN" : "LOSE") + "\n");
            out.flush();
            quit = true;
          } else if(game.getGameStatus() == GameBoard.GameStatus.RED_WINS) {
            System.out.println("GAME RED_WINS !");
            out.write((playerColor == GameBoard.Slot.RED ? "WIN" : "LOSE") + "\n");
            out.flush();
            quit = true;
          }

          if(quit) {
            if(game.getPlayerTurn() == this.playerColor) {
              out.write("INSERTED " + sharedInput.getChosenColum() + "\n");
              out.flush();
            }
            break;
          }
        }

        isGameFinished = true;
        System.out.println("[Server " + SERVER_ID + "] closing connection");

      } catch (IOException | InterruptedException e) {
        System.out.println("[Server " + SERVER_ID + "] exception: " + e);
      }
    }

  }
}