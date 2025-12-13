package isKahoot.Clients.Test2;

import isKahoot.Clients.Client;

public class Diana2 {

    public static void main(String[] args) {
        String username = "diana";
        String teamId = "equipa2";

        System.out.println("Client - " + username);
        System.out.println("Equipa: " +teamId);


        Client client= new Client();
        client.runClient(username, teamId);
    }
}