package isKahoot.Game;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;


public class TeamBarrier {

    private final int teamSize;
    private int respondedCount;
    private final int correctOption;
    private final int questionPoints;

    private List<Integer> teamAnswers =new ArrayList<>();

    private int teamScoreFinal;
    private boolean barrierOpen;

    private ReentrantLock lock = new ReentrantLock();
    private final Condition teamReady= lock.newCondition();
    //nome teamReady é a negacao da condicao de bloqueio, neste caso
    // bloqueia quando tem de esperar pelo outro membro da equipa

    public TeamBarrier(int teamSize, int correctOption, int questionPoints) {
        this.correctOption = correctOption;
        this.questionPoints = questionPoints;
        this.respondedCount =0;
        this.teamScoreFinal=0;
        this.barrierOpen=false;
        this.teamSize=teamSize;
    }

  /**  public synchronized int submitAnswer(int answerInex)throws InterruptedException{
        //barreita abriu(todos responderam ou timeout)
        if(barrierOpen && respondedCount <teamSize){
            return teamScoreFinal;
        }

        //regista resposta
        teamAnswers.add(answerInex);
        respondedCount++;

        //logica da barreira, so abre quando todos responderam, da mesma equipa
        if(respondedCount < teamSize){

            //espera o outro elemento da equipa responda
            while (!barrierOpen){
                wait();
            }

        } else {
            //todos responderam, abre barreira
            calculateTeamScore();
            barrierOpen=true;
            notifyAll();
        }
        return teamScoreFinal;
    } */

    public int submitAnswer(int answerInex)throws InterruptedException{
        lock.lock();
        try{
            //abriu por timeout, nem todos mandaram resposta
            if(barrierOpen && respondedCount <teamSize){
                return teamScoreFinal;
            }

            teamAnswers.add(answerInex);
            respondedCount++;

            // logica da barrier
            if(respondedCount < teamSize){
                // barreira nao aberta, a var condicional espera
                while (!barrierOpen){
                    teamReady.await();
                }
            } else {
                //ultimo da equipa respondeu, barrier abre e notifica
                calculateTeamScore();
                barrierOpen=true;
                teamReady.signalAll();
            }
            return teamScoreFinal;
        } finally {
            lock.unlock();
        }

    }

    // caso falte aguem responder, tem de abirr para ir a proxima pergunta
    public void forceOpenBarrier(){
        lock.lock();
        try{
            if(!barrierOpen){
                calculateTeamScore();
                barrierOpen=true;
                teamReady.signalAll();
            }

        } finally {
            lock.unlock();
        }

    }


    private void calculateTeamScore(){
        if(teamAnswers.isEmpty()){
            teamScoreFinal=0;
            return;
        }

        int correctCount=0;
        for(int answer:teamAnswers){
            if(answer == correctOption){
                correctCount++;
            }
        }

        //todos acertaram
        if(correctCount == teamSize){
            teamScoreFinal= questionPoints *2; //bónus
        }
        //apenas um acertou
        else if (correctCount ==1) {
            teamScoreFinal= questionPoints; //pontos normais
        }
        //ninguem acertou :((
        else {
            teamScoreFinal=0; //nenhum acertou
        }
    }

}
