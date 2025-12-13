package isKahoot.Game;


import java.util.ArrayList;
import java.util.List;

/**
 * Barreira para coordenar as respostas da equip
 * Espera que os 2 elementos da equipa respondam.
 * Calcula a pontuação conjunta (bónus se ambos acertarem).
 * Suporta fim por timeout.
 */
public class TeamBarrier {

    private final int teamSize;
    private int respondedCount;
    private final int correctOption;
    private final int questionPoints;

    private List<Integer> teamAnswers =new ArrayList<>();

    private int teamScoreFinal;
    private boolean barrierOpen;

    public TeamBarrier(int teamSize, int correctOption, int questionPoints) {
        this.correctOption = correctOption;
        this.questionPoints = questionPoints;
        this.respondedCount =0;
        this.teamScoreFinal=0;
        this.barrierOpen=false;
        this.teamSize=teamSize;
    }

    public synchronized int submitAnswer(int answerInex)throws InterruptedException{
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
                wait(30000);
            }

        } else {
            //todos responderam, abre barreira
            calculateTeamScore();
            barrierOpen=true;
            notifyAll();
        }
        return teamScoreFinal;
    }

/**
 * Chamado pelo Servidor se o tempo da ronda acabar
 * Força a abertura da barreira mesmo que falte alguem
 */
    public synchronized void forceOpenBarrier(){
        if(!barrierOpen){
            calculateTeamScore();
            barrierOpen=true;
            notifyAll();
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
