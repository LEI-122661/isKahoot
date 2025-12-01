package isKahoot.Game;// isKahoot.Game.Question.java

public class Question {
    private String question;
    private int points;
    private int correct;
    private String[] options;
    private String type;

    public String getQuestion() { return question; }
    public int getPoints() { return points; }
    public int getCorrect() { return correct; }
    public String[] getOptions() { return options; }
    public String getType() { return type; }

    public boolean isTeamQuestion(){
        return "TEAM".equals(type);
    }

    @Override
    public String toString() {
        return "isKahoot.Game.Question{" + question + ", points=" + points + "type: " + type + "}";
    }
}
