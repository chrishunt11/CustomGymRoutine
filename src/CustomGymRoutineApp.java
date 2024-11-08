import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CustomGymRoutineApp extends JFrame {
    private JTabbedPane tabbedPane;
    private JPanel workoutGeneratorPanel;
    private JPanel savedWorkoutsPanel;
    private JPanel workoutLogPanel;

    public CustomGymRoutineApp() {
        // Set up the frame
        setTitle("Custom Gym Routine");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize the tabbed pane
        tabbedPane = new JTabbedPane();

        // Create tabs
        workoutGeneratorPanel = createWorkoutGeneratorPanel();
        savedWorkoutsPanel = createSavedWorkoutsPanel();
        workoutLogPanel = createWorkoutLogPanel();

        // Add tabs to the tabbed pane
        tabbedPane.addTab("Workout Generator", workoutGeneratorPanel);
        tabbedPane.addTab("Saved Workouts", savedWorkoutsPanel);
        tabbedPane.addTab("Workout Log", workoutLogPanel);

        // Add tabbed pane to frame
        add(tabbedPane);
    }

    private JPanel createWorkoutGeneratorPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Top input section
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.add(new JLabel("Muscle Group:"));
        JTextField muscleField = new JTextField();
        inputPanel.add(muscleField);

        inputPanel.add(new JLabel("Difficulty:"));
        JTextField difficultyField = new JTextField();
        inputPanel.add(difficultyField);

        inputPanel.add(new JLabel("Equipment:"));
        JTextField equipmentField = new JTextField();
        inputPanel.add(equipmentField);

        // Button to generate workout
        JButton generateButton = new JButton("Generate Workout");
        JButton saveButton = new JButton("Save Workout");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(generateButton);
        buttonPanel.add(saveButton);

        // Result area
        JTextArea resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Add components to main panel
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);

        // Button actions (simplified without API call)
        generateButton.addActionListener(e -> resultArea.setText("Generated Workout for " + muscleField.getText()));
        saveButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Workout Saved!"));

        return panel;
    }

    private JPanel createSavedWorkoutsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea savedWorkoutsArea = new JTextArea();
        savedWorkoutsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(savedWorkoutsArea);
        savedWorkoutsArea.setText("Your saved workouts will appear here.");

        JButton backButton = new JButton("Back to Workout Generator");
        backButton.addActionListener(e -> tabbedPane.setSelectedIndex(0)); // Switch to Workout Generator tab

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(backButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createWorkoutLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        logArea.setText("Log your workout progress here.");

        JButton saveLogButton = new JButton("Save Log");
        saveLogButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Workout Log Saved!"));

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(saveLogButton, BorderLayout.SOUTH);

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CustomGymRoutineApp app = new CustomGymRoutineApp();
            app.setVisible(true);
        });
    }
}
