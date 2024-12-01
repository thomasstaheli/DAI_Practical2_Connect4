package ch.heigvd.dai.network;

import ch.heigvd.dai.Display;
import ch.heigvd.dai.GameBoard;
import ch.heigvd.dai.UserIO;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class TcpClient {

    private String host;
    private static final int PORT = 6433;
    private static final int CLIENT_ID = (int) (Math.random() * 1000000);
    private static final String TEXTUAL_DATA = "PING";

    public TcpClient(String ip) throws IllegalArgumentException {
        // Vérifier que l'adresse IP est non null
        if (ip == null) {
            throw new IllegalArgumentException("L'adresse IP ne peut pas être nulle.");
        }

        // Diviser l'IP par les points
        String[] parts = ip.split("\\.");

        // Vérifier qu'elle est composée de 4 parties
        if (parts.length != 4) {
            throw new IllegalArgumentException("L'adresse IP doit contenir exactement 4 parties séparées par des points.");
        }

        // Vérifier que chaque partie est un nombre valide
        for (String part : parts) {
            try {
                int value = Integer.parseInt(part);

                // Vérifier que le nombre est dans la plage valide
                if (value < 0 || value > 255) {
                    throw new IllegalArgumentException("Chaque partie de l'adresse IP doit être comprise entre 0 et 255.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Chaque partie de l'adresse IP doit être un nombre entier.");
            }
        }

        // Si tout est valide, assigner l'IP à l'attribut `host`
        this.host = ip;
    }

    public void run() {

        System.out.println("[Client " + CLIENT_ID + "] starting with id " + CLIENT_ID);
        System.out.println("[Client " + CLIENT_ID + "] connecting to " + this.host + ":" + PORT);

        int width, height, winLenght;
        int chosenColumn;
        int chosenRow;

        GameBoard game;
        Display display;

        try (Socket socket = new Socket(this.host, PORT);
             BufferedReader in =
                     new BufferedReader(
                             new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out =
                     new BufferedWriter(
                             new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            System.out.println("[Client " + CLIENT_ID + "] connected to " + this.host + ":" + PORT);

            // READ width height winlenght ...
            System.out.println("Waiting for Server, to send height, widht and len win to start the game.");
            String message  = in.readLine();
            String[] parses = message.split(" ");
            // Check for [0] ? if it is a command
            height    = Integer.parseInt(parses[1]);
            width     = Integer.parseInt(parses[2]);
            winLenght = Integer.parseInt(parses[3]);
            System.out.println("SUCCES ! Width = " + width + ", Height = " + height + ", Win Lenght = " + winLenght);

            // Game part
            System.out.println("Init the boardgame and display ...");
            game = new GameBoard(height, width, winLenght);
            display = new Display(game);

            // Waiting until the game start
            System.out.println("Waiting for another player ...");
            // Receive PLAY or WAIT
            String playerTurn = in.readLine();

            if(playerTurn.equals("PLAY")) {
                System.out.println("Game Start and you start playing ! :D");
            } else {
                System.out.println("Game Start and you are waiting first ! :|");
            }

            do {

                // READ if game continue or not
                if(playerTurn.equals("PLAY")) {
                    chosenColumn = UserIO.getIntInput(0, width - 1);
                    chosenRow    = game.addSlot(chosenColumn);

                    while (chosenRow == -1) {
                        //get another column if full
                        System.out.println("Cette colonne est complète, veuillez en choisir une autre.");
                        chosenColumn = UserIO.getIntInput(0, width - 1);
                        chosenRow    = game.addSlot(chosenColumn);
                    }
                    // PLACE <column>
                    System.out.println("PLACE " + chosenColumn + "\n");
                    out.write("PLACE " + chosenColumn + "\n");
                    out.flush();

                    System.out.println("Waiting for server response ...");
                    if(in.readLine().equals("TOKEN_PLACED")) {
                        System.out.println("TOKEN_PLACED");
                    } else {
                        System.out.println("ERROR : message unknown");
                    }

                    playerTurn = "WAIT";

                } else {
                    System.out.println("Waiting other player is placing ...");
                    // GAME_CONTINUE
                    message = in.readLine();
                    if(!message.equals("GAME_CONTINUE")) {
                        // victoire ou draw
                        break;
                    }

                    message = in.readLine();
                    parses = message.split(" ");
                    // INSERTED <column>
                    game.addSlot(Integer.parseInt(parses[1]));
                    System.out.println("The other player placed his token in column " + Integer.parseInt(parses[1]));
                    System.out.println("This is now your turn ! ");
                    playerTurn = "PLAY";
                }

                // Changing the turn
                game.invertPlayerTurn();
                display.showGameBoard();

            } while (true);

            if(message.equals("WIN")) {
                System.out.println("YOU WIN !!");
            } else {
                display.showGameBoard();
                System.out.println("YOU LOSE !!");
            }

            System.out.println("[Client " + CLIENT_ID + "] response from server: " + in.readLine());
            System.out.println("[Client " + CLIENT_ID + "] closing connection");

        } catch (IOException e) {
            System.out.println("[Client " + CLIENT_ID + "] exception: " + e);
        }
    }

}