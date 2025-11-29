package isKahoot.Clients;

/**
 * Cliente 3 do jogo IsKahoot.
 * Conecta ao servidor com username "Client3".
 */
public class Client3 {
    public static void main(String[] args) {
        Client client = new Client();
        client.runClient("Client3"); // auto-atribui a "Client3"
    }
}