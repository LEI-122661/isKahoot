package isKahoot.Server;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class GameServer {
    public static final int PORT = 12025;
    public static final int MAX_CLIENTS = 4;

    private ServerSocket server;

    public void runServer() {
        try {
            server = new ServerSocket(PORT);
            System.out.println("Servidor à espera de " + MAX_CLIENTS + " clientes...");

            int connected = 0;

            while (connected < MAX_CLIENTS) {
                waitForConnection(++connected);
            }

            System.out.println("Já ligaram " + MAX_CLIENTS + " clientes. Começar jogo.");
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

    private void waitForConnection(int clientId) throws IOException {
        Socket connection = server.accept(); // espera ligação
        ConnectionHandler handler = new ConnectionHandler(connection, clientId);
        handler.start();
        System.out.println("Started new connection... Cliente " + clientId);
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.runServer();
    }

}
