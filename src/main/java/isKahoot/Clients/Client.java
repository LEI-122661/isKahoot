package isKahoot.Clients;

import isKahoot.GUI;
import isKahoot.Server.GameHandler;
import isKahoot.Server.GameServer;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket connection; // Connection
    private Scanner in;        // Stream reader
    private PrintWriter out;   // Stream writer
    private GUI gui

    public void runClient() {
        try {
            connectToServer();
            setStreams();
            processConnection();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void connectToServer() throws IOException {
        InetAddress endereco = InetAddress.getByName(null); // localhost
        System.out.println("Endereco: " + endereco);
        connection = new Socket(endereco, GameServer.PORT);
        System.out.println("Socket: " + connection);
    }

    private void setStreams() throws IOException {
        out = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(connection.getOutputStream())
                ), true
        );
        in = new Scanner(connection.getInputStream());
    }

    private void processConnection() {
        // ler a mensagem inicial do servidor (se existir)
        // ler mudanças de tela de acordo com o servidor TODO
        while (in.hasNextLine()) {
                String msg = in.nextLine();
                System.out.println("Servidor: " + msg);

                // TODO: aqui decides o que fazer:
                // if (msg.startsWith("SCREEN:LOBBY"))  -> mostra tela lobby
                // if (msg.startsWith("SCREEN:QUESTION")) -> mostra pergunta
                // if (msg.startsWith("SCREEN:RESULTS")) -> mostra resultados

        }
        out.println("FIM");  // indica ao servidor que quer terminar
    }

    public void closeConnection() {
        try {
            if (connection != null)
                connection.close();
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

