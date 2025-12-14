package isKahoot.Game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa uma equipa no jogo IsKahoot.
 * Cada equipa tem no máximo 2 jogadores.
 * Mantém pontuação total e por ronda.
 */
public class Team implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int MAX_PLAYERS = 2;

    private final String teamId;
    private final String teamName;
    private int totalScore;
    private int roundScore; // pontos ganhos na ronda atual
    private final List<String> players;
    private boolean ready; // indica se a equipa está pronta para jogar



    public Team(String teamId, String teamName) {
        this.teamId = Objects.requireNonNull(teamId, "teamId não pode ser null");
        this.teamName = Objects.requireNonNull(teamName, "teamName não pode ser null");
        this.totalScore = 0;
        this.roundScore = 0;
        this.players = new ArrayList<>();
        this.ready = false;
    }


    public synchronized boolean addPlayer(String playerName) {
        Objects.requireNonNull(playerName, "playerName não pode ser null");

        if (players.size() >= MAX_PLAYERS) {
            System.out.println("[TEAM] Equipa " + teamName + " já tem " + MAX_PLAYERS + " jogadores.");
            return false;
        }

        if (players.contains(playerName)) {
            System.out.println("[TEAM] Jogador " + playerName + " já está nesta equipa.");
            return false;
        }

        players.add(playerName);
        System.out.println("[TEAM] Jogador " + playerName + " adicionado à equipa " + teamName + ".");
        return true;
    }



    // true or false conforme falha ou nao
    public synchronized boolean removePlayer(String playerName) {
        boolean removed = players.remove(playerName);
        if (removed) {
            System.out.println("[TEAM] Jogador " + playerName + " removido da equipa " + teamName + ".");
        }
        return removed;
    }

    public synchronized boolean hasPlayer(String playerName) {
        return players.contains(playerName);
    }




    public synchronized List<String> getPlayers() {
        return new ArrayList<>(players);

    }


    public synchronized int getPlayerCount() {
        return players.size();
    }




    public synchronized int getTotalScore() {
        return totalScore;
    }




    public synchronized int getRoundScore() {
        return roundScore;
    }




    public synchronized void addPoints(int points) {
        if (points <0) {
            System.out.println("[TEAM] Pontos não podem ser negativos.");
            return;
        }
        this.totalScore += points;
        this.roundScore += points;
        System.out.println("[TEAM] Equipa " + teamName + " ganhou " + points + " pontos. Total: " + totalScore);
    }


    public synchronized void setTotalScore(int score) {
        if (score < 0) {
            System.out.println("[TEAM] Pontos não podem ser negativos.");
            return;
        }
        this.totalScore = score;
    }




    public synchronized void resetRoundScore() {
        this.roundScore = 0;
    }





    public void setReady(boolean ready) {
        this.ready = ready;
    }




    public boolean isReady() {
        return ready;
    }




    public boolean isFull() {
        return players.size() == MAX_PLAYERS;
    }




    public String getTeamId() {
        return teamId;
    }




    public String getTeamName() {
        return teamName;
    }




    @Override
    public synchronized String toString() {
        return teamName + " (" + players.size() + "/" + MAX_PLAYERS + " players) - Total Score: " + totalScore + " | Round: " + roundScore;
    }




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return teamId.equals(team.teamId);
    }



    @Override
    public int hashCode() {
        return Objects.hash(teamId);
    }
}