package isKahoot;

import java.util.*;

/**
 * GameState representa UMA sala de jogo.
 * Guarda pergunta atual, respostas da ronda e placar por equipa.
 */
public class GameState {

    private final List<Question> questions;
    private int currentIndex;

    // team -> Team
    private final Map<String, Team> teams;

    // username -> optionIndex (resposta desta ronda)
    private final Map<String, Integer> currentAnswers;

    private boolean roundActive;

    public GameState(List<Question> questions, Map<String, Team> teams) {
        this.questions = questions;
        this.teams = teams;
        this.currentAnswers = new HashMap<>();
        this.currentIndex = 0;
        this.roundActive = false;
    }

    /** Começa o jogo. */
    public void startGame() {
        currentIndex = 0;
        startRound();
    }

    /** Começa uma ronda: limpa respostas antigas. */
    public void startRound() {
        currentAnswers.clear();
        roundActive = true;
    }

    /**
     * Pergunta atual, ou null se acabou.
     */
    public Question getCurrentQuestion() {
        if (currentIndex >= questions.size()){
            return null;
        }
         return questions.get(currentIndex);
    }

    public boolean hasMoreQuestions() {
        return currentIndex < questions.size();
    }

    public boolean isRoundActive() {
        return roundActive;
    }

    /**
     * Regista resposta de um jogador.
     * Retorna false se já respondeu ou se a ronda acabou.
     */
    public synchronized boolean receiveAnswer(String username, int optionIndex) {
        if (!roundActive) return false;
        if (currentAnswers.containsKey(username)) return false;

        currentAnswers.put(username, optionIndex);
        return true;
    }

    /**
     * Termina a ronda e calcula pontos simples.
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
                team.updateScore(gained);
            }

            String teamId = team.getTeamName();
            roundPoints.put(teamId,
                    roundPoints.getOrDefault(teamId, 0) + gained);
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

    /** Placar total atual: teamId -> score total. */
    public Map<String, Integer> getTotalScores() {
        Map<String, Integer> totals = new HashMap<>();
        for (Team t : teams.values()) {
            totals.put(t.getTeamName(), t.getScore());
        }
        return totals;
    }

    // ---------------- helpers ----------------

    private Team findTeamOfPlayer(String username) {
        for (Team t : teams.values()) {
            if (t.hasPlayer(username)) return t;
        }
        return null;
    }
}
