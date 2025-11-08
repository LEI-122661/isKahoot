import java.util.List;
import java.util.Map;

public class GameState {
    private List<Question> questions;
    private int currentIndex;
    private Map<String, Team> teams;

    public GameState(List<Question> questions, Map<String, Team> teams) {
        this.questions = questions;
        this.teams = teams;
        this.currentIndex = 0;
    }

    public Question getCurrentQuestion() {
        return questions.get(currentIndex);
    }

    public boolean nextQuestion() {
        if (currentIndex < questions.size() - 1) {
            currentIndex++;
            return true;
        }
        return false;
    }
}
