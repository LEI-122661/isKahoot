# IsKahoot - Jogo Concorrente e Distribuído (PCD 2025/26)

O **IsKahoot** é um sistema de quiz distribuído e concorrente, inspirado no popular Kahoot!, desenvolvido como projeto principal da unidade curricular de **Programação Concorrente e Distribuída**. O sistema foca-se na gestão rigorosa de concorrência e sincronização de threads em Java, utilizando mecanismos de coordenação personalizados.

## 🚀 Arquitetura do Sistema

O projeto utiliza uma arquitetura cliente-servidor para gerir múltiplas sessões de jogo autónomas simultaneamente.

### Servidor (Central de Comando)
* **Gestão de Salas**: Criação dinâmica de salas via TUI (Interface de Texto).
* **Controlo de Fluxo**: Cada jogo corre numa thread `GameHandler` dedicada, enquanto cada jogador ligado é gerido por uma thread `DealWithClient`.
* **TUI Administrativa**: Suporta comandos como `create`, `list`, `start` e `exit`.

### Cliente e Equipa
* **Equipas**: O jogo é disputado individualmente, mas a pontuação e classificação são feitas por equipas de dois jogadores.
* **GUI (Interface Gráfica)**: Desenvolvida em Swing para visualização de perguntas, cronómetros e placares em tempo real.
* **Protocolo**: Comunicação baseada em Sockets TCP com serialização de objetos.

---

## 🛠️ Mecanismos de Coordenação (Requisitos PCD)

Conforme exigido pelo enunciado, todos os mecanismos de coordenação foram desenvolvidos manualmente, sem recurso às bibliotecas padrão do Java:

1. **ModifiedCountdownLatch**: Gere as **perguntas individuais** e atribui bónus de rapidez (dobro da pontuação) aos primeiros dois jogadores a responder. Implementa um método `await()` com tempo limite.
2. **TeamBarrier**: Utilizada nas **perguntas de equipa**. Implementada obrigatoriamente com `ReentrantLock` e variáveis condicionais (`Condition`). A pontuação só é calculada após a resposta de todos os membros ou fim do tempo.
3. **Gestão Thread-Safe**: Uso de `GameState` para manter a integridade dos dados entre múltiplas threads concorrentes.

---

## 📦 Como Executar e Testar

### 1. Iniciar o Servidor
Inicie o servidor e utilize a interface de texto para configurar a partida. No terminal do servidor, crie uma sala (ex: 3 equipas de 2 jogadores):

```bash
# Iniciar o servidor
java isKahoot.Server.GameServer

# No terminal do servidor, crie a sala:
create 3 2
```

### 2. Como Testar (Simulação de Jogadores)
Para testar o comportamento concorrente e as barreiras de equipa, utilize as classes de teste pré-configuradas no pacote `isKahoot.Clients.Test2`. Abra terminais distintos para cada par:

**Abrir Terminais para a Equipa 1:**
```bash
# Terminal A
java isKahoot.Clients.Test2.Alice1

# Terminal B
java isKahoot.Clients.Test2.Bob1
```

**Abrir Terminais para a Equipa 2:**
```bash
# Terminal C
java isKahoot.Clients.Test2.Charlie2

# Terminal D
java isKahoot.Clients.Test2.Diana2
```

**Abrir Terminais para a Equipa 3:**
```bash
# Terminal E
java isKahoot.Clients.Test2.Eva3

# Terminal F
java isKahoot.Clients.Test2.Frank3
```

### 3. Iniciar o Jogo
Após os jogadores estarem conectados (o servidor mostrará o estado das equipas preenchidas), utilize o comando no servidor:

```bash
start {CODIGO_DA_SALA}
```
