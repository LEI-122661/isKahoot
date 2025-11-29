package isKahoot.Clients;

/**
 * Cliente 1 do jogo IsKahoot.
 * Conecta ao servidor com username "Client1".
 */
public class Client1 {
    public static void main(String[] args) {
        Client client = new Client();
        client.runClient("Client1"); // auto-atribui a "Client1"
    }
}