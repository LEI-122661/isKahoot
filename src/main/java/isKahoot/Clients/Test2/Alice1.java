package isKahoot.Clients.Test2;

import isKahoot.Clients.Client;

public class Alice1 {

    public static void main(String[] args) {
        // valores pré-configurados
        String username = "alice";
        String teamId = "equipa1";

        System.out.println("Client - " + username);
        System.out.println("Equipa: " + teamId);

        //cria instancia e chama runClient
        Client client = new Client();
        client.runClient(username, teamId);
    }
}