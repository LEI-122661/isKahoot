package isKahoot.Server;

import isKahoot.Clients.ClientInfo;
import isKahoot.Game.GameState;
import isKahoot.Game.Team;

import java.io.*;
import java.net.Socket;
import java.util.Map;

/**
 * Thread que gere a comunicação com um cliente específico.
 * Recebe informações do cliente, atribui a uma equipa, e processa mensagens durante o jogo.
 */
public class ConnectionHandler extends Thread {

    private final Socket connection;
    private final int clientId;
    private final Map<String, Team> teams; // referência às equipas do jogo
    private final GameState gameState;     // referência ao estado do jogo

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;               // nome do jogador (ex: "Client1")
    private String assignedTeamId;         // equipa atribuída (ex: "team1")

    /**
     * Construtor do ConnectionHandler.
     *
     * @param connection socket da conexão com o cliente
     * @param clientId identificador sequencial do cliente
     * @param teams mapa de equipas (para atribuição automática)
     * @param gameState estado do jogo compartilhado
     */
    public ConnectionHandler(Socket connection, int clientId,
                             Map<String, Team> teams, GameState gameState) {
        this.connection = connection;
        this.clientId = clientId;
        this.teams = teams;
        this.gameState = gameState;
        this.username = "Client" + clientId; // default
        this.assignedTeamId = null; // será atribuído após receber ClientInfo
    }

    @Override
    public void run() {
        try {
            setStreams();
            receiveClientInfo();      // Recebe e processa informações do cliente
            assignToTeam();           // Atribui automaticamente a uma equipa disponível
            processConnection();      // Processa mensagens durante o jogo
        } catch (IOException e) {
            System.err.println("[HANDLER " + username + "] Erro: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    /**
     * Inicializa os streams de comunicação.
     * CRÍTICO: Criar ObjectOutputStream ANTES de ObjectInputStream!
     */
    private void setStreams() throws IOException {
        out = new ObjectOutputStream(connection.getOutputStream());
        out.flush(); // CRÍTICO!
        in = new ObjectInputStream(connection.getInputStream());
        System.out.println("[HANDLER] Streams inicializados para cliente #" + clientId);
    }

    /**
     * Recebe as informações do cliente (ClientInfo).
     * O cliente envia isto logo após conectar.
     */
    private void receiveClientInfo() throws IOException {
        try {
            Object obj = in.readObject();
            if (obj instanceof ClientInfo) {
                ClientInfo info = (ClientInfo) obj;
                this.username = info.getUsername();
                System.out.println("[HANDLER] Recebido ClientInfo: " + info);
                System.out.println("[HANDLER] Username atribuído: " + username);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[HANDLER] Erro ao deserializar ClientInfo: " + e.getMessage());
        }
    }

    /**
     * Atribui automaticamente o cliente a uma equipa disponível.
     * Procura pela primeira equipa que tem menos de 2 jogadores.
     */
    //temos 4 threads a correr, cada um com o seu connection handler, a tentar acerder a equipas ao mesmo tempo
    private void assignToTeam() {
        synchronized (teams) {
            for (String teamId : teams.keySet()) {
                Team team = teams.get(teamId);
                if (team.getPlayerCount() < 2) {
                    // Encontrou uma equipa com espaço
                    if (team.addPlayer(username)) {
                        this.assignedTeamId = teamId;
                        System.out.println("[HANDLER] " + username + " atribuído à equipa: " + teamId);
                        sendMessage("TEAM_ASSIGNED:" + teamId); // Avisa o cliente
                        return;
                    }
                }
            }
        }

        // Se chegou aqui, nenhuma equipa tem espaço
        System.err.println("[HANDLER] ERRO: Nenhuma equipa disponível para " + username);
        sendMessage("ERROR:Nenhuma equipa disponível");
    }

    /**
     * Processa mensagens do cliente durante o jogo.
     */
    private void processConnection() {
        try {
            while (!Thread.interrupted()) {
                try {
                    Object msg = in.readObject();

                    if (msg instanceof String) {
                        String strMsg = (String) msg;
                        System.out.println("[HANDLER " + username + "] Recebido: " + strMsg);
                        handleClientMessage(strMsg);
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("[HANDLER " + username + "] Tipo desconhecido: " + e.getMessage());
                }
            }
        } catch (EOFException e) {
            System.out.println("[HANDLER " + username + "] Cliente desconectou normalmente.");
        } catch (IOException e) {
            System.err.println("[HANDLER " + username + "] Erro na comunicação: " + e.getMessage());
        }
    }

    /**
     * Trata mensagens recebidas do cliente.
     * Exemplos: "ANSWER:2", "NEXT", etc.
     */
    private void handleClientMessage(String msg) {
        if (msg.startsWith("ANSWER:")) {
            String answerStr = msg.substring("ANSWER:".length());
            try {
                int optionIndex = Integer.parseInt(answerStr);
                System.out.println("[HANDLER " + username + "] Resposta recebida: opção " + optionIndex);

                // Registar resposta no GameState
                boolean accepted = gameState.receiveAnswer(username, optionIndex);
                if (accepted) {
                    System.out.println("[HANDLER " + username + "] Resposta aceite!");
                } else {
                    System.out.println("[HANDLER " + username + "] Resposta rejeitada (já respondeu ou ronda terminou)");
                }
            } catch (NumberFormatException e) {
                System.err.println("[HANDLER " + username + "] Opção inválida: " + answerStr);
            }

        } else if (msg.equals("NEXT")) {
            System.out.println("[HANDLER " + username + "] Pronto para próxima pergunta");

        } else if (msg.equals("FIM")) {
            System.out.println("[HANDLER " + username + "] Cliente pediu encerramento");
            Thread.currentThread().interrupt();

        } else {
            System.out.println("[HANDLER " + username + "] Mensagem desconhecida: " + msg);
        }
    }

    /**
     * Envia uma mensagem ao cliente.
     * Thread-safe.
     */
    public synchronized void sendMessage(String msg) {
        try {
            out.writeObject(msg);
            out.flush();
            System.out.println("[HANDLER " + username + "] Enviado: " + msg);
        } catch (IOException e) {
            System.err.println("[HANDLER " + username + "] Erro ao enviar mensagem: " + e.getMessage());
        }
    }

    /**
     * Fecha a conexão com o cliente.
     */
    private void closeConnection() {
        try {
            // Remove o jogador da equipa
            if (assignedTeamId != null && teams.containsKey(assignedTeamId)) {
                teams.get(assignedTeamId).removePlayer(username);
            }

            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            System.out.println("[HANDLER " + username + "] Conexão fechada.");
        } catch (IOException e) {
            System.err.println("[HANDLER " + username + "] Erro ao fechar: " + e.getMessage());
        }
    }

    /**
     * Retorna o nome do jogador.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Retorna a equipa atribuída.
     */
    public String getAssignedTeamId() {
        return assignedTeamId;
    }
}