package isKahoot.Game;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * GUI melhorada e minimalista para o IsKahoot.
 * Sem botão avançar (desnecessário).
 * Design limpo e elegante.
 */
public class GUI extends JFrame {

    // Cards / telas
    private static final String SCREEN_LOBBY = "LOBBY";
    private static final String SCREEN_QUESTION = "QUESTION";
    private static final String SCREEN_FINAL = "FINAL";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    // Tela Lobby
    private JPanel lobbyPanel;

    // Tela Pergunta
    private final JPanel questionPanel;
    private final JLabel questionLabel;
    private final JButton[] optionButtons; // em vez de RadioButtons
    private int selectedOption = -1;
    private final JLabel timerLabel;
    private final JLabel scoreLabel;
    private int timeLeft;
    private Timer countdownTimer;

    // Tela Final
    private JPanel finalPanel;
    private JLabel finalScoreLabel;

    private AnswerSender answerSender;
    private boolean answered;

    // Cores minimalistas
    private static final Color PRIMARY_COLOR = new Color(50, 150, 200);      // Azul
    private static final Color HOVER_COLOR = new Color(70, 170, 220);        // Azul claro
    private static final Color SELECTED_COLOR = new Color(100, 200, 100);    // Verde
    private static final Color WRONG_COLOR = new Color(220, 80, 80);         // Vermelho
    private static final Color BG_COLOR = new Color(245, 245, 247);          // Cinzento muito claro
    private static final Color TEXT_COLOR = new Color(40, 40, 50);           // Cinzento escuro

    public GUI() {
        setTitle("IsKahoot");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(600, 400));
        setLocationRelativeTo(null);
        setResizable(false);

        // Criar as telas (ordem importa)
        createLobbyScreen();

        // Tela de pergunta (inicializar aqui para usar no construtor)
        questionPanel = new JPanel(new BorderLayout(0, 20));
        questionPanel.setBackground(BG_COLOR);
        questionPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // ===== TOP: Pergunta e Timer =====
        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.setBackground(BG_COLOR);

