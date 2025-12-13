package isKahoot.Clients.Test2;

import isKahoot.Clients.Client;

public class Bob1 {

    public static void main(String[] args) {
        String username = "bob";
        String teamId = "equipa1";

        System.out.println("Client - " +username);
        System.out.println("Equipa: " + teamId);

        Client client = new Client();
        client.runClient(username, teamId);
    }
}