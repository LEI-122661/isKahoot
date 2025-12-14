package isKahoot.Game;

public class ModifiedCountdownLatch {

    private int bonusFactor;
    private int numPlayersGetBonus;            //quantos ganham bonus
    private int waitperiod;
    private int playersLeftToAnswer;                // jogadores que faltam responder
    private boolean timedOut = false;
    private int totalPlayers;


    public ModifiedCountdownLatch(int bonusFactor, int numPlayersGetBonus, int waitperiod, int playersLeftToAnswer) {
        this.bonusFactor = bonusFactor;
        this.numPlayersGetBonus = numPlayersGetBonus;
        this.waitperiod = waitperiod;
        this.playersLeftToAnswer = playersLeftToAnswer;
        this.totalPlayers = playersLeftToAnswer;

    }

    //chamado por cada (DealWithClient) quando responde
    public synchronized int countdown (){
        if(timedOut || playersLeftToAnswer <0){
            return 0;
        }

        playersLeftToAnswer--;

        if(playersLeftToAnswer ==0) {   //ultimo players respondeu
            notifyAll();
        }

        //Logiva do bonus
        int answersRecieved = totalPlayers - playersLeftToAnswer;
        if(answersRecieved <=bonusFactor){
            return numPlayersGetBonus;
        } else {
            return 1;
        }

    }

    //chamada pelo
    public synchronized void await() throws InterruptedException {
        long startTime = System.currentTimeMillis();

        //enquanto houver jogadores que ainda não responderam
        while (playersLeftToAnswer > 0) {
            long timeElapsed = System.currentTimeMillis() - startTime;
            long remaining = waitperiod - timeElapsed;

            // 1  Se o tempo acabou
            if (remaining <= 0) {
                timedOut = true;
                break; // Sai do loop imediatament
            }
            // 2 Espera apenas o tempo que sobra
            wait(remaining);
        }
    }
}
