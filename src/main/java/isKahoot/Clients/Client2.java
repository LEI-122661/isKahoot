package isKahoot.Clients;

/**
 * Cliente 2 do jogo IsKahoot.
 * Conecta ao servidor com username "Client2".
 */
public class Client2 {
    public static void main(String[] args) {
        Client client = new Client();
        client.runClient("Client2","Equipa1"); // auto-atribui a "Client2"
    }
}