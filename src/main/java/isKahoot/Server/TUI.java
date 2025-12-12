package isKahoot.Server;

import java.util.Scanner;

public class TUI {

    private GameServer gameServer;


    public TUI(GameServer gameServer) {
        this.gameServer=gameServer;
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== ISKAHOOT SERVER ===");
        System.out.println("Comandos disponíveis:");
        System.out.println(" > create       : Cria uma nova sala e gera o código");
        System.out.println(" > list         : Lista todas as salas ativas e jogadores");
        System.out.println(" > start <code> : Inicia o jogo na sala especificada (ex: start ABCD)");
        System.out.println(" > exit         : Encerra o servidor");

        while (true) {
            System.out.print("> ");
            // Lê a linha inteira e remove espaços extra
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            // Separa comando de argumentos (ex: "start ABCD" -> ["start", "ABCD"])
            String[] parts = line.split("\\s+");
            String command = parts[0].toLowerCase();

            switch (command) {
                case "create":
                    if (parts.length < 2) {
                        System.out.println("Erro: faltam argumentos. Ex: create 2 2");
                    }
                    int numEquipas = Integer.parseInt(parts[1]);
                    int numJogadores = Integer.parseInt(parts[2]);
                    gameServer.createRoom(numEquipas, numJogadores);
                    break;


                case "list":
                    System.out.println(gameServer.listRooms());
                    break;

                case "start":
                    if (parts.length < 2) {
                        System.out.println("Erro: Indica o código da sala. Ex: start ABCD");
                    } else {
                        String code = parts[1].toUpperCase();
                        gameServer.startGame(code);
                    }
                    break;

                case "exit":
                    gameServer.closeServer();
                    return;

                default:
                    System.out.println("Comando desconhecido.");
            }
        }
    }


}
