package isKahoot.Game;

import java.util.*;

/**
 * GameState representa o estado de um jogo IsKahoot.
 * Armazena perguntas, equipas, respostas da ronda atual e lógica de pontuação.
 */
public class GameState {

    private final List<Question> questions;
    private int currentIndex;

    // teamId -> Team
    private final Map<String, Team> teams;

    // username -> optionIndex (resposta desta ronda)
    private final Map<String, Integer> currentAnswers;

    private boolean roundActive;

    /**
     * Construtor do GameState.
     *
     * @param questions lista de perguntas do jogo
     * @param teams mapa de equipas
     */
    public GameState(List<Question> questions, Map<String, Team> teams) {
        this.questions = questions;
        this.teams = teams;
        this.currentAnswers = new HashMap<>();
        this.currentIndex = 0;
        this.roundActive = false;
    }

    /**
     * Começa o jogo.
     */
    public void startGame() {
        currentIndex = 0;
        startRound();
    }

    /**
     * Começa uma ronda: limpa respostas antigas.
     */
    public void startRound() {
        currentAnswers.clear();
        roundActive = true;

        // Reseta a pontuação de ronda em todas as equipas
        for (Team team : teams.values()) {
            team.resetRoundScore();
        }
    }

    /**
     * Retorna a pergunta atual, ou null se acabou.
     */
    public Question getCurrentQuestion() {
        if (currentIndex >= questions.size()) {
            return null;
        }
        return questions.get(currentIndex);
    }

    /**
     * Verifica se há mais perguntas.
     */
    public boolean hasMoreQuestions() {
        return currentIndex < questions.size();
    }

    /**
     * Verifica se a ronda está ativa.
     */
    public boolean isRoundActive() {
        return roundActive;
    }

    /**
     * Registra resposta de um jogador.
     * Retorna false se já respondeu ou se a ronda acabou.
     *
     * @param username nome do jogador
     * @param optionIndex índice da opção (0-3)
     * @return true se a resposta foi aceite
     */
    public synchronized boolean receiveAnswer(String username, int optionIndex) {
        if (!roundActive) return false;
        if (currentAnswers.containsKey(username)) return false;

        currentAnswers.put(username, optionIndex);
        return true;
    }

    /**
     * Termina a ronda e calcula pontos.
     * Retorna mapa teamId -> pontos ganhos nessa ronda.
     */
    public synchronized Map<String, Integer> endRoundAndComputePoints() {
        roundActive = false;
        Question q = getCurrentQuestion();

        if (q == null) return Collections.emptyMap();

        int correct = q.getCorrect();
        int pointsPerCorrect = q.getPoints();

        Map<String, Integer> roundPoints = new HashMap<>();

        for (Map.Entry<String, Integer> entry : currentAnswers.entrySet()) {
            String username = entry.getKey();
            int answer = entry.getValue();
            Team team = findTeamOfPlayer(username);

            if (team == null) continue;

            int gained = 0;
            if (answer == correct) {
                gained = pointsPerCorrect;
                team.addPoints(gained); // CORRIGIDO: era updateScore
            }

            String teamId = team.getTeamId(); // CORRIGIDO: era getTeamName
            roundPoints.put(teamId, roundPoints.getOrDefault(teamId, 0) + gained);
        }

        return roundPoints;
    }

    /**
     * Avança para a próxima pergunta.
     * Retorna true se ainda há perguntas, false se acabou o jogo.
     */
    public boolean nextQuestion() {
        if (currentIndex < questions.size() - 1) {
            currentIndex++;
            startRound();
            return true;
        }
        return false;
    }

    /**
     * Retorna placar total: teamId -> score total.
     */
    public Map<String, Integer> getTotalScores() {
        Map<String, Integer> totals = new HashMap<>();
        for (Team t : teams.values()) {
            totals.put(t.getTeamId(), t.getTotalScore()); // CORRIGIDO: era getScore
        }
        return totals;
    }

    /**
     * Retorna placar da ronda: teamId -> pontos nesta ronda.
     */
    public Map<String, Integer> getRoundScores() {
        Map<String, Integer> roundScores = new HashMap<>();
        for (Team t : teams.values()) {
            roundScores.put(t.getTeamId(), t.getRoundScore());
        }
        return roundScores;
    }

    /**
     * Retorna todas as equipas.
     */
    public Collection<Team> getTeams() {
        return teams.values();
    }

    /**
     * Retorna uma equipa pelo ID.
     */
    public Team getTeam(String teamId) {
        return teams.get(teamId);
    }

    /**
     * Retorna o número de jogadores que responderam.
     */
    public int getAnswerCount() {
        return currentAnswers.size();
    }

    /**
     * Retorna o número de jogadores ativos.
     */
    public int getActivePlayers() {
        int count = 0;
        for (Team t : teams.values()) {
            count += t.getPlayerCount();
        }
        return count;
    }

    // ============= Helpers =============

    /**
     * Encontra a equipa de um jogador.
     */
    private Team findTeamOfPlayer(String username) {
        for (Team t : teams.values()) {
            if (t.hasPlayer(username)) return t;
        }
        return null;
    }
}