        questionLabel = new JLabel("", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 26));
        questionLabel.setForeground(TEXT_COLOR);

        timerLabel = new JLabel("30s", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        timerLabel.setForeground(PRIMARY_COLOR);

        topPanel.add(questionLabel, BorderLayout.CENTER);
        topPanel.add(timerLabel, BorderLayout.SOUTH);

        questionPanel.add(topPanel, BorderLayout.NORTH);

        // ===== CENTER: Opções (botões grandes) =====
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 0, 12));
        optionsPanel.setBackground(BG_COLOR);

        optionButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = createOptionButton(i);
            optionsPanel.add(optionButtons[i]);
        }

        questionPanel.add(optionsPanel, BorderLayout.CENTER);

        // ===== BOTTOM: Placar =====
        scoreLabel = new JLabel("Pontuação: --", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        scoreLabel.setForeground(new Color(100, 100, 110));

        questionPanel.add(scoreLabel, BorderLayout.SOUTH);

        cardPanel.add(questionPanel, SCREEN_QUESTION);

        // Tela final
        createFinalScreen();

        add(cardPanel, BorderLayout.CENTER);
        setVisible(true);

        // Estado inicial
        showLobby();
    }

    /* ========== Criação das Telas ========== */

    private void createLobbyScreen() {
        lobbyPanel = new JPanel(new BorderLayout());
        lobbyPanel.setBackground(BG_COLOR);
        lobbyPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel titleLabel = new JLabel("IsKahoot", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(PRIMARY_COLOR);

        JLabel lobbyLabel = new JLabel("À espera do jogo começar...", SwingConstants.CENTER);
        lobbyLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        lobbyLabel.setForeground(TEXT_COLOR);

        lobbyPanel.add(titleLabel, BorderLayout.NORTH);
        lobbyPanel.add(lobbyLabel, BorderLayout.CENTER);

        cardPanel.add(lobbyPanel, SCREEN_LOBBY);
    }

    private JButton createOptionButton(int index) {
        JButton btn = new JButton();
        btn.setFont(new Font("Arial", Font.PLAIN, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY_COLOR);
        btn.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        int finalIndex = index;
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled() && !answered) {
                    btn.setBackground(HOVER_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!answered && selectedOption != finalIndex) {
                    btn.setBackground(PRIMARY_COLOR);
                }
            }
        });

        btn.addActionListener(e -> selectOption(finalIndex));
        btn.setEnabled(false);

        return btn;
    }

    private void createFinalScreen() {
        finalPanel = new JPanel(new BorderLayout());
        finalPanel.setBackground(BG_COLOR);
        finalPanel.setBorder(new EmptyBorder(60, 40, 60, 40));

        JLabel titleLabel = new JLabel("Resultado Final", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setForeground(PRIMARY_COLOR);

        finalScoreLabel = new JLabel("Pontuação: --", SwingConstants.CENTER);
        finalScoreLabel.setFont(new Font("Arial", Font.PLAIN, 28));
        finalScoreLabel.setForeground(TEXT_COLOR);

        JLabel celebrationLabel = new JLabel("Obrigado por jogar!", SwingConstants.CENTER);
        celebrationLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        celebrationLabel.setForeground(new Color(150, 150, 160));

        finalPanel.add(titleLabel, BorderLayout.NORTH);
        finalPanel.add(finalScoreLabel, BorderLayout.CENTER);
        finalPanel.add(celebrationLabel, BorderLayout.SOUTH);

        cardPanel.add(finalPanel, SCREEN_FINAL);
    }

    /* ========== Métodos Públicos ========== */

    public void showLobby() {
        cardLayout.show(cardPanel, SCREEN_LOBBY);
    }

    public void showQuestionScreen(String questionText, String[] opts, int seconds) {
        cardLayout.show(cardPanel, SCREEN_QUESTION);
        showQuestion(questionText, opts);
        startTimer(seconds);
    }

    public void showFinalScreen(String finalText) {
        finalScoreLabel.setText(finalText);
        cardLayout.show(cardPanel, SCREEN_FINAL);
    }

    public void setAnswerSender(AnswerSender sender) {
        this.answerSender = sender;
    }

    public void setNextSender(NextSender sender) {
        // Não é usado nesta versão minimalista (resposta automática)
    }

    /* ========== Lógica da Tela de Pergunta ========== */

    private void showQuestion(String questionText, String[] opts) {
        // Usa HTML para word wrap em JLabel
        questionLabel.setText("<html><body style='width: 600px'>"
                + questionText + "</body></html>");

        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(opts[i]);
            optionButtons[i].setEnabled(true);
            optionButtons[i].setBackground(PRIMARY_COLOR);
        }

        answered = false;
        selectedOption = -1;
    }

    private void selectOption(int index) {
        if (answered) return;

        // Desselecionar anterior
        if (selectedOption >= 0) {
            optionButtons[selectedOption].setBackground(PRIMARY_COLOR);
        }

        // Selecionar novo
        selectedOption = index;
        optionButtons[index].setBackground(SELECTED_COLOR);

        // Enviar resposta automaticamente (sem botão)
        answered = true;

        // Desabilitar outras opções
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setEnabled(false);
        }

        if (answerSender != null) {
            answerSender.sendAnswer(index);
        }
    }

    public void startTimer(int secondsLeft) {
        timeLeft = secondsLeft;
        updateTimer(timeLeft);

        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        countdownTimer = new Timer(1000, e -> {
            timeLeft--;
            updateTimer(timeLeft);

            if (timeLeft <= 0) {
                countdownTimer.stop();
                for (JButton btn : optionButtons) {
                    btn.setEnabled(false);
                }
            }
        });

        countdownTimer.start();
    }

    public void updateTimer(int secondsLeft) {
        // Cor muda com urgência
        if (secondsLeft <= 5) {
            timerLabel.setForeground(WRONG_COLOR);
        } else if (secondsLeft <= 10) {
            timerLabel.setForeground(new Color(220, 150, 50));
        } else {
            timerLabel.setForeground(PRIMARY_COLOR);
        }

        timerLabel.setText(secondsLeft + "s");
    }

    public void showScoreboard(String text) {
        scoreLabel.setText("Pontuação: " + text);
    }
}