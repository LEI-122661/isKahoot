package isKahoot.Clients.Test1;

import isKahoot.Clients.Client;

/**
 * Cliente 3 do jogo IsKahoot.
 * Conecta ao servidor com username "Client3".
 */
public class Client3 {
    public static void main(String[] args) {
        Client client = new Client();
        client.runClient("Client3", "Equipa2"); // auto-atribui a "Client3"
    }
}