package isKahoot.Clients.Test2;

import isKahoot.Clients.Client;

/**
 * Client1 - Jogador 1 (Equipa 1)
 * Pré-configurado: alice
 *
 * Execução: java Client1
 * Exemplo: java Client1
 */
public class Eva3 {

    public static void main(String[] args) {
        // ✅ Valores pré-configurados
        String username = "eva";          // Nome do jogador
        String teamId = "equipa3";          // Equipa 1

        System.out.println("Client - " + username);
        System.out.println("Equipa: " + teamId);

        // ✅ Cria instância e chama runClient
        Client client = new Client();
        client.runClient(username, teamId);
    }
}