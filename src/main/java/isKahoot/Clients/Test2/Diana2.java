package isKahoot.Clients.Test2;

import isKahoot.Clients.Client;

/**
 * Client4 - Jogador 4 (Equipa 2)
 * Pré-configurado: diana
 *
 * Execução: java Client4
 * Exemplo: java Client4
 */
public class Diana2 {

    public static void main(String[] args) {
        // ✅ Valores pré-configurados
        String username = "diana";          // Nome do jogador
        String teamId = "equipa2";          // Equipa 2

        System.out.println("Client - " + username);
        System.out.println("Equipa: " + teamId);

        // ✅ Cria instância e chama runClient
        Client client = new Client();
        client.runClient(username, teamId);
    }
}