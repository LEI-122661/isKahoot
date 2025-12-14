package isKahoot.Server;

import java.util.Scanner;


public class TUI {

    private final GameServer gameServer;

    public TUI(GameServer gameServer) {
        this.gameServer = gameServer;
    }


    public void start() {
        Scanner scanner = new Scanner(System.in);

        printBanner();
        //printHelp(); - só devia ser ativado ao pedirem ajuda

        while (true) {
            System.out.print("\n> ");
            String line = scanner.nextLine().trim();

            // Ignora linhas vazias
            if (line.isEmpty()) {
                continue;
            }

            // Divide a linha em comando e argumentos
            String[] parts = line.split("\\s+");
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "create":
                        handleCreate(parts);
                        break;

                    case "list":
                        handleList();
                        break;

                    case "start":
                        handleStart(parts);
                        break;

                    case "help":
                        printHelp();
                        break;

                    case "exit":
                        handleExit(scanner);
                        return;

                    default:
                        System.out.println(" Comando desconhecido. Digite 'help' para ver os comandos disponiveis.");
                }
            } catch (Exception e) {
                System.out.println(" Erro ao processar comando: " + e.getMessage());
            }
        }
    }


    private void handleCreate(String[] parts) {
        if (parts.length < 3) {
            System.out.println("   Erro: faltam argumentos.");
            System.out.println("   Sintaxe: create <numEquipas> <numJogadoresPerEquipa>");
            System.out.println("   Exemplo: create 2 2");
            return;
        }

        try {
            int numEquipas = Integer.parseInt(parts[1]);
            int numJogadores = Integer.parseInt(parts[2]);

            if (numEquipas <= 0 || numJogadores <= 0) {
                System.out.println(" Erro: números devem ser maiores que 0.");
                return;
            }

            gameServer.createRoom(numEquipas, numJogadores);

        } catch (NumberFormatException e) {
            System.out.println(" Erro: argumentos devem ser números inteiros.");
        }
    }


    private void handleList() {
        String roomList = gameServer.listRooms();
        System.out.println(roomList);
    }


    private void handleStart(String[] parts) {
        if (parts.length < 2) {
            System.out.println(" Erro: faltam argumentos.");
            System.out.println("   Sintaxe: start <codigoSala>");
            return;
        }

        String code = parts[1].toUpperCase();
        gameServer.startGame(code);
    }


    private void handleExit(Scanner scanner) {
        System.out.println(" Tens a certeza que queres encerrar o servidor? (s/n)");
        System.out.print("> ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("s") || confirm.equals("sim")) {
            System.out.println("Encerrando servidor...");
            gameServer.closeServer();
        } else {
            System.out.println("Operação cancelada.");
        }
    }


    private void printBanner() {
        System.out.println("\n  IsKahoot ");
        System.out.println("    para ajuda com comandos, digite 'help'\n");
    }


    private void printHelp() {
        System.out.println("\n-> Comandos disponíveis:");

        System.out.println("  create <numEquipas> <numJogadores>");
        System.out.println("    → Cria uma nova sala com o número especificado de equipas e jogadores");
        System.out.println("    Exemplo: create 2 2");

        System.out.println("\n  list");
        System.out.println("    → Mostra todas as salas ativas e o estado atual");
        System.out.println("    Exemplo: list");

        System.out.println("\n  start <codigoSala>");
        System.out.println("    → Inicia o jogo numa sala específica");
        System.out.println("    Exemplo: start ABCD");

        System.out.println("\n  help");
        System.out.println("    → Mostra esta mensagem de ajuda");

        System.out.println("\n  exit");
        System.out.println("    → Encerra o servidor");

    }
}