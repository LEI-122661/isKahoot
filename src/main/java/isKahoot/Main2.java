package isKahoot;

import java.io.IOException;
import java.util.*;

public class Main2{

    public static void main(String[] args) {

        List<Question> questions;
        try {
            questions = QuestionLoader.loadFromJson("quizzes.json");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Team teamA = new Team("A");
        teamA.addPlayer("matilde");

        Team teamB = new Team("B");
        teamB.addPlayer("guilherme");

        Map<String, Team> teams = new HashMap<>();
        teams.put("A", teamA);
        teams.put("B", teamB);

        GameState gs = new GameState(questions, teams);

        ClientGUI gui = new ClientGUI();

        // SUBMETER: regista e mostra pontuação, NÃO avança
        gui.setAnswerSender(optionIndex -> {
            gs.receiveAnswer("matilde", optionIndex);
            gs.endRoundAndComputePoints();

            gui.showScoreboard(
                    "Equipa A: " + teamA.getScore() +
                            " | Equipa B: " + teamB.getScore()
            );
        });

        // AVANÇAR: só aqui passa à próxima
        gui.setNextSender(() -> {
            if (gs.nextQuestion()) {
                Question q = gs.getCurrentQuestion();
                String[] opts = q.getOptions();

                gui.showQuestion(q.getQuestion(), opts);
                gui.startTimer(30);
            } else {
                gui.showQuestion("Fim do jogo!", new String[]{"", "", "", ""});
                gui.removeButtons();

            }
        });

        gs.startGame();
        Question first = gs.getCurrentQuestion();
        String[] firstOpts = first.getOptions();

        gui.showQuestion(first.getQuestion(), firstOpts);
        gui.startTimer(30);
        gui.showScoreboard("Equipa A: 0  | Equipa B: ");
    }
}
