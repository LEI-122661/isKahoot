package isKahoot.Server;

import isKahoot.Game.GameState;
import isKahoot.Game.Question;
import isKahoot.Game.Team;
import java.util.*;
public class GameRoom {

    private String roomCode;
    private List<ConnectionHandler> players =new ArrayList<>();
    private Map<String, Team> teams = new HashMap<>();
    private GameState gameState;
    private boolean isGameRunning = false;
    private List<Question> questions = new ArrayList<>();

    public GameRoom(String roomCode, List<Question> questions) {
        this.roomCode = roomCode;
        this.questions=questions;
    }

    //adiciona jogadro a sala
    public synchronized boolean addPlayer(ConnectionHandler player){
        if(isGameRunning){
            return false;  //ja comecou nao da para entarr
        }
        players.add(player);
        return true;
    }

    public synchronized void startGame(){
        if(players.isEmpty() || isGameRunning){
            return;
        }
        System.out.println("[ROOM " + roomCode + "] A iniciar jogo com " + players.size() + " jogadores.");

        //cria equipas
        int numTeams = (int) Math.ceil(players.size() / 2.0);  // Exemplo: 5 jogadores -> 3 equipas
        for (int i = 1; i <= numTeams; i++) {
            String teamId = "team"+i;
            teams.put(teamId, new Team(teamId, "Team " + i));
        }

        //cria gamestate
        this.gameState = new GameState(questions, teams);

        for(ConnectionHandler p:players){
            p.setGameData(teams, gameState);
            p.assignToTeam;
        }

        //lanca thread do jogo
        new GameHandler(players,gameState).start();
        isGameRunning=true;


    }

    public int getPlayerCount(){
        return players.size();
    }

    public  List<ConnectionHandler> getPlayers(){
        return players;
    }

    public boolean isGameRunning(){
        return isGameRunning;
    }
}
