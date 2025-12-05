package isKahoot.Game;

public class AnswerSemaphore {

    private final int totalPlayers;
    private int permits;     //numero de respostas que recebemos
    private boolean timedOut;

    public AnswerSemaphore(int totalPlayers) {
        this.totalPlayers = totalPlayers;
        this.permits = totalPlayers;
        this.timedOut = false;
    }

    /**
     * enunciado pede: "método waitForTimeout, bloqueante"
     * bloqueia até todos responderem OU o tempo acabar.
     */
    public synchronized void waitForTimeout(long temporizador) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        //enquanto esperamos respostas e tempo nao esgotou
        while (permits >0  && !timedOut) {
            long timePassed =System.currentTimeMillis() -startTime;
            long timeLeft = temporizador - timePassed;

            if(timeLeft <= 0){  //acabao tempo
                timedOut=true;
                notifyAll();
                break;
            }

            wait(timeLeft);  //espera o tempo que falta
        }

    }

    /**
     * acquire devolve pontuacao das perguntas individuais
     * dobro para os dois primeiros a responder
     */
    public synchronized int acquire() {

        //tempo acabou, detetado por waitForTimeout
        if(timedOut){
            return 0; //se o tempo esgotou, nao ha pontos
        }

        //se ainda esperamos respostas
        if(permits >0){
            permits--;   //chegou resposta

            //se e ultimo a responder
            if(permits == 0){
                notifyAll();  //avisa waitForTimeout que ja responderam todos
            }
        } else {
            return 0;
        }

        //Calculo de pontos
        // Se total = 4 e faltam (permits) 3 -> responderam 1 (foi o 1º)
        int numberPlayersAnswered = totalPlayers - permits;

        if(numberPlayersAnswered <= 2){
            return 2; //dobro pontos
        } else {
            return 1; //pontos normais
        }
    }


}
