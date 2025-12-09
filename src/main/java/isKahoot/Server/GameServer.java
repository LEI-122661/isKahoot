package isKahoot.Server;

import isKahoot.Game.GameState;
import isKahoot.Game.Question;
import isKahoot.Game.QuestionLoader;
import isKahoot.Game.Team;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Servidor principal do jogo IsKahoot.
 * Aceita conexões de clientes e coordena o fluxo do jogo.
 */
public class GameServer {

    public static final int PORT = 12025;
    public static final int EXPECTED_CLIENTS = 4; // espera 4 clientes

    private ServerSocket server;
    private final List<ConnectionHandler> clients = new ArrayList<>();
    private Map<String, Team> teams;
    private GameState gameState;

    private Map<String, GameRoom> activeRooms = new HashMap<>();
    private boolean aceptiongClients = false;
    private Thread acceptanceThread;


    public void createRoom(int numTeams, int numPlayersPerTeam) {
        String roomCode = generateRoomCode();

        String path = findQuizzesFile();
        if(path ==null){
            System.err.println("[SERVER] ERRO: Ficheiro quizzes.json não encontrado! Não foi possível criar a sala.");
            return;
        }

        List<Question> questions = QuestionLoader.loadFromJson(path);
        if (questions.isEmpty()) {
            System.out.println("[SERVER] Erro: Ficheiro sem perguntas.");
            return;
        }

        //cria sala
        GameRoom room = new GameRoom(roomCode, questions, numTeams, numPlayersPerTeam);
        synchronized (activeRooms){
            activeRooms.put(roomCode, room);
        }

        startAcceptingClients();

        System.out.println("[SERVER] Sala criada com sucesso! CÓDIGO: " + roomCode);
        System.out.println("[SERVER] (Partilha este código com os clientes)");

    }

    public void startGame(String roomCode){
        GameRoom room;
        synchronized (activeRooms){
            room = activeRooms.get(roomCode);
        }
        if(room ==null){
            System.out.println("[SERVER] Erro: Sala com código " +roomCode + " não existe.");
        } else {
            room.startGame();  //lanca gamehandler da slaa
        }
    }


    public String listRooms() {
        // Bloqueamos o mapa para ninguém o alterar enquanto lemos
        synchronized (activeRooms) {

            // Se o mapa estiver vazio, despacha logo
            if (activeRooms.isEmpty()) {
                return "Nenhuma sala ativa.";
            }

            // StringBuilder é como um saco onde vamos metendo texto
            StringBuilder sb = new StringBuilder();
            sb.append("--- Salas Ativas ---\n");

            // Percorrer todas as salas (Foreach)
            for (Map.Entry<String, GameRoom> entry : activeRooms.entrySet()) {

                String codigoSala = entry.getKey();
                GameRoom sala = entry.getValue();

                // Construção simples com "+" em vez de %s
                sb.append(" [Sala " + codigoSala + "]");
                sb.append(" - Jogadores: " + sala.getPlayerCount());

                // Transforma o boolean em texto "Sim" ou "Não"
                String estadoJogo = "";
                if (sala.isGameRunning()) {
                    estadoJogo = "Sim";
                } else {
                    estadoJogo = "Não";
                }
                sb.append(" - A decorrer: " + estadoJogo);
                sb.append("\n"); // Muda de linha

                // Lista os nomes dos jogadores desta sala
                for (ConnectionHandler jogador : sala.getPlayers()) {
                    sb.append("    -> " + jogador.getUsername() + "\n");
                }
            }

            // Devolve o texto
            return sb.toString();
        }
    }

    public GameRoom getRoom(String roomCode){
        synchronized (activeRooms){
            return activeRooms.get(roomCode);
        }
    }


    private String generateRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();

