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

    /**
     * Construtor da equipa.
     * @param teamId identificador único da equipa (ex: "Team1", "Team2")
     * @param teamName nome da equipa (ex: "Python Squad", "Java Masters")
     */
    public Team(String teamId, String teamName) {
        this.teamId = Objects.requireNonNull(teamId, "teamId não pode ser null");
        this.teamName = Objects.requireNonNull(teamName, "teamName não pode ser null");
        this.totalScore = 0;
        this.roundScore = 0;
        this.players = new ArrayList<>();
        this.ready = false;
    }

    /**
     * Adiciona um jogador à equipa.
     * Máximo 2 jogadores por equipa.
     *
     * @param playerName nome do jogador
     * @return true se foi adicionado com sucesso, false caso contrário
     */
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

    /**
     * Remove um jogador da equipa.
     *
     * @param playerName nome do jogador
     * @return true se foi removido com sucesso
     */
    public synchronized boolean removePlayer(String playerName) {
        boolean removed = players.remove(playerName);
        if (removed) {
            System.out.println("[TEAM] Jogador " + playerName + " removido da equipa " + teamName + ".");
        }
        return removed;
    }

    /**
     * Verifica se um jogador pertence a esta equipa.
     *
     * @param playerName nome do jogador
     * @return true se o jogador está na equipa
     */
    public synchronized boolean hasPlayer(String playerName) {
        return players.contains(playerName);
    }

    /**
     * Retorna a lista de jogadores (cópia imutável).
     *
     * @return lista de nomes dos jogadores
     */
    public synchronized List<String> getPlayers() {
        return Collections.unmodifiableList(new ArrayList<>(players));
    }

    /**
     * Retorna o número de jogadores na equipa.
     *
     * @return número de jogadores
     */
    public synchronized int getPlayerCount() {
        return players.size();
    }

    /**
     * Retorna a pontuação total da equipa.
     *
     * @return pontuação acumulada
     */
    public synchronized int getTotalScore() {
        return totalScore;
    }

    /**
     * Retorna a pontuação da ronda atual.
     *
     * @return pontos ganhos nesta ronda
     */
    public synchronized int getRoundScore() {
        return roundScore;
    }

    /**
     * Atualiza a pontuação total da equipa.
     * Adiciona pontos ao total e à ronda.
     *
     * @param points pontos a adicionar
     */
    public synchronized void addPoints(int points) {
        if (points < 0) {
            System.out.println("[TEAM] Pontos não podem ser negativos.");
            return;
        }
        this.totalScore += points;
        this.roundScore += points;
        System.out.println("[TEAM] Equipa " + teamName + " ganhou " + points + " pontos. Total: " + totalScore);
    }

    /**
     * Define a pontuação total diretamente.
     * Útil para resets ou correções.
     *
     * @param score nova pontuação total
     */
    public synchronized void setTotalScore(int score) {
        if (score < 0) {
            System.out.println("[TEAM] Pontos não podem ser negativos.");
            return;
        }
        this.totalScore = score;
    }

    /**
     * Reseta a pontuação da ronda para 0.
     * Chamada no início de cada nova ronda.
     */
    public synchronized void resetRoundScore() {
        this.roundScore = 0;
    }

    /**
     * Marca a equipa como pronta para jogar.
     */
    public synchronized void setReady(boolean ready) {
        this.ready = ready;
    }

    /**
     * Verifica se a equipa está pronta.
     *
     * @return true se pronta
     */
    public synchronized boolean isReady() {
        return ready;
    }

    /**
     * Verifica se a equipa está completa (todos 2 jogadores presentes).
     *
     * @return true se a equipa tem 2 jogadores
     */
    public synchronized boolean isFull() {
        return players.size() == MAX_PLAYERS;
    }

    /**
     * Retorna o identificador da equipa.
     *
     * @return teamId
     */
    public String getTeamId() {
        return teamId;
    }

    /**
     * Retorna o nome da equipa.
     *
     * @return teamName
     */
    public String getTeamName() {
        return teamName;
    }

    /**
     * Retorna representação em String da equipa.
     *
     * @return formato: "TeamName (2/2 players) - Score: 150"
     */
    @Override
    public synchronized String toString() {
        return teamName + " (" + players.size() + "/" + MAX_PLAYERS + " players) - Total Score: " + totalScore + " | Round: " + roundScore;
    }

    /**
     * Compara duas equipas pelo seu ID.
     *
     * @param o outro objeto
     * @return true se os IDs são iguais
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return teamId.equals(team.teamId);
    }

    /**
     * HashCode baseado no teamId.
     *
     * @return hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(teamId);
    }
}