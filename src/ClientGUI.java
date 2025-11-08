// ClientGUI.java
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class ClientGUI extends JFrame {
    private JLabel questionLabel;
    private JRadioButton[] options;
    private ButtonGroup group;
    private JButton submitButton;

    private List<Question> questions;
    private int currentIndex = 0;

    public ClientGUI() {
        setTitle("IsKahoot");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        try {
            questions = QuestionLoader.loadFromJson("perguntas.json");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar perguntas: " + e.getMessage());
            System.exit(1);
        }

        // Mostrar a primeira pergunta
        Question q = questions.get(currentIndex);
        questionLabel = new JLabel(q.getQuestion());
        add(questionLabel, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel(new GridLayout(q.getOptions().size(), 1));
        group = new ButtonGroup();
        options = new JRadioButton[q.getOptions().size()];

        for (int i = 0; i < q.getOptions().size(); i++) {
            options[i] = new JRadioButton(q.getOptions().get(i));
            group.add(options[i]);
            optionsPanel.add(options[i]);
        }

        add(optionsPanel, BorderLayout.CENTER);

        submitButton = new JButton("Submeter");
        submitButton.addActionListener(e -> {
            for (int i = 0; i < options.length; i++) {
                if (options[i].isSelected()) {
                    JOptionPane.showMessageDialog(this, "Respondeste: " + q.getOptions().get(i));
                    break;
                }
            }
        });

        add(submitButton, BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }
}
