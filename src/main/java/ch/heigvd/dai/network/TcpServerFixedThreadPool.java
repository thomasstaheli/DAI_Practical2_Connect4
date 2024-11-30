package ch.heigvd.dai.network;

import ch.heigvd.dai.Display;
import ch.heigvd.dai.GameBoard;

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
    private int numberOfPlayerConnected;
    private ClientHandler[] playerConnected;

    private static final int height = 5;
    private static final int width = 5;
    private static final int winLenght = 4;

    public TcpServerFixedThreadPool() {

        this.numberOfPlayerConnected = 0;
        this.playerConnected = new ClientHandler[NUMBER_OF_THREADS];
        GameBoard game = new GameBoard(height, width, winLenght);
        Display display = new Display(game);
        GameBoard.Slot gamePlayerTurn = GameBoard.Slot.RED;

        try (ServerSocket serverSocket = new ServerSocket(PORT);

             ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS) ) {
            System.out.println("[Server " + SERVER_ID + "] starting with id " + SERVER_ID);
            System.out.println("[Server " + SERVER_ID + "] listening on port " + PORT);

            while (!serverSocket.isClosed()) {
                // Attente de deux joueurs
                if(this.numberOfPlayerConnected == 2) {
                    // We are indicating that the game is starting
                    this.playerConnected[0].isGameStarting = true;
                    this.playerConnected[1].isGameStarting = true;
                    System.out.println("Waiting until the game finished ...");
                    // Attente que la partie se finisse
                    while(!this.playerConnected[0].isGameFinished) {}
                    System.out.println("Game finished, server restarting ...");
                    // Reset des valeurs pour une nouvelle partie
                    gamePlayerTurn = GameBoard.Slot.RED;
                    this.numberOfPlayerConnected = 0;
                    // this.playerConnected = new ClientHandler[NUMBER_OF_THREADS];

                } else {
                    // Tant qu'il n'y a pas deux joueurs
                    Socket clientSocket = serverSocket.accept();
                    this.playerConnected[numberOfPlayerConnected] = new ClientHandler(clientSocket, game, display, gamePlayerTurn);
                    executor.submit(this.playerConnected[numberOfPlayerConnected]);
                    ++this.numberOfPlayerConnected;
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
        private GameBoard game;
        private Display display;
        private GameBoard.Slot playerColor;
        private boolean isGameStarting;
        private boolean isGameFinished;

        // TODO : Debug
        private int id;

        public ClientHandler(Socket socket, GameBoard game, Display display, GameBoard.Slot playerColor) {
            this.socket = socket;
            this.game = game;
            this.display = display;
            this.playerColor = playerColor;
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
                String initMessage = "INIT " + height + " " + width + " " + winLenght + "\n";
                out.write(initMessage);
                out.flush();

                // Synchronise the start of the two client
                while (!this.isGameStarting) {
                    // I had to put a sleep here to avoid the compiler optimisation of this while
                    TimeUnit.MILLISECONDS.sleep(10);
                }

                System.out.println("Sending ... " + SERVER_ID);
                // Sending WAIT or PLAY
                out.write((this.playerColor == GameBoard.Slot.RED ? "PLAY" : "WAIT") + "\n");
                out.flush();

                String message;
                String[] parses;

                int chosenRow = 0;

                System.out.println("Waiting in the while true ...");
                while(true) {

                    if(game.getPlayerTurn() == this.playerColor) {

                        System.out.println("Waiting PLACE command ...");
                        message = in.readLine();
                        parses = message.split(" ");

                        if(parses[0].equals("PLACE")) {
                            game.setChosenColumn(Integer.parseInt(parses[1]));
                            chosenRow = game.addSlot(game.getChosenColumn());
                            out.write("TOKEN_PLACED" + "\n");
                            out.flush();
                        } else {
                            System.out.println("ERROR : Player did not send PLACE command");
                        }

                        display.showGameBoard();
                        game.invertPlayerTurn();

                    } else {
                        System.out.println("Waiting for player turn ...");
                        while(game.getPlayerTurn() != this.playerColor) {
                            TimeUnit.MILLISECONDS.sleep(500);
                        }

                    }

                    // TODO déplacer ce code après
                    if(game.getPlayerTurn() == this.playerColor) {
                        if (game.checkDrawCondition()) {
                            System.out.println("DRAW");
                            break;
                        } else if (game.checkWinCondition(game.getChosenColumn(), chosenRow) != GameBoard.GameStatus.GAME_CONTINUE) {
                            System.out.println("WIN");
                            break;
                        } else {
                            System.out.println("Sending to the other player : game continue and inserted cmd");
                            out.write("GAME_CONTINUE" + "\n");
                            out.flush();
                            out.write("INSERTED " + game.getChosenColumn() + "\n");
                            out.flush();
                        }
                    }
                }

                isGameFinished = true;
                /*
                if(in.readLine().equals("PING")){
                    out.write("PONG\n");
                    out.flush();
                }
                else {
                    out.write("HELLO" + "\n");
                    out.flush();
                }
                */

                System.out.println(
                        "[Server " + SERVER_ID + "] sending response to client: " + "HELLO");

                System.out.println("[Server " + SERVER_ID + "] closing connection");
            } catch (IOException | InterruptedException e) {
                System.out.println("[Server " + SERVER_ID + "] exception: " + e);
            }
        }

    }
}