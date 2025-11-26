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

    /**
     * Inicia o servidor.
     */
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
                handler.start();
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

            // 7. Inicia o GameHandler (ciclo do jogo)
            System.out.println("[SERVER] Iniciando ciclo do jogo...");
            GameHandler gameHandler = new GameHandler(clients, gameState);
            gameHandler.start();

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
    }

    /**
     * Procura o ficheiro quizzes.json em vários locais.
     */
    private static String findQuizzesFile() {
        String[] possiblePaths = {
                "isKahoot/resources/quizzes.json",
                "isKahoot/target/classes/quizzes.json",
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

    /**
     * Fecha o servidor.
     */
    private void closeServer() {
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
        server.runServer();
    }
}