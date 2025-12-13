package isKahoot.Server;

import isKahoot.Game.GameState;
import isKahoot.Game.Question;
import isKahoot.Game.Team;
import java.util.*;

/**
 * GameRoom representa uma sala de jogo IsKahoot.
 * Gestiona jogadores, equipas, e o ciclo do jogo.
 */
public class GameRoom {

    private String roomCode;
    private List<ConnectionHandler> players = new ArrayList<>();
    private Map<String, Team> teams = new HashMap<>();
    private GameState gameState;
    private boolean isGameRunning = false;
    private List<Question> questions = new ArrayList<>();

    // Configuração da sala
    private Integer numTeams;
    private Integer numPlayersPerTeam;
    private int maxPlayers;

    // ⭐ NOVO: Controlo manual do servidor
    private boolean isReadyToStart = false;

    /**
     * Construtor do GameRoom.
     */
    public GameRoom(String roomCode, List<Question> questions, int numTeams, int numPlayersPerTeam) {
        this.roomCode = roomCode;
        this.questions = questions;
        this.numTeams = numTeams;
        this.numPlayersPerTeam = numPlayersPerTeam;
        this.maxPlayers = numTeams * numPlayersPerTeam;
        this.isReadyToStart = false;  // Começa desautorizado
    }

    /**
     * Adiciona um jogador à sala.
     * Retorna false se a sala está cheia ou o jogo já começou.
     */
    public synchronized boolean addPlayer(ConnectionHandler player) {
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

    /**
     * Inicia o jogo quando autorizado pelo servidor.
     * NÃO começa automaticamente!
     */
    public synchronized void startGame() {
        if (isGameRunning) {
            System.out.println("[ROOM " + roomCode + "] ❌ Jogo já está em execução!");
            return;
        }

        if (!isReadyToStart) {
            System.out.println("[ROOM " + roomCode + "] ❌ Sala não está autorizada para começar!");
            System.out.println("[ROOM " + roomCode + "] Jogadores: " + players.size() + "/" + maxPlayers);
            return;
        }

        if (players.isEmpty()) {
            System.out.println("[ROOM " + roomCode + "] ❌ Nenhum jogador na sala!");
            return;
        }

        System.out.println("[ROOM " + roomCode + "] 🎮 A iniciar jogo com " + players.size() + " jogadores.");

        // Criar equipas
        for (int i = 1; i <= numTeams; i++) {
            String teamId = "equipa" + i;
            teams.put(teamId, new Team(teamId, "Team " + i));
        }

        // Criar GameState
        this.gameState = new GameState(questions, teams);

        for (ConnectionHandler p : players) {
            p.setgameInfo(teams, gameState);
            p.assignToTeam();
        }

        // Lançar thread do jogo
        new GameHandler(players, gameState).start();
        isGameRunning = true;
    }

    /**
     * ⭐ NOVO: Verifica se pode começar (todos conectados e autorizados).
     */
    public synchronized boolean canStartGame() {
        return !isGameRunning && players.size() == maxPlayers;
    }

    /**
     * ⭐ NOVO: Autoriza o servidor para começar o jogo.
     */
    public synchronized void authorizeStart() {
        if (!isGameRunning && players.size() == maxPlayers) {
            isReadyToStart = true;
            System.out.println("[ROOM " + roomCode + "] ✅ Sala autorizada para começar!");
        } else if (players.size() < maxPlayers) {
            System.out.println("[ROOM " + roomCode + "] ❌ Nem todos os jogadores chegaram!");
            System.out.println("[ROOM " + roomCode + "] Presentes: " + players.size() + "/" + maxPlayers);
        }
    }

    /**
     * ⭐ NOVO: Retorna quantos jogadores faltam.
     */
    public synchronized int getRemainingPlayers() {
        return maxPlayers - players.size();
    }

    /**
     * ⭐ NOVO: Verifica se sala está autorizada.
     */
    public synchronized boolean isReady() {
        return isReadyToStart;
    }

    /**
     * ⭐ NOVO: Retorna o estado da sala.
     */
    public synchronized String getStatus() {
        String status = isGameRunning ? "🎮 Em curso" : "⏸️ Aguardando";
        return "[" + roomCode + "] " + status + " (" + players.size() + "/" + maxPlayers + " jogadores)";
    }

    // ========== Getters ==========

    public int getPlayerCount() {
        return players.size();
    }

    public List<ConnectionHandler> getPlayers() {
        return new ArrayList<>(players);
    }

    public boolean isGameRunning() {
        return isGameRunning;
    }

    public String getRoomCode() {
        return roomCode;
    }
}