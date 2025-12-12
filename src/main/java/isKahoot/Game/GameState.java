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
    private final Map<String, Integer> answerBonus = new HashMap<>();

    private boolean roundActive;
    private boolean isTeamRound;

    //SINCRONIZAÇÃO DAS RESPOSTAS
    //private AnswerSemaphore semaforo;
    private Map<String, TeamBarrier> teamBarriers;
    private ModifiedCountdownLatch latch;

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
        answerBonus.clear();
        roundActive = true;

        for (Team team : teams.values()) {  //reinicia pontuação da ronda
            team.resetRoundScore();
        }

        isTeamRound = (currentIndex % 2 != 0);
        // Índice Par (0, 2, 4...) = INDIVIDUAL
        // Índice Ímpar (1, 3, 5...) = EQUIPA

        int activePlayers = getActivePlayers();

        //configurar latch
        if (activePlayers > 0) {
            latch = new ModifiedCountdownLatch(2, 2, 30000, activePlayers);
        } else {
            latch = null;
        }

        //configurar barrier
        if (isTeamRound) {
            teamBarriers = new HashMap<>();
            Question q = getCurrentQuestion();
            int points = q.getPoints();
            int correct = q.getCorrect();

            for (Team t : teams.values()) {
                int size = t.getPlayerCount();
                if (size > 0) {
                    teamBarriers.put(t.getTeamId(), new TeamBarrier(size, correct, points));
                }
            }
        } else {
            teamBarriers = null;
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

    public synchronized boolean receiveAnswer(String username, int optionIndex) {
        if (!roundActive) return false;
        if (currentAnswers.containsKey(username)) return false;

        currentAnswers.put(username, optionIndex);
        return true;
    }

    //tirei synchronized porque o semaforo ou barreira ja tratam da concorrencia
    public boolean receiveAnswer(String username, int optionIndex) {
        if (!roundActive) return false;
        if (currentAnswers.containsKey(username)) return false;

        //regista resposta
        currentAnswers.put(username,optionIndex);

        //chama semaforo e obtem multiplicador de pontos
        int pointsMultiplier = 0;
        if (semaforo != null){
            pointsMultiplier= semaforo.acquire();
        }

        //guardar bonus
        answerBonus.put(username,pointsMultiplier);

        return true;
    }  **/

    public boolean recieveAnswer(String username, int optionIndex) {
        Team team;
        synchronized(this) {
            if (!roundActive || currentAnswers.containsKey(username)) return false;
            // Regista logo que respondeu
            currentAnswers.put(username, optionIndex);
            team = findTeamOfPlayer(username);
        }
        if (team == null) return false;

        // 2. Decisao consaonte ronda
        if (teamBarriers != null && isTeamRound) {
            return handleTeamAnswer(username, optionIndex, team);
        } else {
            return handleIndividualAnswer(username); // No individual o index já foi guardado
        }
    }

    private boolean handleIndividualAnswer(String username) {
        int multiplier = 0;
        if (latch != null) {
            multiplier = latch.countdown();
        }
        synchronized(this) {
            answerBonus.put(username, multiplier);
        }
        return true;
    }

    private boolean handleTeamAnswer(String username, int optionIndex, Team team) {
        int points = 0;
        try {
            TeamBarrier barrier = teamBarriers.get(team.getTeamId());
            if (barrier != null) {
                // BLOQUEANTE: Espera pelo colega
                points = barrier.submitAnswer(optionIndex);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        // Guarda os pontos finais da equipa
        synchronized(this) {
            answerBonus.put(username, points);
        }
        return true;
    }


    //para gamehandler chamar waitForTimeout do semaforo
    public void waitForRoundToFinish() throws InterruptedException {
        if (latch != null){  //individual ou equipa, conta jogadores
            latch.await();
        }

        //tempo acabou, forca abertura das barreiras de equipa para n ficar preso no wait
        if(teamBarriers != null &&  isTeamRound){
            for(TeamBarrier b: teamBarriers.values()){
                b.forceOpenBarrier();
            }
        }
    }

    /**
     * Termina a ronda e calcula pontos.
     * Retorna mapa teamId -> pontos ganhos nessa ronda.

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

            int pointsGained = 0;
            if (answer == correct) {

                int bonus=answerBonus.getOrDefault(username,0);
                pointsGained = pointsPerCorrect * bonus;
                team.addPoints(pointsGained); // CORRIGIDO: era updateScore
            }

            String teamId = team.getTeamId(); // CORRIGIDO: era getTeamName
            roundPoints.put(teamId, roundPoints.getOrDefault(teamId, 0) + pointsGained);
        }

        return roundPoints;
    } **/


    public synchronized Map<String, Integer> endRoundAndComputePoints() {
        roundActive = false;
        Question q = getCurrentQuestion();
        if (q == null) return Collections.emptyMap();

        Map<String, Integer> roundPoints = new HashMap<>();

        // --- LÓGICA DE EQUIPA
        if (isTeamRound) {
            for (Team team : teams.values()) {
                // Tenta encontrar UM jogador desta equipa que tenha registo de bónus/pontos, 1 pq na barrier os dois recebem mesma pontuacao
                for (String player : team.getPlayers()) {
                    if (answerBonus.containsKey(player)) {
                        int points = answerBonus.get(player);
                        team.addPoints(points);
                        roundPoints.put(team.getTeamId(), points);
                        break; // Já processámos esta equipa, interrompe o loop dos jogadores e vai para a próxima equipa
                    }
                }
            }

        } else {
            // --- LÓGICA INDIVIDUAL
            // Aqui temos de somar o esforço individual de cada jogador
            int correct = q.getCorrect();
            int pointsPerCorrect = q.getPoints();
            for (Map.Entry<String, Integer> entry : currentAnswers.entrySet()) {  //por cada membro da equipa
                String username = entry.getKey();
                int answer = entry.getValue();
                Team team = findTeamOfPlayer(username);

                if (team == null) continue;

                int gained = 0;
                if (answer == correct) {
                    int multiplier = answerBonus.getOrDefault(username, 1);
                    gained = pointsPerCorrect * multiplier;
                    team.addPoints(gained);
                }
                // Soma ao total da equipa na ronda (acumula se o parceiro também acertar)
                String tId = team.getTeamId();
                roundPoints.put(tId, roundPoints.getOrDefault(tId, 0) + gained);
            }
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