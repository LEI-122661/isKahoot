package isKahoot.Game;// isKahoot.Game.Question.java

public class Question {
    private String question;
    private int points;
    private int correct;
    private String[] options;

    public String getQuestion() { return question; }
    public int getPoints() { return points; }
    public int getCorrect() { return correct; }
    public String[] getOptions() { return options; }

    @Override
    public String toString() {
        return "isKahoot.Game.Question{" + question + ", points=" + points + "}";
    }
}
