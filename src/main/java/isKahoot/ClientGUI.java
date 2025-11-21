package isKahoot;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.security.PublicKey;

public class ClientGUI extends JFrame {

    private JLabel questionLabel;
    private JRadioButton[] options;
    private ButtonGroup group;
    private JButton submitButton;
    private JButton nextButton;
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private Timer countdownTimer;
    private int timeLeft;

    private AnswerSender answerSender;
    private NextSender nextSender;

    private boolean answered;


    public ClientGUI() {
        setTitle("IsKahoot");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(new Dimension(500, 300));
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10,10));
        mainPanel.setBorder(new EmptyBorder(10,10,10,10));
        add(mainPanel, BorderLayout.CENTER);

        questionLabel = new JLabel("", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(questionLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(4,1,5,5));
        options = new JRadioButton[4];
        group = new ButtonGroup();

        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton("");
            options[i].setFont(new Font("Arial", Font.BOLD, 14));
            group.add(options[i]);
            centerPanel.add(options[i]);
        }
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(1,4,5,5));
        timerLabel = new JLabel("Tempo: --", SwingConstants.CENTER);

        submitButton = new JButton("Submeter");
        submitButton.addActionListener(e -> handleSubmit());

        nextButton = new JButton("Avançar");
        nextButton.addActionListener(e -> handleNext());

        scoreLabel = new JLabel("Pontuação: --", SwingConstants.CENTER);

        bottomPanel.add(timerLabel);
        bottomPanel.add(submitButton);
        bottomPanel.add(nextButton);
        bottomPanel.add(scoreLabel);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);

        setOptionsEnabled(false);
        submitButton.setEnabled(false);
        nextButton.setEnabled(false);
    }

    public void setAnswerSender(AnswerSender sender) {
        this.answerSender = sender;
    }

    public void setNextSender(NextSender sender) {
        this.nextSender = sender;
    }

    public void showQuestion(String questionText, String[] opts) {
        questionLabel.setText(questionText);

        group.clearSelection();
        for (int i = 0; i < 4; i++) {
            options[i].setText(opts[i]);
        }

        answered = false;
        setOptionsEnabled(true);
        submitButton.setEnabled(true);
        nextButton.setEnabled(false); // só fica ativo depois de submeter
    }

    public void updateTimer(int secondsLeft) {
        timerLabel.setText("Tempo: " + secondsLeft + "s");
    }

    public void startTimer(int secondsLeft){
        timeLeft=secondsLeft;
        updateTimer(timeLeft);

        //timer de outra pergunta ativo?? o tempo vai acabar e passar automaticamente a proxima_??
        if(countdownTimer!=null){
            countdownTimer.stop();
        }

        countdownTimer =new Timer(1000, e -> {
            timeLeft--;
            updateTimer(timeLeft);

            if (timeLeft <=0){
                countdownTimer.stop();

                setOptionsEnabled(false);
                submitButton.setEnabled(false);
                nextButton.setEnabled(true);
            }
        });
        countdownTimer.start();

    }

    public void showScoreboard(String text) {
        scoreLabel.setText(text);
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

        // bloqueia mudança da resposta
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
