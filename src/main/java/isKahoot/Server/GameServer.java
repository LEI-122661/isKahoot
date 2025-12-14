package isKahoot.Server;

import isKahoot.Game.Question;
import isKahoot.Game.QuestionLoader;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

//Servidor principal do jogo IsKahoot, sceita conexões de clientes e coordena o fluxo do jogo

public class GameServer {

    public static final int PORT = 12025;
    public static final int EXPECTED_CLIENTS = 4;

    private ServerSocket server;
    private Map<String, GameRoom> activeRooms = new HashMap<>();
    private boolean acceptingClients = false;
    private Thread acceptanceThread;
    private int clientCounter = 0;  //ADICIONADO Contador para IDs únicos

    private final List<DealWithClient> allClients = new ArrayList<>();


    public void createRoom(int numTeams, int numPlayersPerTeam) {
        String roomCode = generateRoomCode();
        String path = findQuizzesFile();

        if (path == null) {
            System.err.println("[SERVER] ERRO: Ficheiro quizzes.json não encontrado :/ ");
            return;
        }

        List<Question> questions = QuestionLoader.loadFromJson(path);
        if (questions.isEmpty()) {
            System.out.println("[SERVER] Erro: Ficheiro sem perguntas.");
            return;
        }

        // Cria sala
        GameRoom room = new GameRoom(roomCode, questions, numTeams, numPlayersPerTeam);
        synchronized (activeRooms) {
            activeRooms.put(roomCode, room);
        }

        startAcceptingClients();
        System.out.println("[SERVER] ✅ Sala criada com sucesso!");
        System.out.println("[SERVER] 📌 CÓDIGO: " + roomCode);
        System.out.println("[SERVER] 👥 Esperando " + (numTeams * numPlayersPerTeam) + " jogadores");
        System.out.println("[SERVER] 📝 Tipo: " + numTeams + " equipas, " + numPlayersPerTeam + " jogadores por equipa");
    }

    /**
     * MODIFICADO: Inicia o jogo com verificações.
     */
    public void startGame(String roomCode) {
        GameRoom room;
        synchronized (activeRooms) {
            room = activeRooms.get(roomCode);
        }

        if (room == null) {
            System.out.println("[SERVER]  Erro: Sala com código " + roomCode + " não existe.");
            return;
        }

        // Verifica se pode começar
        if (!room.canStartGame()) {
            System.out.println("[SERVER] Erro: Sala não tem todos os jogadores conectados!");
            System.out.println("[SERVER] Esperando " + room.getRemainingPlayers() + " jogador(es)");
            return;
        }
        // Autoriza e inicia
        room.autorizeStart();
        room.startGame();
        System.out.println("[SERVER] ✅ 🎮 Jogo iniciado na sala " + roomCode);
    }

    //alteracao, para ficar mais bonito :)
    public String listRooms() {
        synchronized (activeRooms) {
            if (activeRooms.isEmpty()) {
                return " Nenhuma sala ativa.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("\n--- SALAS ATIVAS -------------------------\n");

            for (Map.Entry<String, GameRoom> entry : activeRooms.entrySet()) {
                GameRoom sala = entry.getValue();
                sb.append(sala.getStatus()).append("\n");

                List<DealWithClient> players = sala.getPlayers();
                if (players.isEmpty()) {
                    sb.append("   (nenhum jogador)\n");
                } else {
                    for (DealWithClient jogador : players) {
                        sb.append("   -> ").append(jogador.getUsername()).append("\n");
                    }
                }
                sb.append("\n");
            }

            return sb.toString();
        }
    }

    //Obtem uma sala pelo room code
    public GameRoom getRoom(String roomCode) {
        synchronized (activeRooms) {
            return activeRooms.get(roomCode);
        }
    }


    private String generateRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();

        do {
            sb.setLength(0);
            for (int i = 0; i < 4; i++) {
                int index = rnd.nextInt(chars.length());
                char letraSorteada = chars.charAt(index);
                sb.append(letraSorteada);
            }
        } while (activeRooms.containsKey(sb.toString()));

        return sb.toString();
    }

    // comecar servidor
    public void runServer() {
        try {
            System.out.println("[SERVER] Carregando perguntas...");

            String quizPath = findQuizzesFile();
            if (quizPath == null) {
                System.err.println("[SERVER] ❌ ERRO: Ficheiro quizzes.json não encontrado!");
                System.err.println("[SERVER] Procurou em:");
                System.err.println("  - isKahoot/resources/quizzes.json");
                System.err.println("  - isKahoot/target/classes/quizzes.json");
                System.err.println("  - src/main/resources/quizzes.json");
                System.err.println("  - quizzes.json (raiz)");
                return;
            }

            System.out.println("[SERVER] ✅ Ficheiro encontrado em: " + quizPath);

            // Cria server socket
            server = new ServerSocket(PORT);
            System.out.println("[SERVER] 🚀 Servidor IsKahoot iniciado na porta " + PORT);
            System.out.println("[SERVER] 👂 À espera de conexões...\n");

            // Inicia TUI
            TUI tui = new TUI(this);
            tui.start();

        } catch (IOException e) {
            System.err.println("[SERVER] ❌ Erro ao iniciar servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void startAcceptingClients() {
        if (acceptingClients) return;

        acceptingClients = true;
        acceptanceThread = new Thread(() -> {  //server.accept e metoto bloq, sem esta thread
                                               //o resto da tui nao funcionava, comandos list, start...
            try {
                while (acceptingClients) {

                    //
                    Socket clientSocket = server.accept();

                    //CORRIGIDO: Passar 3 parametros
                    //cria nova thraed respondavel por gerir comunicacao com cliente
                    DealWithClient handler = new DealWithClient(clientSocket, ++clientCounter, this);
                    synchronized (allClients){
                        allClients.add(handler);
                    }
                    new Thread(handler).start();
                }
            } catch (IOException e) {
                if (acceptingClients) {
                    System.err.println("[SERVER] Erro ao aceitar cliente: " + e.getMessage());
                }
            }
        });
        acceptanceThread.start();
    }

    //Encontra o ficheiro quizzes.json em vários locais.
    private String findQuizzesFile() {
        String[] possiblePaths = {
                "isKahoot/resources/quizzes.json",
                "isKahoot/target/classes/quizzes.json",
                "src/main/resources/quizzes.json",
                "quizzes.json"
        };

        for (String path : possiblePaths) {
            if (Files.exists(Paths.get(path))) {
                return path;
            }
        }
        return null;
    }




    public void closeServer() {
        acceptingClients = false;
        try {
            if (server != null) {
                server.close();
            }
            // faltava desconectar clinetes!!
            synchronized (allClients) {
                for (DealWithClient client : allClients) {
                    client.closeConnection();
                }
                allClients.clear();
            }
            System.out.println("[SERVER] Servidor encerrado e todos os clientes desconetados.");
            System.exit(0);
        } catch (IOException e) {
            System.err.println("[SERVER] Erro ao encerrar servidor: " + e.getMessage());
        }
    }


    // MAIN!!
    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.runServer();
    }
}