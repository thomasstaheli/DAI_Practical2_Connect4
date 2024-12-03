package ch.heigvd.dai.network;

import ch.heigvd.dai.Display;
import ch.heigvd.dai.GameBoard;
import ch.heigvd.dai.util.UserIO;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * This class represents a TCP client that connects to a game server, communicates with it,
 * and manages the gameplay from the client side.
 */
public class TcpClient {

  private final String host;
  private final int port;
  private static final int CLIENT_ID = (int) (Math.random() * 1000000);

  /**
   * Constructor for TcpClient.
   *
   * @param ip The server's IP address.
   * @throws IllegalArgumentException if the provided IP address is null or invalid.
   */
  public TcpClient(String ip, int port) throws IllegalArgumentException {
    // Checking the ip addr
    if (ip == null) {
      throw new IllegalArgumentException("L'adresse IP ne peut pas être nulle.");
    }

    String[] parts = ip.split("\\.");

    // Checking if it's an IPV4 ip
    if (parts.length != 4) {
      throw new IllegalArgumentException("L'adresse IP doit contenir exactement 4 parties séparées par des points.");
    }

    // Checking that every number of the ip is valid
    for (String part : parts) {
      try {
        int value = Integer.parseInt(part);

        if (value < 0 || value > 255) {
          throw new IllegalArgumentException("Chaque partie de l'adresse IP doit être comprise entre 0 et 255.");
        }
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Chaque partie de l'adresse IP doit être un nombre entier.");
      }
    }

    // If everything is valid, so we set the host ip
    this.host = ip;
    this.port = port;
  }

  /**
   * Runs the TCP client to connect to the server, play the game, and handle game logic.
   */
  public void run() {

    System.out.println("[Client " + CLIENT_ID + "] starting with id " + CLIENT_ID);
    System.out.println("[Client " + CLIENT_ID + "] connecting to " + this.host + ":" + this.port);

    int width, height, winLenght;
    int chosenColumn;

    GameBoard game;
    Display display;

    try (Socket socket = new Socket(this.host, this.port);
         BufferedReader in =
                 new BufferedReader(
                         new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
         BufferedWriter out =
                 new BufferedWriter(
                         new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

      System.out.println("[Client " + CLIENT_ID + "] connected to " + this.host + ":" + this.port);

      // Waiting to receive the ruleseet of the game
      System.out.println("Waiting for Server, to send height, widht and lenght win to start the game.");
      String message  = in.readLine();
      String[] parses = message.split(" ");
      // Receiving from the server, the ruleset
      height    = Integer.parseInt(parses[1]);
      width     = Integer.parseInt(parses[2]);
      winLenght = Integer.parseInt(parses[3]);
      // Indicate the Win lenght to win the game
      System.out.println("SUCCES ! You have to put " + winLenght + " into the board to WIN !");

      // Setuping the game and display
      System.out.println("Init the boardgame and display ...");
      game = new GameBoard(height, width, winLenght);
      display = new Display(game);

      // Waiting until the game start
      System.out.println("Waiting for another player ...");
      // Receive PLAY or WAIT
      String playerTurn = in.readLine();
      System.out.println("Good luck !");

      if(playerTurn.equals("PLAY")) {
        System.out.println("Game Start and you start playing ! :D");
      } else {
        System.out.println("Game Start and you are waiting first ! :|");
      }

      do {
        // If it's his turn to play
        if(playerTurn.equals("PLAY")) {

          boolean errorOccured;
          do {
            System.out.println("Chose a column between " + 0 + " et " + (game.getWidth() - 1));
            System.out.println("Use command : PLACE <column number>    => to play");
            String userInput = UserIO.getUserInput();
            // Expecting command :
            // PLACE <column>
            out.write(userInput + "\n");
            out.flush();

            System.out.println("Waiting for server response ...");

            message = in.readLine();
            if (message.equals("TOKEN_PLACED")) {
              errorOccured = false;
              System.out.println("TOKEN_PLACED");
              parses       = userInput.split(" ");
              // If everything is ok, we place in the local player board
              chosenColumn = Integer.parseInt(parses[1]);
              game.addSlot(chosenColumn);
            } else if(message.equals("OK_FF15")) {
              errorOccured = false;
            } else {
              // See which error occurred
              switch (parses[1]) {
                case "-1":
                  System.out.println("ERROR : Unknown command.");
                  break;
                case "1" :
                  System.out.println("ERROR : Out of index.");
                  break;
                case "2" :
                  System.out.println("ERROR : Index is not a number.");
                  break;
                case "3" :
                  System.out.println("ERROR : The colum is full.");
                  break;
              }
              errorOccured = true;
            }
          } while (errorOccured);
          // Changing Player turn
          if(message.equals("OK_FF15")) break;

          playerTurn = "WAIT";

        } else {
          System.out.println("Waiting other player is placing ...");
          // GAME_CONTINUE
          message = in.readLine();
          if(!message.equals("GAME_CONTINUE")) {
            // WIN or DRAW or LOSE
            // The game stop
            break;
          }

          // Expecting command :
          // INSERTED <column>
          message = in.readLine();
          parses = message.split(" ");
          game.addSlot(Integer.parseInt(parses[1]));
          // Giving information to the user
          System.out.println("The other player placed his token in column " + Integer.parseInt(parses[1]));
          System.out.println("This is now your turn ! ");
          // Changing player Turn
          playerTurn = "PLAY";
        }

        // Changing the game turn and display the game board
        game.invertPlayerTurn();
        display.showGameBoard();

      } while (true);

      // After loop ended, we check the game status
      if(message.equals("OK_FF15")) {
        System.out.println("You lost by forfeit ...");
      }
      else if(message.equals("WIN")) {
        System.out.println("YOU WIN !!");
      } else {
        // Receiving the last INSERTED to update the game board
        // INSERTED <column>
        message = in.readLine();
        parses = message.split(" ");
        game.addSlot(Integer.parseInt(parses[1]));
        // Giving information to the user
        System.out.println("The other player placed his token in column " + Integer.parseInt(parses[1]));
        display.showGameBoard();
        System.out.println("YOU LOSE !!");
      }
      // End of communication
      System.out.println("[Client " + CLIENT_ID + "] closing connection");

    } catch (IOException e) {
      System.out.println("[Client " + CLIENT_ID + "] exception: " + e);
    }
  }

}
