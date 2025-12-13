package isKahoot.Clients.Test2;

import isKahoot.Clients.Client;


public class Eva3 {

    public static void main(String[] args) {

        String username = "eva";
        String teamId = "equipa3";

        System.out.println("Client - " + username);
        System.out.println("Equipa: " + teamId);

        Client client = new Client();
        client.runClient(username, teamId);
    }
}