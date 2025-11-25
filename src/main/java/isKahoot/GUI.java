package isKahoot;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GUI extends JFrame {

    // Cards / telas
    private static final String SCREEN_LOBBY = "LOBBY";
    private static final String SCREEN_QUESTION = "QUESTION";
    private static final String SCREEN_FINAL = "FINAL";

    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel = new JPanel(cardLayout);

    // Tela Lobby
    private JPanel lobbyPanel;
    private JLabel lobbyLabel;

    // Tela Pergunta (a tua UI atual)
    private JPanel questionPanel;
    private JLabel questionLabel;
    private JRadioButton[] options;
    private ButtonGroup group;
    private JButton submitButton;
    private JButton nextButton;
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private int timeLeft;
    private Timer countdownTimer;

    // Tela Final / Scoreboard
    private JPanel finalPanel;
    private JLabel finalScoreLabel;

    private AnswerSender answerSender;
    private NextSender nextSender;
    private boolean answered;

    public GUI() {
        setTitle("IsKahoot");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(600, 400));
        setLocationRelativeTo(null);

        // criar as três telas
        createLobbyScreen();
        createQuestionScreen();
        createFinalScreen();

        add(cardPanel, BorderLayout.CENTER);
        setVisible(true);

        // estado inicial
        showLobby();
    }

    /* ---------- Criação das telas ---------- */

    private void createLobbyScreen() {
        lobbyPanel = new JPanel(new BorderLayout());
        lobbyPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        lobbyLabel = new JLabel("À espera do jogo começar...", SwingConstants.CENTER);
        lobbyLabel.setFont(new Font("Arial", Font.BOLD, 20));

        lobbyPanel.add(lobbyLabel, BorderLayout.CENTER);

        cardPanel.add(lobbyPanel, SCREEN_LOBBY);
    }

    private void createQuestionScreen() {
        questionPanel = new JPanel(new BorderLayout(10, 10));
        questionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        questionLabel = new JLabel("", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        questionPanel.add(questionLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        options = new JRadioButton[4];
        group = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton("");
            options[i].setFont(new Font("Arial", Font.BOLD, 14));
            group.add(options[i]);
            centerPanel.add(options[i]);
        }
        questionPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        timerLabel = new JLabel("Tempo: --", SwingConstants.CENTER);
        scoreLabel = new JLabel("Pontuação: --", SwingConstants.CENTER);

        submitButton = new JButton("Submeter");
        submitButton.addActionListener(e -> handleSubmit());

        nextButton = new JButton("Avançar");
        nextButton.addActionListener(e -> handleNext());

        bottomPanel.add(timerLabel);
        bottomPanel.add(submitButton);
        bottomPanel.add(nextButton);
        bottomPanel.add(scoreLabel);

        questionPanel.add(bottomPanel, BorderLayout.SOUTH);

        setOptionsEnabled(false);
        submitButton.setEnabled(false);
        nextButton.setEnabled(false);

        cardPanel.add(questionPanel, SCREEN_QUESTION);
    }

    private void createFinalScreen() {
        finalPanel = new JPanel(new BorderLayout());
        finalPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Resultado Final", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));

        finalScoreLabel = new JLabel("Pontuação: --", SwingConstants.CENTER);
        finalScoreLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        finalPanel.add(title, BorderLayout.NORTH);
        finalPanel.add(finalScoreLabel, BorderLayout.CENTER);

        cardPanel.add(finalPanel, SCREEN_FINAL);
    }

    /* ---------- Métodos para o GameHandler/Client usar ---------- */

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
        this.nextSender = sender;
    }

    /* ---------- Lógica daaaa tela de pergunta ---------- */

    public void showQuestion(String questionText, String[] opts) {
        questionLabel.setText(questionText);
        group.clearSelection();
        for (int i = 0; i < 4; i++) {
            options[i].setText(opts[i]);
            options[i].setVisible(true);
        }

        answered = false;
        setOptionsEnabled(true);
        submitButton.setEnabled(true);
        nextButton.setEnabled(false);
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
                setOptionsEnabled(false);
                submitButton.setEnabled(false);
                nextButton.setEnabled(true);
            }
        });
        countdownTimer.start();
    }

    public void updateTimer(int secondsLeft) {
        timerLabel.setText("Tempo: " + secondsLeft + "s");
    }

    public void showScoreboard(String text) {
        scoreLabel.setText(text);
    }

    public void removeButtons() {
        submitButton.setVisible(false);
        nextButton.setVisible(false);
        for (JRadioButton rb : options) {
            rb.setVisible(false);
        }
    }

    private void handleSubmit() {
        if (answered) return;

        int selected = -1;
        for (int i = 0; i < 4; i++) {
            if (options[i].isSelected()) {
                selected = i;
                break;
            }
        }

        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Escolhe uma opção!");
            return;
        }

        answered = true;
        setOptionsEnabled(false);
        submitButton.setEnabled(false);
        nextButton.setEnabled(true);

        if (answerSender != null) {
            answerSender.sendAnswer(selected);
        }
    }

    private void handleNext() {
        if (!answered) return;
        nextButton.setEnabled(false);
        if (nextSender != null) {
            nextSender.nextQuestion();
        }
    }

    private void setOptionsEnabled(boolean enabled) {
        for (JRadioButton rb : options) rb.setEnabled(enabled);
    }
}
