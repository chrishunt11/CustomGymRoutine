import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomGymRoutineApp extends JFrame {

    private JTabbedPane tabbedPane;
    private JPanel workoutGeneratorPanel, savedWorkoutsPanel, workoutLogPanel;
    private JTextField muscleField, difficultyField, equipmentField;
    private JTextArea resultArea, savedWorkoutsArea, logArea;
    private List<String> generatedWorkouts;  // Add this line
    private List<String> savedWorkouts;

    public CustomGymRoutineApp() {
        setTitle("Custom Gym Routine");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        generatedWorkouts = new ArrayList<>();  // Initialize generatedWorkouts
        savedWorkouts = new ArrayList<>();

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

        workoutGeneratorPanel = createWorkoutGeneratorPanel();
        savedWorkoutsPanel = createSavedWorkoutsPanel();
        workoutLogPanel = createWorkoutLogPanel();

        tabbedPane.addTab("Workout Generator", workoutGeneratorPanel);
        tabbedPane.addTab("Saved Workouts", savedWorkoutsPanel);
        tabbedPane.addTab("Workout Log", workoutLogPanel);

        add(tabbedPane);
        setVisible(true);
    }

    private JPanel createWorkoutGeneratorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Enter Workout Details"));
        muscleField = new JTextField();
        difficultyField = new JTextField();
        equipmentField = new JTextField();

        inputPanel.add(new JLabel("Muscle Group:", JLabel.RIGHT));
        inputPanel.add(muscleField);
        inputPanel.add(new JLabel("Difficulty:", JLabel.RIGHT));
        inputPanel.add(difficultyField);
        inputPanel.add(new JLabel("Equipment:", JLabel.RIGHT));
        inputPanel.add(equipmentField);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton generateButton = new JButton("Generate Workout");
        JButton saveButton = new JButton("Save Selected Workouts");

        generateButton.addActionListener(e -> fetchWorkoutData());
        saveButton.addActionListener(e -> saveSelectedWorkouts());

        buttonPanel.add(generateButton);
        buttonPanel.add(saveButton);

        // Result Area
        resultArea = new JTextArea(10, 40);
        resultArea.setFont(new Font("Serif", Font.PLAIN, 14));
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Generated Workouts"));

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSavedWorkoutsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        savedWorkoutsArea = new JTextArea(15, 40);
        savedWorkoutsArea.setFont(new Font("Serif", Font.PLAIN, 14));
        savedWorkoutsArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(savedWorkoutsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Saved Workouts"));

        JButton backButton = new JButton("Back to Workout Generator");
        backButton.addActionListener(e -> tabbedPane.setSelectedIndex(0));

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(backButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createWorkoutLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        logArea = new JTextArea(15, 40);
        logArea.setFont(new Font("Serif", Font.PLAIN, 14));
        logArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Workout Log"));

        JButton logWorkoutButton = new JButton("Log Workout Details");
        logWorkoutButton.addActionListener(e -> logWorkoutDetails());

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(logWorkoutButton, BorderLayout.SOUTH);

        return panel;
    }

    private void fetchWorkoutData() {
        String muscle = muscleField.getText().trim();
        String difficulty = difficultyField.getText().trim();
        String equipment = equipmentField.getText().trim();

        if (muscle.isEmpty() || difficulty.isEmpty() || equipment.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Prepare API URL and Connection
            String apiUrl = "https://api.api-ninjas.com/v1/exercises?muscle=" + muscle;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Api-Key", "YOUR_API_KEY");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            connection.disconnect();

            // Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(content.toString());

            resultArea.setText(""); // Clear previous results
            generatedWorkouts.clear();

            for (JsonNode node : root) {
                String name = node.get("name").asText();
                String type = node.get("type").asText();
                String muscleGroup = node.get("muscle").asText();
                String equipmentRequired = node.get("equipment").asText();
                String difficultyLevel = node.get("difficulty").asText();
                String instructions = node.get("instructions").asText();

                // Format and add each exercise to result area and generatedWorkouts list
                String workout = String.format("Exercise: %s\nType: %s\nMuscle: %s\nEquipment: %s\nDifficulty: %s\nInstructions: %s\n\n",
                        name, type, muscleGroup, equipmentRequired, difficultyLevel, instructions);

                resultArea.append(workout);
                generatedWorkouts.add(workout);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch data from API.", "API Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveSelectedWorkouts() {
        if (generatedWorkouts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No workouts generated to save. Please generate a workout first.", "Save Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        savedWorkouts.addAll(generatedWorkouts);
        updateSavedWorkoutsArea();
        JOptionPane.showMessageDialog(this, "Selected workouts saved successfully!", "Save Confirmation", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateSavedWorkoutsArea() {
        savedWorkoutsArea.setText("");
        for (String workout : savedWorkouts) {
            savedWorkoutsArea.append(workout + "\n");
        }
    }

    private void logWorkoutDetails() {
        if (savedWorkouts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No saved workouts to log. Please save workouts first.", "Log Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder logEntry = new StringBuilder();
        for (String workout : savedWorkouts) {
            logEntry.append(workout)
                    .append("Sets: [  ]  Reps: [  ]  Weight: [  ]\n\n");
        }

        logArea.append(logEntry.toString());
        JOptionPane.showMessageDialog(this, "Workout details logged successfully!", "Log Confirmation", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CustomGymRoutineApp::new);
    }
}
