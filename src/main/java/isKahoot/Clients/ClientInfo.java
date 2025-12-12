package isKahoot.Clients;

import java.io.Serializable;

/**
 * Informações do cliente ao conectar-se ao servidor.
 * Serializable para enviar via ObjectStreams.
 */
public class ClientInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String username;      // nome do jogador (ex: "Client1", "Client2")
    private String gameCode;      // código do jogo (futuramente)
    private String teamId;        // ID da equipa (ex: "team1", "team2")
    
    /**
     * Construtor com informações básicas.
     *
     * @param username nome do jogador
     * @param gameCode código do jogo (pode ser null para agora)
     * @param teamId   ID da equipa solicitada (pode ser null, servidor atribui)
     */
    public ClientInfo(String username, String gameCode, String teamId) {
        this.username = username;
        this.gameCode = gameCode;
        this.teamId = teamId;
    }
    
    // Getters
    public String getUsername() {
        return username;
    }
    
    public String getGameCode() {
        return gameCode;
    }
    
    public String getTeamId() {
        return teamId;
    }
    
    // Setters
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }
    
    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }
    
    @Override
    public String toString() {
        return "ClientInfo{" +
                "username='" + username + '\'' +
                ", gameCode='" + gameCode + '\'' +
                ", teamId='" + teamId + '\'' +
                '}';
    }
}