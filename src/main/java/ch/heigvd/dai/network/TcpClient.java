package ch.heigvd.dai.network;

import javax.management.RuntimeOperationsException;
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

        try (Socket socket = new Socket(this.host, PORT);
             BufferedReader in =
                     new BufferedReader(
                             new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out =
                     new BufferedWriter(
                             new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)); ) {
            System.out.println("[Client " + CLIENT_ID + "] connected to " + this.host + ":" + PORT);
            System.out.println(
                    "[Client "
                            + CLIENT_ID
                            + "] sending textual data to server "
                            + this.host
                            + ":"
                            + PORT
                            + ": "
                            + TEXTUAL_DATA);

            out.write(TEXTUAL_DATA + "\n");
            out.flush();

            System.out.println("[Client " + CLIENT_ID + "] response from server: " + in.readLine());

            System.out.println("[Client " + CLIENT_ID + "] closing connection");
        } catch (IOException e) {
            System.out.println("[Client " + CLIENT_ID + "] exception: " + e);
        }
    }
}