package isKahoot.Clients;

/**
 * Client2 - Jogador 2 (Equipa 1)
 * Pré-configurado: bob
 *
 * Execução: java Client2
 * Exemplo: java Client2
 */
public class Client12 {

    public static void main(String[] args) {
        // ✅ Valores pré-configurados
        String username = "bob";            // Nome do jogador
        String teamId = "equipa1";          // Equipa 1

        System.out.println("Client2 - " + username);
        System.out.println("Equipa: " + teamId);

        // ✅ Cria instância e chama runClient
        Client client = new Client();
        client.runClient(username, teamId);
    }
}