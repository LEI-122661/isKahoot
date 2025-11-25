package isKahoot.Server;


import isKahoot.Clients.Client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameServer {
    public static final int PORT = 12025;
    public static final int MAX_CLIENTS = 4;

    private ServerSocket server;
    private List<ConnectionHandler> clients = new ArrayList<>();

    public void runServer() {
        try {
            server = new ServerSocket(PORT);
            System.out.println("Servidor à espera de " + MAX_CLIENTS + " clientes...");

            int connected = 0;
            while (connected < MAX_CLIENTS) {
                Socket connection = server.accept(); // espera ligação
                ConnectionHandler handler = new ConnectionHandler(connection, ++connected);
                handler.start();
                clients.add(handler);
                System.out.println("Started new connection... Cliente " + connected);
            }

            System.out.println("Já ligaram " + MAX_CLIENTS + " clientes. Começar jogo.");

            // Agora podes iniciar o GameHandler
            GameHandler gameHandler = new GameHandler(clients);
            gameHandler.start();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.runServer();
    }

}
