package isKahoot.Server;

import java.util.List;
public class GameHandler extends Thread {
    private final List<ConnectionHandler> clients;

    public GameHandler(List<ConnectionHandler> clients) {
        this.clients = clients;
    }

    @Override
    public void run() {
        try {
            // 1) LOBBY
            broadcast("SCREEN:LOBBY");
            Thread.sleep(5000); // tempo para todos entrarem / verem lobby

            // 2) PERGUNTA 1
            broadcast("SCREEN:QUESTION:1");
            Thread.sleep(10000); // tempo para responder

            // 3) RESULTADOS da pergunta 1
            broadcast("SCREEN:RESULTS:1");
            Thread.sleep(5000);

            // 4) PERGUNTA 2 (exemplo)
            broadcast("SCREEN:QUESTION:2");
            Thread.sleep(10000);

            // 5) RESULTADOS finais
            broadcast("SCREEN:FINAL");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void broadcast(String msg) {
        for (ConnectionHandler ch : clients) {
            ch.sendMessage(msg);
        }
        System.out.println("Enviado para todos: " + msg);
    }
}
