package isKahoot.Game;

public class ModifiedCountdownLatch {

    private int bonusFactor;
    private int bonusCount;            //quantos ganham bonus
    private int waitperiod;
    private int count;                // jogadores que faltam responder
    private boolean timedOut = false;
    private int totalPlayers;


    public ModifiedCountdownLatch(int bonusFactor, int bonusCount, int waitperiod, int count) {
        this.bonusFactor = bonusFactor;
        this.bonusCount = bonusCount;
        this.waitperiod = waitperiod;
        this.count = count;
        this.totalPlayers = count;

    }

    //chamado por cada jogador(ConnectionHandler) quando responde
    public synchronized int countdown (){
        if(timedOut || count<0){
            return 0;
        }

        count--;

        if(count == 0) {   //ultimo players respondeu
            notifyAll();
        }

        //Logiva do bonus
        int answersRecieved = totalPlayers - count; //+1 pq ja decrementou
        if(answersRecieved <=bonusFactor){
            return bonusCount;
        } else {
            return 1;
        }

    }

    //chamada pelo
   /** public synchronized void await() throws  InterruptedException {
        long startTime = System.currentTimeMillis();
        long remainingTime = waitperiod;

        while (!timedOut && count>0){
            wait(remainingTime);

            long tempoDecorrido = System.currentTimeMillis() - startTime;
            remainingTime = waitperiod - tempoDecorrido;
        }

        if(remainingTime <= 0 && count>0){
            timedOut =true;
        }
    } */

    /**
     * Chamado pelo SERVIDOR para esperar pelo fim da ronda.
     * Implementa o Timer interno de forma segura.
     */

    public synchronized void await() throws InterruptedException {
        long startTime = System.currentTimeMillis();

        // Loop enquanto houver jogadores que ainda não responderam
        while (count > 0) {
            long timeElapsed = System.currentTimeMillis() - startTime;
            long remaining = waitperiod - timeElapsed;

            // 1. Verificação de Segurança: Se o tempo acabou (ou ficou negativo)
            if (remaining <= 0) {
                timedOut = true;
                break; // Sai do loop IMEDIATAMENTE. Não tenta fazer wait(-1).
            }

            // 2. Espera apenas o tempo que sobra
            wait(remaining);
        }
    }
}
