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

        if(count == 0){   //ultimo players respondeu
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
    public synchronized void await() throws  InterruptedException {
        long startTime = System.currentTimeMillis();
        long remainngTime = waitperiod;

        while (!timedOut && count>0){
            wait(remainngTime);

            long tempoDecorrido = System.currentTimeMillis() - startTime;
            remainngTime = waitperiod - tempoDecorrido;
        }

        if(remainngTime <= 0 && count>0){
            timedOut =true;
        }
    }




}
