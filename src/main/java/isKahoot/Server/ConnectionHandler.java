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
    private String requestedTeamId;
    private GameServer gameServer;

    private Map<String, Team> teams; // referência às equipas do jogo
    private GameState gameState;     // referência ao estado do jogo

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;               // nome do jogador (ex: "Client1")
    private String assignedTeamId;         // equipa atribuída (ex: "team1")

    /**
     * Construtor do ConnectionHandler.
     *
     * @param connection socket da conexão com o cliente
     * @param clientId identificador sequencial do cliente
     */
    public ConnectionHandler(Socket connection, int clientId, GameServer gameServer) {
        this.connection = connection;
        this.clientId = clientId;
        this.gameServer = gameServer;
        this.username = "Client" + clientId; // default
        this.assignedTeamId = null; // será atribuído após receber ClientInfo
    }

    public void setgameInfo(Map<String, Team> teams, GameState gameState) {
        this.teams=teams;
        this.gameState=gameState;
    }

    @Override
    public void run() {
        try {
            setStreams();

            ClientInfo infoClient = receiveClientInfo();      // Recebe e processa informações do cliente
            if(infoClient == null){
                return;
            }

            String code = infoClient.getGameCode();
            System.out.println("[HANDLER " + username + "] A tentar entrar na sala: " + code);

            GameRoom room = gameServer.getRoom(code);
            if(room != null){
                if(room.addPlayer(this)){
                    System.out.println("[HANDLER] " + username + " entrou na sala " + code);
                    sendMessage("SCREEN:LOBBY");

                    processConnection();
                } else {
                    sendMessage("ERROR:Jogo já começou");
                }
            } else{
                sendMessage("ERROR:Código de sala inválido");
            }
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
    private ClientInfo receiveClientInfo() throws IOException {
        try {
            Object obj = in.readObject();
            if (obj instanceof ClientInfo) {
                ClientInfo info = (ClientInfo) obj;
                this.username = info.getUsername();
                this.requestedTeamId = info.getTeamId();
                System.out.println("[HANDLER] Recebido ClientInfo: " + info);
                System.out.println("[HANDLER] Username atribuído: " + username);
                return info;
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[HANDLER] Erro ao deserializar ClientInfo: " + e.getMessage());
        }
        return null;
    }



    public void assignToTeam() {
        if(teams == null) return;
        synchronized (teams) {
            // REGRA 1: teamId DEVE ser não-null
            if (requestedTeamId == null || requestedTeamId.equals("null")) {
                System.out.println("[HANDLER] " + username + " rejeitado: teamId é obrigatório!");
                sendMessage("ERROR:teamId obrigatório para entrar");
                return;
            }

            // REGRA 2: Equipa tem de existir
            if (!teams.containsKey(requestedTeamId)) {
                System.out.println("[HANDLER] " + username + " rejeitado: equipa '" + requestedTeamId + "' não existe!");
                sendMessage("ERROR:Equipa inválida: " + requestedTeamId);
                return;
            }

            // REGRA 3: Equipa tem de ter espaço (máximo 2)
            Team team = teams.get(requestedTeamId);
            if (team.getPlayerCount() >= 2) {
                System.out.println("[HANDLER] " + username + " rejeitado: equipa '" + requestedTeamId + "' está cheia (2/2)!");
                sendMessage("ERROR:Equipa cheia: " + requestedTeamId);
                return;
            }

            // TUDO OK: Adiciona à equipa
            if (team.addPlayer(username)) {
                this.assignedTeamId = requestedTeamId;
                System.out.println("[HANDLER] " + username + " atribuído à equipa: " + requestedTeamId);
                sendMessage("TEAM_ASSIGNED:" + requestedTeamId);
            }
        }
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
            if(gameState!=null){
                String answerStr = msg.substring("ANSWER:".length());
                try {
                    int optionIndex = Integer.parseInt(answerStr);
                    System.out.println("[HANDLER " + username + "] Resposta recebida: opção " + optionIndex);

                    // Registar resposta no GameState
                    boolean accepted = gameState.recieveAnswer(username, optionIndex);
                    if (accepted) {
                        System.out.println("[HANDLER " + username + "] Resposta aceite!");
                    } else {
                        System.out.println("[HANDLER " + username + "] Resposta rejeitada (já respondeu ou ronda terminou)");
                    }
                } catch (NumberFormatException e) {
                    System.err.println("[HANDLER " + username + "] Opção inválida: " + answerStr);
                }
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
    public void closeConnection() {
        try {
            // Remove o jogador da equipa
            if (assignedTeamId != null && teams!=null && teams.containsKey(assignedTeamId)) {
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


    public String getUsername() {
        return username;
    }



    public String getAssignedTeamId() {
        return assignedTeamId;
    }
}