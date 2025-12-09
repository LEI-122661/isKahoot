package isKahoot.Game;

public class ModifiedCountdownLatch {

    private int bonusFactor;
    private int bonusCount;
    private int waitperiod;
    private int currentCount;


    public ModifiedCountdownLatch(int bonusFactor, int bonusCount, int waitperiod, int currentCount) {
        this.bonusFactor = bonusFactor;
        this.bonusCount = bonusCount;
        this.waitperiod = waitperiod;
        this.currentCount = currentCount;
    }


    public synchronized int countdown (){
        //TODO: implementar logica de bonus
        currentCount--;

        if(currentCount<=0){
            notifyAll();
            return 0;
        }
        if(currentCount<=2){
            return
        }

        return bonusFactor;
    }


    public synchronized void await () throws InterruptedException{
        long startTime = System.currentTimeMillis();

        while(currentCount>0){
            long timePassed =System.currentTimeMillis() -startTime;
            long timeLeft = waitperiod - timePassed;

            if(timeLeft <= 0){  //acabao tempo
                notifyAll();
                break;
            }

            wait(timeLeft);  //espera o tempo que falta
        }
    }

    public void setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
    }



}
