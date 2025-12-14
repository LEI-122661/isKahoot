package isKahoot.Server;

import isKahoot.Game.GameState;
import isKahoot.Game.Question;
import isKahoot.Game.Team;
import java.util.*;


public class GameRoom {

    private String roomCode;
    private List<DealWithClient> players = new ArrayList<>();
    private Map<String, Team> teams = new HashMap<>();
    private GameState gameState;
    private boolean isGameRunning = false;
    private List<Question> questions = new ArrayList<>();

    // Configuração da sala
    private Integer numTeams;
    private Integer numPlayersPerTeam;
    private int maxPlayers;

    //NOVO: Controlo manual do servidor
    private boolean isReadyToStart = false;


    public GameRoom(String roomCode, List<Question> questions, int numTeams, int numPlayersPerTeam) {
        this.roomCode = roomCode;
        this.questions = questions;
        this.numTeams = numTeams;
        this.numPlayersPerTeam = numPlayersPerTeam;
        this.maxPlayers = numTeams * numPlayersPerTeam;
        this.isReadyToStart = false;  // Começa desautorizado
    }

    // Adiciona um jogador à sala, retorna false se a sala está cheia ou o jogo já começou.
    public synchronized boolean addPlayer(DealWithClient player) {
        if (isGameRunning) {
            return false;  // Jogo já começou
        }

        if (players.size() >= maxPlayers) {
            return false;  // Sala cheia
        }

        players.add(player);
        System.out.println("[ROOM " + roomCode + "] Jogador " + player.getUsername()
                + " entrou. (" + players.size() + "/" + maxPlayers + ")");

        return true;
    }

    //inicia jogo com autorizacao do server
    public synchronized void startGame() {
        if (isGameRunning) {
            System.out.println("[ROOM " + roomCode + "] Jogo já está em execução!");
            return;
        }

        if (!isReadyToStart) {
            System.out.println("[ROOM " + roomCode + "] Sala não está autorizada para começar!");
            System.out.println("[ROOM " + roomCode + "] Jogadores: " + players.size() + "/" + maxPlayers);
            return;
        }

        if (players.isEmpty()) {
            System.out.println("[ROOM " + roomCode + "] Nenhum jogador na sala!");
            return;
        }

        System.out.println("[ROOM " + roomCode + "] A iniciar jogo com " + players.size() + " jogadores.");

        // Criar equipas
        for (int i = 1; i <= numTeams; i++) {
            String teamId = "equipa" + i;
            teams.put(teamId, new Team(teamId, "Team " + i));
        }

        // Criar GameState
        this.gameState = new GameState(questions, teams);

        for (DealWithClient p : players) {
            p.setgameInfo(teams, gameState);
            p.assignToTeam();
        }

        // Lançar thread do jogo
        new GameHandler(players, gameState).start();
        isGameRunning = true;
    }


    public synchronized boolean canStartGame() {
        return !isGameRunning && players.size() == maxPlayers;
    }


    public synchronized void autorizeStart() {
        if (!isGameRunning && players.size() == maxPlayers) {
            isReadyToStart = true;
            System.out.println("[ROOM " + roomCode + "] Sala autorizada para começar!");
        } else if (players.size() < maxPlayers) {
            System.out.println("[ROOM " + roomCode + "] Nem todos os jogadores chegaram!");
            System.out.println("[ROOM " + roomCode + "] Presentes: " + players.size() + "/" + maxPlayers);
        }
    }


    public synchronized int getRemainingPlayers() {
        return maxPlayers - players.size();
    }


    public synchronized boolean isReady() {
        return isReadyToStart;
    }


    public synchronized String getStatus() {
        String status = isGameRunning ? "🎮 Em curso" : " Aguardando";
        return "[" + roomCode + "] " + status + " (" + players.size() + "/" + maxPlayers + " jogadores)";
    }

    // ========== Getters ==========

    public int getPlayerCount() {
        return players.size();
    }

    public List<DealWithClient> getPlayers() {
        return new ArrayList<>(players);
    }

    public boolean isGameRunning() {
        return isGameRunning;
    }

    public String getRoomCode() {
        return roomCode;
    }
}