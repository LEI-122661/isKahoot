// ClientGUI.java
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class ClientGUI extends JFrame {
    private JLabel questionLabel;
    private JRadioButton[] options;
    private ButtonGroup group;
    private JButton submitButton, nextButton;
    private JLabel scoreLabel;

    private List<Question> questions;
    private int currentIndex = 0;
    private int totalScore;

    public ClientGUI() {
        setTitle("IsKahoot");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(new Dimension(500, 300));
        setLocationRelativeTo(null);

        try {
            questions = QuestionLoader.loadFromJson("perguntas.json");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar perguntas: " + e.getMessage());
            System.exit(1);
        }

        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        add(mainPanel, BorderLayout.CENTER);

        questionLabel = new JLabel("", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(questionLabel, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        mainPanel.add(optionsPanel, BorderLayout.CENTER);

        group = new ButtonGroup();
        options = new JRadioButton[4];
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            group.add(options[i]);
            optionsPanel.add(options[i]);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout());
        submitButton = new JButton("Submeter");
        nextButton = new JButton("Próxima Pergunta");
        nextButton.setEnabled(false);
        buttonPanel.add(submitButton);
        buttonPanel.add(nextButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Eventos
        submitButton.addActionListener(e -> handleSubmit());
        nextButton.addActionListener(e -> showNextQuestion());


        //SCORE
        JPanel pointsPanel = new JPanel(new BorderLayout());
        scoreLabel = new JLabel("Pontos: 0");
        pointsPanel.setBorder(new EmptyBorder(0, 0, 0, 10));
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        pointsPanel.add(scoreLabel, BorderLayout.WEST);
        add(pointsPanel, BorderLayout.EAST);

        // Mostrar a primeira pergunta
        showQuestion(currentIndex);

        setVisible(true);
    }

    private void showQuestion(int index) {
        if (index >= questions.size()) {
            JOptionPane.showMessageDialog(this, "Fim do quiz! 🎉");
            submitButton.setEnabled(false);
            nextButton.setEnabled(false);
            return;
        }

        Question q = questions.get(index);
        questionLabel.setText((index + 1) + ". " + q.getQuestion());

        for (int i = 0; i < q.getOptions().size(); i++) {
            options[i].setText(q.getOptions().get(i));
            options[i].setSelected(false);
        }

        group.clearSelection();
        submitButton.setEnabled(true);
        nextButton.setEnabled(false);
    }

    private void handleSubmit() {
        Question q = questions.get(currentIndex);
        int selected = -1;

        for (int i = 0; i < options.length; i++) {
            if (options[i].isSelected()) {
                selected = i + 1; // +1 porque o JSON usa 1-based index
                break;
            }
        }

        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Escolhe uma opção primeiro!");
            return;
        }

        if (selected == q.getCorrect()) {
            totalScore+= q.getPoints();
            scoreLabel.setText("Pontos: " + totalScore);
            JOptionPane.showMessageDialog(this, "Correto! +" + q.getPoints() + " pontos");
        } else {
            JOptionPane.showMessageDialog(this, "Errado! A resposta certa era: " + q.getOptions().get(q.getCorrect() - 1));
        }

        submitButton.setEnabled(false);
        nextButton.setEnabled(true);
    }

    private void showNextQuestion() {
        currentIndex++;
        showQuestion(currentIndex);
    }
}
