package isKahoot.Server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ConnectionHandler extends Thread {

    private final Socket connection;
    private final int clientId;
    private Scanner in;
    private PrintWriter out;

    public ConnectionHandler(Socket connection, int clientId) {
        this.connection = connection;
        this.clientId = clientId;
    }

    @Override
    public void run() {
        try {
            setStreams();
            processConnection();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void setStreams() throws IOException {
        out = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(connection.getOutputStream())
                ), true
        );
        in = new Scanner(connection.getInputStream());

        out.println("Ligado ao servidor. És o cliente #" + clientId);
    }

    private void processConnection() {
        while (true) {
            if (!in.hasNextLine()) break; // cliente fechou
            String str = in.nextLine();
            if (str.equals("FIM"))
                break;
            System.out.println("Cliente " + clientId + " -> " + str);
            out.println("Eco[" + clientId + "]: " + str);
        }
    }

    private void closeConnection() {
        try {
            if (connection != null)
                connection.close();
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            System.out.println("Ligação com cliente " + clientId + " fechada.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }
}
