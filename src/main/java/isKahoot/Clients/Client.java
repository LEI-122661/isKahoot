package isKahoot.Clients;

import isKahoot.Game.GUI;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

    private Socket connection;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private GUI gui;
    private String username;
    private String teamID;

    //Inicia o client com um username especifico, temos classes dos nossos clientes
    public void runClient(String username, String teamID) {
        this.username = username;
        this.teamID = teamID;

        try {
            connectToServer();
            setStreams();
            sendClientInfo(); // Envia informações ao server
            createGUI();
            processConnection();
        } catch (IOException e) {
            System.err.println("[CLIENT " + username + "] Erro: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void connectToServer() throws IOException {
        InetAddress endereco = InetAddress.getByName("localhost"); // localhost
        System.out.println("[CLIENT " + username + "] Conectando a: " + endereco + ":12025");
        connection = new Socket(endereco, 12025); // porta do GameServer
        System.out.println("[CLIENT " + username + "] Conectado com sucesso!");
    }


     //inicializa os streams de comunicação, cria ObjectOutputStream ANTES de ObjectInputStream para evitar deadlock!!!

    private void setStreams() throws IOException {
        out = new ObjectOutputStream(connection.getOutputStream());
        out.flush(); //  !! para enviar dados, sem isto servidor fica a espera do cabecalho ineterno de outputstream e nunca cria o InputStream
        in = new ObjectInputStream(connection.getInputStream());
        System.out.println("[CLIENT " + username + "] Streams inicializados.");
    }


    //envia informacoess do cliente ao servidor
    private void sendClientInfo() throws IOException {

        // NOVO: Perguntar o código da sala ao user
        java.util.Scanner scanner =new java.util.Scanner(System.in);
        System.out.print(">> Insere o CÓDIGO DA SALA (dado pelo servidor): ");
        String code = scanner.nextLine().trim().toUpperCase();

        ClientInfo info = new ClientInfo(username, code, teamID);
        // nome: "ClientX" nome do user,teamID: "EquipaX" nome da equipa

        out.writeObject(info);  //serializa e envia o objeto ClientInfo por connction
        out.flush();            //enviar informacoes
        System.out.println("[CLIENT " + username + "] Informações enviadas: " + info);
    }

     //Processa mensagens do servidor, bloqueia a espera de msgs até o servidor encerrar a conexão
    private void processConnection() {
        try {
            while (true) {
                try {
                    Object msg = in.readObject();

                    if (msg instanceof String) {   //nosso protocolo usa apenas strings "SCREEN:LOBBY", "SCREEN:QUESTION:...", etc.
                        String strMsg = (String) msg;
                        System.out.println("[CLIENT " + username + "] Recebido: " + strMsg);

                        // Processa mensagens de tela
                        javax.swing.SwingUtilities.invokeLater(() -> handleServerMessage(strMsg));
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("[CLIENT " + username + "] Tipo de mensagem desconhecida: " + e.getMessage());
                }
            }
        } catch (EOFException e) {
            fecharJanelaClientes();
            System.out.println("[CLIENT " + username + "] Servidor fechou a conexão.");

        } catch (IOException e) {
            System.err.println("[CLIENT " + username + "] Erro na comunicação: " + e.getMessage());
        }
    }

    //interpreta mensagens recebidas do servidor e atualiza a GUI
    private void handleServerMessage(String msg) {
        if (msg.startsWith("SCREEN:LOBBY")) {
            System.out.println("[CLIENT " + username + "] Mostrando LOBBY");
            gui.showLobby();

        } else if (msg.startsWith("SCREEN:QUESTION:")) {
            String payload = msg.substring("SCREEN:QUESTION:".length());
            String[] parts = payload.split("\\|");  // usamos // para dizer que queremos o caractere | mesmo, pois | é especial em regex
            //Se msg era "SCREEN:QUESTION:O que é?|A|B|C|D|30", o payload fica "O que é?|A|B|C|D|30".

            if (parts.length >= 6) {  // pergunta + 4 opções + tempo
                String questionText = parts[0];
                String[] opts = new String[]{parts[1], parts[2], parts[3], parts[4]};
                int seconds;
                try {
                    seconds = Integer.parseInt(parts[5]);
                } catch (NumberFormatException e) {
                    seconds = 30;
                }

                System.out.println("[CLIENT " + username + "] Mostrando PERGUNTA");
                gui.showQuestionScreen(questionText, opts, seconds);
            }

        } else if (msg.startsWith("SCREEN:FINAL:")) {
            String finalText = msg.substring("SCREEN:FINAL:".length());
            System.out.println("[CLIENT " + username + "] Mostrando RESULTADO FINAL");
            gui.showFinalScreen(finalText);

        } else if (msg.startsWith("SCORE:")) {
            String scoreText = msg.substring("SCORE:".length());
            System.out.println("[CLIENT " + username + "] Atualizando pontuação");
            gui.showScoreboard(scoreText);

        } else if (msg.startsWith("TIME:")) {
            String timeValue = msg.substring("TIME:".length());
            try {
                int seconds = Integer.parseInt(timeValue);
                gui.updateTimer(seconds);
            } catch (NumberFormatException ignored) {
            }

        } else {
            System.out.println("[CLIENT " + username + "] Mensagem desconhecida: " + msg);
        }
    }

    // cria GUI!!
    private void createGUI() {
        javax.swing.SwingUtilities.invokeLater(() -> {  //Coloca a criação da janela na fila de espera da Thread do Swing,executa quando for seguro
            gui = new GUI();
            gui.setTitle("IsKahoot - " + username); // title com nome do user, janela personalizada

            // enviar resposta
            gui.setAnswerSender(selectedIndex -> {
                try {
                    out.writeObject("ANSWER:" + selectedIndex);
                    out.flush();
                } catch (IOException e) {
                    System.err.println("[CLIENT " + username + "] Erro ao enviar resposta: " + e.getMessage());
                }
            });

        });
    }

    // fecha connection
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            System.out.println("[CLIENT " + username + "] Conexão fechada.");
        } catch (IOException e) {
            System.err.println("[CLIENT " + username + "] Erro ao fechar conexão: " + e.getMessage());
        }
    }


    private void fecharJanelaClientes() {
        System.out.println("[CLIENT] Conexão encerrada.");

        javax.swing.JOptionPane.showMessageDialog(null,
                "O Jogo Terminou!\nO servidor encerrou a conexão.",
                "Fim de Jogo",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);

        System.exit(0);
    }
}