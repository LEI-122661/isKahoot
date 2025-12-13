package isKahoot.Clients.Test1;

import isKahoot.Clients.Client;

/**
 * Cliente 4 do jogo IsKahoot.
 * Conecta ao servidor com username "Client4".
 */
public class Client4 {
    public static void main(String[] args) {
        Client client = new Client();
        client.runClient("Client4","Equipa2"); // auto-atribui a "Client4"
    }
}