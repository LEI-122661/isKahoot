package isKahoot;

import java.util.ArrayList;
import java.util.List;

public class Team {

    private String teamName;
    private int score;
    private List<String> players;


    public Team(String teamName) {
        this.teamName=teamName;
        this.score=0;
        this.players= new ArrayList<>();
    }

    public String getTeamName() {
        return teamName;
    }

    public int getScore(){
        return score;
    }

    public List<String> getPlayers(){
        return players;
    }

    public void addPlayer(String playerName){
        if(players.size()<2){
            players.add(playerName);
        }
        else {
            System.out.println("Max 2 players per team.");
        }
    }

    public void updateScore(int points){
        score += points;
    }

    public boolean hasPlayer(String playerName){
        return players.contains(playerName);
    }
}
