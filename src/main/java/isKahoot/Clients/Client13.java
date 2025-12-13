package isKahoot.Clients;

/**
 * Client3 - Jogador 3 (Equipa 2)
 * Pré-configurado: charlie
 *
 * Execução: java Client3
 * Exemplo: java Client3
 */
public class Client13 {

    public static void main(String[] args) {
        // ✅ Valores pré-configurados
        String username = "charlie";        // Nome do jogador
        String teamId = "equipa2";          // Equipa 2

        System.out.println("Client3 - " + username);
        System.out.println("Equipa: " + teamId);

        // ✅ Cria instância e chama runClient
        Client client = new Client();
        client.runClient(username, teamId);
    }
}