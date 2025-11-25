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
    private GUI gui;

    public void runClient() {
        try {
            connectToServer();
            setStreams();
            createGUI();
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
        // ler mudanças de tela de acordo com o servidor
        while (in.hasNextLine()) {
            String msg = in.nextLine();
            javax.swing.SwingUtilities.invokeLater(() -> handleServerMessage(msg));
        }
    }

    private void handleServerMessage(String msg) {
            // LIGA AS TELAS DA GUI ÀS MENSAGENS DO SERVIDOR

            if (msg.startsWith("SCREEN:LOBBY")) {
                // Mostra tela de lobby (aguardando o início)
                gui.showLobby();

            }else if (msg.startsWith("SCREEN:QUESTION:")) {
                // Mensagem esperada: SCREEN:QUESTION:Texto da pergunta|Opção1|Opção2|Opção3|Opção4|10
                String payload = msg.substring("SCREEN:QUESTION:".length());
                String[] parts = payload.split("\\|");
                if (parts.length >= 6) {
                    String questionText = parts[0];
                    String[] opts = new String[]{parts[1], parts[2], parts[3], parts[4]};
                    int seconds;
                    // Protege conversão
                    try {
                        seconds = Integer.parseInt(parts[5]);
                    } catch (NumberFormatException e) {
                        seconds = 10; // Valor default
                    }
                    gui.showQuestionScreen(questionText, opts, seconds);
                }

            } else if (msg.startsWith("SCREEN:FINAL:")) {
                // Mensagem final da partida
                String finalText = msg.substring("SCREEN:FINAL:".length());
                gui.showFinalScreen(finalText);

            } else if (msg.startsWith("SCORE:")) {
                // Atualiza a pontuação/placar intermediário
                String scoreText = msg.substring("SCORE:".length());
                gui.showScoreboard(scoreText);

            } else if (msg.startsWith("TIME:")) {
                // Atualiza apenas o timer (caso implementes essa lógica separada)
                String timeValue = msg.substring("TIME:".length());
                try {
                    int seconds = Integer.parseInt(timeValue);
                    gui.updateTimer(seconds);
                } catch (NumberFormatException ignored) {}

            } else {
                // Mensagem não reconhecida (podes mostrar uma popup ou log)
                System.out.println("Mensagem desconhecida do servidor: " + msg);
            }
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

    private void createGUI() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            gui = new GUI();

            gui.setAnswerSender(selectedIndex -> {
                out.println("ANSWER:" + selectedIndex);
            });
            gui.setNextSender(() -> {
                out.println("NEXT");
            });
        });
    }

}

