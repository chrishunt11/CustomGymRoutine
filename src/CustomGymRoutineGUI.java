import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;

public class CustomGymRoutineGUI extends JFrame {
    private JTextField muscleField;
    private JTextField difficultyField;
    private JTextField equipmentField;
    private JTextArea resultArea;
    private JLabel muscleLabel;
    private JLabel difficultyLabel;
    private JLabel equipmentLabel;
    private List<String> savedWorkouts;

    public CustomGymRoutineGUI() {
        setTitle("Custom Gym Routine");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Initialize the list to store saved workouts
        savedWorkouts = new ArrayList<>();

        // Header label
        JLabel headerLabel = new JLabel("Create Your Custom Gym Routine", JLabel.CENTER);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(headerLabel, BorderLayout.NORTH);

        // Input Panel (Top Left Corner)
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Workout Preferences"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Muscle Group:"), gbc);

        gbc.gridx = 1;
        muscleField = new JTextField("e.g., Chest", 15);
        addPlaceholderText(muscleField, "e.g., Chest");
        preventEnterKey(muscleField);
        inputPanel.add(muscleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Difficulty:"), gbc);

        gbc.gridx = 1;
        difficultyField = new JTextField("e.g., Beginner", 15);
        addPlaceholderText(difficultyField, "e.g., Beginner");
        preventEnterKey(difficultyField);
        inputPanel.add(difficultyField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Equipment:"), gbc);

        gbc.gridx = 1;
        equipmentField = new JTextField("e.g., Dumbbell", 15);
        addPlaceholderText(equipmentField, "e.g., Dumbbell");
        preventEnterKey(equipmentField);
        inputPanel.add(equipmentField, gbc);

        // Buttons for Generating and Saving Workouts
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton generateButton = new JButton("Generate Workout");
        JButton saveButton = new JButton("Save Workout");
        buttonPanel.add(generateButton);
        buttonPanel.add(saveButton);
        inputPanel.add(buttonPanel, gbc);

        add(inputPanel, BorderLayout.WEST);

        // Summary Panel (Top Middle and Top Right)
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Workout Summary"));

        muscleLabel = new JLabel("Muscle Group: ");
        difficultyLabel = new JLabel("Difficulty: ");
        equipmentLabel = new JLabel("Equipment: ");

        summaryPanel.add(muscleLabel);
        summaryPanel.add(difficultyLabel);
        summaryPanel.add(equipmentLabel);

        add(summaryPanel, BorderLayout.CENTER);

        // Result Area (Bottom Half for Workout Descriptions)
        resultArea = new JTextArea();
        resultArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Available Workouts"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setPreferredSize(new Dimension(780, 300)); // Adjusted width and height

        add(scrollPane, BorderLayout.SOUTH);

        // Generate Workout Button Action
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String muscle = muscleField.getText().trim();
                String difficulty = difficultyField.getText().trim();
                String equipment = equipmentField.getText().trim();

                // Call the method to generate workout
                List<Exercise> customWorkout = CustomGymRoutine.generateCustomWorkout(muscle, difficulty, equipment);

                // Display the workout summary and description in the result area
                if (customWorkout.isEmpty()) {
                    resultArea.setText("No exercises found matching your criteria.");
                    muscleLabel.setText("Muscle Group: ");
                    difficultyLabel.setText("Difficulty: ");
                    equipmentLabel.setText("Equipment: ");
                } else {
                    // Update the summary labels
                    muscleLabel.setText("Muscle Group: " + muscle);
                    difficultyLabel.setText("Difficulty: " + difficulty);
                    equipmentLabel.setText("Equipment: " + equipment);

                    // Build the detailed workout description
                    StringBuilder result = new StringBuilder();
                    for (Exercise exercise : customWorkout) {
                        result.append("Exercise: ").append(exercise.name).append("\n");
                        result.append("Type: ").append(exercise.type).append("\n");
                        result.append("Muscle: ").append(exercise.muscle).append("\n");
                        result.append("Equipment: ").append(exercise.equipment).append("\n");
                        result.append("Difficulty: ").append(exercise.difficulty).append("\n");
                        result.append("Instructions: ").append(exercise.instructions).append("\n\n");
                    }
                    resultArea.setText(result.toString());
                }
            }
        });

        // Save Workout Button Action
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String workout = resultArea.getText();
                if (!workout.isEmpty() && !workout.equals("No exercises found matching your criteria.")) {
                    savedWorkouts.add(workout); // Add workout to saved workouts list
                    JOptionPane.showMessageDialog(null, "Workout saved successfully!");
                } else {
                    JOptionPane.showMessageDialog(null, "No workout to save. Generate a workout first.");
                }
            }
        });
    }

    private void addPlaceholderText(JTextField textField, String placeholder) {
        textField.setForeground(Color.GRAY);
        textField.setText(placeholder);
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText(placeholder);
                }
            }
        });
    }

    private void preventEnterKey(JTextField textField) {
        textField.addActionListener(e -> textField.transferFocus()); // moves focus on pressing Enter
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CustomGymRoutineGUI gui = new CustomGymRoutineGUI();
            gui.setVisible(true);
        });
    }
}
