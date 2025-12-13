package isKahoot.Server;

import isKahoot.Game.GameState;
import isKahoot.Game.Question;

import java.util.List;
import java.util.Map;

/**
 * Thread que gere o ciclo do jogo.
 * Envia perguntas, aguarda respostas, calcula pontos e avança para a próxima ronda.
 */
public class GameHandler extends Thread {

    private final List<ConnectionHandler> clients;
    private final GameState gameState;
    private static final long QUESTION_TIMEOUT = 30000; // 30 segundos

    /**
     * Construtor do GameHandler.
     *
     * @param clients lista de clientes conectados
     * @param gameState estado do jogo
     */
    public GameHandler(List<ConnectionHandler> clients, GameState gameState) {
        this.clients = clients;
        this.gameState = gameState;
    }

    @Override
    public void run() {
        try {
            // Inicia o jogo
            gameState.startGame();
            System.out.println("[GAME] Jogo iniciado!");

            int questionNumber = 1;

            // Loop para cada pergunta
            while (gameState.hasMoreQuestions()) {
                Question q = gameState.getCurrentQuestion();

                if (q == null) break;

                System.out.println("[GAME] Pergunta " + questionNumber + ": " + q.getQuestion());

                // 1. Envia a pergunta a todos os clientes
                sendQuestion(q);

                // 2. Aguarda respostas (com timeout)
                waitForAnswers();

                // 3. Calcula pontos
                calculatePoints();

                // 4. Envia placar
                sendScoreboard();

                // 5. Pequena pausa para o utilizador ver o placar
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // 6. Avança para próxima pergunta
                if (!gameState.nextQuestion()) {
                    break; // Fim do jogo
                }

                questionNumber++;
            }

            // Fim do jogo
            System.out.println("[GAME] Jogo terminado!");
            sendFinalScoreboard();

        } catch (Exception e) {
            System.err.println("[GAME] Erro no ciclo do jogo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envia a pergunta a todos os clientes.
     */
    private void sendQuestion(Question q) {
        String questionMsg = "SCREEN:QUESTION:" +
                q.getQuestion() + "|" +
                q.getOptions()[0] + "|" +
                q.getOptions()[1] + "|" +
                q.getOptions()[2] + "|" +
                q.getOptions()[3] + "|" +
                30; // 30 segundos

        broadcast(questionMsg);
        System.out.println("[GAME] Pergunta enviada a " + clients.size() + " clientes");
    }

    /**
     * Aguarda respostas dos clientes com timeout.
     * Termina quando:
     * - Todos os jogadores responderam, OU
     * - O timeout expira
     */
    private void waitForAnswers() {
        int expectedAnswers = gameState.getActivePlayers();

        System.out.println("[GAME] À espera de " + expectedAnswers + " respostas (timeout: + " + QUESTION_TIMEOUT+" )...");

        try{
            //gamsate acorda quando timedout ou todas respostas recebidas
            gameState.waitForRoundToFinish();
            System.out.println("[GAME] Ronda terminou (todas respostas recebidas ou timeout).");

        } catch (InterruptedException e) {
            System.err.println("[GAME] Espera por respostas interrompida: " + e.getMessage());
            Thread.currentThread().interrupt();

        }
        System.out.println("[GAME] Fim da espera (Tempo esgotado ou todos responderam).");


    }


    private void calculatePoints() {
        Map<String, Integer> roundPoints = gameState.endRoundAndComputePoints();

        System.out.println("[GAME] Pontos calculados:");
        for (Map.Entry<String, Integer> entry : roundPoints.entrySet()) {
            System.out.println("  - " + entry.getKey() + ": " + entry.getValue() + " pontos");
        }
    }

    //Envia o placar atualizado a todos os clientes.
    private void sendScoreboard() {
        Map<String, Integer> totalScores = gameState.getTotalScores();

        StringBuilder scoreMsg = new StringBuilder("SCORE:");
        for (Map.Entry<String, Integer> entry : totalScores.entrySet()) {
            scoreMsg.append(entry.getKey()).append(":").append(entry.getValue()).append("|");
        }

        broadcast(scoreMsg.toString());
        System.out.println("[GAME] Placar enviado: " + scoreMsg);
    }




    private void sendFinalScoreboard() {
        Map<String, Integer> totalScores = gameState.getTotalScores();

        StringBuilder finalMsg = new StringBuilder("SCREEN:FINAL:");
        for (Map.Entry<String, Integer> entry : totalScores.entrySet()) {
            finalMsg.append(entry.getKey()).append(":").append(entry.getValue()).append("|");
        }

        broadcast(finalMsg.toString());
        System.out.println("[GAME] Placar final enviado: " + finalMsg);
    }



    private void broadcast(String msg) {
        for (ConnectionHandler ch : clients) {
            ch.sendMessage(msg);
        }
    }
}