        do {
            // PASSO A: Limpar o quadro, codigo antigo existia, tenta de novo
            sb.setLength(0);

            // PASSO B: Construir um código de 4 letras
            for (int i = 0; i < 4; i++) {
                int index = rnd.nextInt(chars.length());
                char letraSorteada = chars.charAt(index);
                sb.append(letraSorteada);
            }

        } while (activeRooms.containsKey(sb.toString()));  //verifica se codigo ja existia, se existia volta ao loop
        return sb.toString();
    }


    /**
     * Inicia o servidor.

    public void runServer() {
        try {
            // 1. Carrega perguntas do ficheiro JSON
            System.out.println("[SERVER] Carregando perguntas...");

            // Tenta encontrar quizzes.json em vários locais
            String quizPath = findQuizzesFile();

            if (quizPath == null) {
                System.err.println("[SERVER] ERRO: Ficheiro quizzes.json não encontrado!");
                System.err.println("[SERVER] Procurou em:");
                System.err.println("  - isKahoot/resources/quizzes.json");
                System.err.println("  - isKahoot/target/classes/quizzes.json");
                System.err.println("  - src/main/resources/quizzes.json");
                System.err.println("  - quizzes.json (raiz)");
                return;
            }

            System.out.println("[SERVER] Ficheiro encontrado em: " + quizPath);
            List<Question> questions = QuestionLoader.loadFromJson(quizPath);

            System.out.println("[SERVER] Perguntas carregadas: " + questions.size());

            if (questions.isEmpty()) {
                System.err.println("[SERVER] ERRO: Nenhuma pergunta foi carregada!");
                return;
            }

            // 2. Cria as equipas
            System.out.println("[SERVER] Criando equipas...");
            teams = new HashMap<>();
            teams.put("team1", new Team("team1", "Team 1"));
            teams.put("team2", new Team("team2", "Team 2"));
            System.out.println("[SERVER] Equipas criadas: " + teams.keySet());

            // 3. Cria o GameState (estado do jogo)
            System.out.println("[SERVER] Inicializando GameState...");
            gameState = new GameState(questions, teams);

            // 4. Inicializa o servidor de sockets
            server = new ServerSocket(PORT);
            System.out.println("[SERVER] Servidor à escuta na porta " + PORT);
            System.out.println("[SERVER] À espera de " + EXPECTED_CLIENTS + " clientes...");

            // 5. Aceita conexões de clientes
            int connectedClients = 0;
            while (connectedClients < EXPECTED_CLIENTS) {
                Socket clientSocket = server.accept();
                System.out.println("[SERVER] Cliente #" + (connectedClients + 1) + " conectado!");

                // Cria thread para gerir este cliente
                ConnectionHandler handler = new ConnectionHandler(
                        clientSocket,
                        connectedClients + 1,
                        teams,           // passa referência das equipas
                        gameState        // passa referência do GameState
                );
                handler.start();   //inicia a thread, ConnectionHandler.run() corre em //
                clients.add(handler);
                connectedClients++;
            }

            System.out.println("[SERVER] Todos os " + EXPECTED_CLIENTS + " clientes conectados!");
            System.out.println("[SERVER] Estado das equipas:");
            for (Team team : teams.values()) {
                System.out.println("  - " + team);
            }

            // 6. Aguarda um pouco antes de iniciar o jogo
            Thread.sleep(2000);

            // 7. Inicia o GameHandler (ciclo do jogo), quando a sala enche
            System.out.println("[SERVER] Iniciando ciclo do jogo...");
            GameHandler gameHandler = new GameHandler(clients, gameState);
            gameHandler.start();  //Lança a thread do jogo, que vai controlar as rondas, enviar perguntas e contar o tempo

        } catch (IOException e) {
            System.err.println("[SERVER] Erro de I/O: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("[SERVER] Servidor interrompido: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("[SERVER] Erro geral: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeServer();
        }
    }  */

    /**
     * Procura o ficheiro quizzes.json em vários locais.
     */
    private static String findQuizzesFile() {
        String[] possiblePaths = {
                "isKahoot/resources/quizzes.json",
                "isKahoot/target/classes/quizzes.json", // ?? porque isto ??
                "src/main/resources/quizzes.json",
                "quizzes.json"
        };

        for (String path : possiblePaths) {
            Path p = Paths.get(path);
            if (Files.exists(p)) {
                return path;
            }
        }
        return null;
    }

    private void startAcceptingClients(){
        if(aceptiongClients){
            return;  //ja esta o jogo a correr
        }

        try {
            server = new ServerSocket(PORT);
            aceptiongClients =true;
            System.out.println("[SERVER] Socket aberto na porta " + PORT + ". À escuta...");

            //thread que aceita clientes para nao bloquear tui
            acceptanceThread = new Thread(() -> {
                int clienNumber = 0;
                while (aceptiongClients && !server.isClosed()) {
                    try{
                        Socket clientSocket = server.accept();
                        clienNumber++;
                        ConnectionHandler cliente = new ConnectionHandler(clientSocket,clienNumber,this,teams);
                        cliente.start();
                        System.out.println("[SERVER] Novo cliente conectado: #" + clienNumber);

                    } catch (IOException e){
                        if(aceptiongClients) {
                            System.err.println("[SERVER] Erro ao aceitar cliente: " + e.getMessage());
                        }
                    }
                }
            });
            acceptanceThread.start();
        } catch (IOException e){
            System.err.println("[SERVER] Erro ao iniciar socket do servidor: " + e.getMessage());
        }
    }

    /**
     * Fecha o servidor.
     */
    public void closeServer() {
        aceptiongClients =false;
        try {
            if (server != null && !server.isClosed()) {
                server.close();
                System.out.println("[SERVER] Servidor fechado.");
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Erro ao fechar servidor: " + e.getMessage());
        }
    }

    /**
     * Main - inicia o servidor.
     */
    public static void main(String[] args) {
        GameServer server = new GameServer();
        new TUI(server).run();  // Inicia a interface de texto
    }
}