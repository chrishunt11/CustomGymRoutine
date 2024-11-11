import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomGymRoutineApp extends JFrame {

    private JTabbedPane tabbedPane;
    private JPanel workoutGeneratorPanel, savedWorkoutsPanel, loggedWorkoutsPanel;
    private JComboBox<String> muscleComboBox, difficultyComboBox, typeComboBox;
    private JTextField workoutNameField;
    private JPanel resultPanel, exerciseInputPanel;
    private JTextArea workoutDetailsArea;
    private List<JCheckBox> workoutCheckBoxes;
    private List<JCheckBox> selectedWorkoutCheckBoxes;
    private Map<String, List<Map<String, String>>> savedWorkoutGroups;
    private Map<String, List<String>> loggedWorkouts;
    private JList<String> savedWorkoutList, loggedWorkoutList;
    private String selectedWorkoutName;
    private List<ExerciseInput> exerciseInputs;

    public CustomGymRoutineApp() {
        setTitle("Custom Gym Routine");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize lists and maps for workouts
        workoutCheckBoxes = new ArrayList<>();
        selectedWorkoutCheckBoxes = new ArrayList<>();
        savedWorkoutGroups = new LinkedHashMap<>();
        loggedWorkouts = new LinkedHashMap<>();

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

        // Create each panel (tab) and add to the tabbed pane
        workoutGeneratorPanel = createWorkoutGeneratorPanel();
        savedWorkoutsPanel = createSavedWorkoutsPanel();
        loggedWorkoutsPanel = createLoggedWorkoutsPanel();

        tabbedPane.addTab("Workout Generator", workoutGeneratorPanel);
        tabbedPane.addTab("Saved Workouts", savedWorkoutsPanel);
        tabbedPane.addTab("Logged Workouts", loggedWorkoutsPanel);

        add(tabbedPane);
        setVisible(true);
    }

    // Panel to generate workouts by choosing options
    private JPanel createWorkoutGeneratorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Input section to pick workout details
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Enter Workout Details"));

        // Dropdowns for selecting workout options
        typeComboBox = new JComboBox<>(new String[]{"", "cardio", "olympic_weightlifting", "plyometrics", "powerlifting", "strength", "stretching", "strongman"});
        muscleComboBox = new JComboBox<>(new String[]{"", "abdominals", "abductors", "adductors", "biceps", "calves", "chest", "forearms", "glutes", "hamstrings", "lats", "lower_back", "middle_back", "neck", "quadriceps", "traps", "triceps"});
        difficultyComboBox = new JComboBox<>(new String[]{"", "beginner", "intermediate", "expert"});
        workoutNameField = new JTextField();

        // Add labels and input fields to the panel
        inputPanel.add(createLabel("Exercise Type:"));
        inputPanel.add(typeComboBox);
        inputPanel.add(createLabel("Muscle Group:"));
        inputPanel.add(muscleComboBox);
        inputPanel.add(createLabel("Difficulty:"));
        inputPanel.add(difficultyComboBox);
        inputPanel.add(createLabel("Workout Name:"));
        inputPanel.add(workoutNameField);

        // Buttons to generate and save workouts
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton generateButton = createButton("Generate Workout");
        JButton saveButton = createButton("Save Selected as Custom Workout");

        generateButton.addActionListener(e -> fetchWorkoutData());
        saveButton.addActionListener(e -> saveCustomWorkout());

        buttonPanel.add(generateButton);
        buttonPanel.add(saveButton);

        // Panel to show the generated workouts
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        JScrollPane resultScrollPane = new JScrollPane(resultPanel);
        resultScrollPane.setBorder(BorderFactory.createTitledBorder("Generated Workouts"));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(resultScrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Panel to manage saved workouts
    private JPanel createSavedWorkoutsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        savedWorkoutList = new JList<>();
        savedWorkoutList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        savedWorkoutList.addListSelectionListener(e -> displayWorkoutExercises());

        JScrollPane listScrollPane = new JScrollPane(savedWorkoutList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("Saved Custom Workouts"));
        listScrollPane.setPreferredSize(new Dimension(250, 0));

        exerciseInputPanel = new JPanel();
        exerciseInputPanel.setLayout(new BoxLayout(exerciseInputPanel, BoxLayout.Y_AXIS));
        JScrollPane exerciseInputScrollPane = new JScrollPane(exerciseInputPanel);
        exerciseInputScrollPane.setBorder(BorderFactory.createTitledBorder("Exercise Details"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton logWorkoutButton = createButton("Log Workout");
        logWorkoutButton.addActionListener(e -> logWorkoutDetails());
        buttonPanel.add(logWorkoutButton);

        panel.add(listScrollPane, BorderLayout.WEST);
        panel.add(exerciseInputScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Panel to view logged workouts
    private JPanel createLoggedWorkoutsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        loggedWorkoutList = new JList<>();
        loggedWorkoutList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loggedWorkoutList.addListSelectionListener(e -> displayLoggedWorkoutDetails(loggedWorkoutList.getSelectedValue()));

        // Adjust the preferred size of the scroll pane to increase the list's width
        JScrollPane listScrollPane = new JScrollPane(loggedWorkoutList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("Logged Workouts"));
        listScrollPane.setPreferredSize(new Dimension(300, 0)); // Increase the width here (e.g., 300 pixels)

        workoutDetailsArea = new JTextArea(15, 30);
        workoutDetailsArea.setEditable(false);
        JScrollPane detailsScrollPane = new JScrollPane(workoutDetailsArea);
        detailsScrollPane.setBorder(BorderFactory.createTitledBorder("Workout Details"));

        updateLoggedWorkoutList();

        panel.add(listScrollPane, BorderLayout.WEST);       // Left: Logged workout names
        panel.add(detailsScrollPane, BorderLayout.CENTER);  // Right: Selected workout details

        return panel;
    }

    // Fetch data from API based on selected options, with flexibility for optional fields
    private void fetchWorkoutData() {
        String type = (String) typeComboBox.getSelectedItem();
        String muscle = (String) muscleComboBox.getSelectedItem();
        String difficulty = (String) difficultyComboBox.getSelectedItem();

        // Build the URL with only selected fields
        StringBuilder apiUrl = new StringBuilder("https://api.api-ninjas.com/v1/exercises?");

        if (type != null && !type.isEmpty()) {
            apiUrl.append("type=").append(type).append("&");
        }
        if (muscle != null && !muscle.isEmpty()) {
            apiUrl.append("muscle=").append(muscle).append("&");
        }
        if (difficulty != null && !difficulty.isEmpty()) {
            apiUrl.append("difficulty=").append(difficulty).append("&");
        }

        // Remove trailing '&' if present
        if (apiUrl.charAt(apiUrl.length() - 1) == '&') {
            apiUrl.setLength(apiUrl.length() - 1);
        }

        try {
            URL url = new URL(apiUrl.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Api-Key", "ViDZePNbkVmheKpy62fxYQ==xIjZAovy9dXD0DR5");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            connection.disconnect();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(content.toString());

            resultPanel.removeAll();
            workoutCheckBoxes.clear();

            // Iterate through each exercise in the response
            for (JsonNode node : root) {
                String name = node.get("name").asText();
                String exerciseType = node.has("type") ? node.get("type").asText() : "N/A"; // Include type if available
                String muscleGroup = node.get("muscle").asText();
                String equipmentRequired = node.get("equipment").asText();

                // Updated description to include Type information
                String workout = String.format("Exercise: %s\nType: %s\nMuscle: %s\nEquipment: %s\n", name, exerciseType, muscleGroup, equipmentRequired);

                JCheckBox checkBox = new JCheckBox("<html>" + workout.replace("\n", "<br>") + "</html>");
                checkBox.addActionListener(e -> {
                    if (checkBox.isSelected() && !selectedWorkoutCheckBoxes.contains(checkBox)) {
                        selectedWorkoutCheckBoxes.add(checkBox);
                    } else if (!checkBox.isSelected()) {
                        selectedWorkoutCheckBoxes.remove(checkBox);
                    }
                });

                resultPanel.add(checkBox);
                workoutCheckBoxes.add(checkBox);
            }

            resultPanel.revalidate();
            resultPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch data from API.", "API Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    // Save a custom workout with selected exercises
    private void saveCustomWorkout() {
        String workoutName = workoutNameField.getText().trim();

        if (workoutName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a name for your workout.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Map<String, String>> selectedExercises = new ArrayList<>();
        for (JCheckBox checkBox : selectedWorkoutCheckBoxes) {
            Map<String, String> exerciseMap = new LinkedHashMap<>();

            // Extract exercise name and type from the checkbox text
            String[] lines = checkBox.getText().replace("<html>", "").replace("</html>", "").split("<br>");
            String exerciseName = lines[0].replace("Exercise: ", "").trim();
            String exerciseType = lines[1].replace("Type: ", "").trim();

            exerciseMap.put("exercise", exerciseName);
            exerciseMap.put("type", exerciseType);
            selectedExercises.add(exerciseMap);
        }

        if (selectedExercises.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No exercises selected. Please select exercises to save.", "Save Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        savedWorkoutGroups.put(workoutName, selectedExercises);
        updateSavedWorkoutList();
        JOptionPane.showMessageDialog(this, "Workout saved successfully as " + workoutName + "!", "Save Confirmation", JOptionPane.INFORMATION_MESSAGE);

        workoutNameField.setText("");
        selectedWorkoutCheckBoxes.clear();
    }


    // Update the saved workouts list display
    private void updateSavedWorkoutList() {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String workoutName : savedWorkoutGroups.keySet()) {
            listModel.addElement(workoutName);
        }
        savedWorkoutList.setModel(listModel);
    }

    // Display exercises for a selected saved workout
    private void displayWorkoutExercises() {
        selectedWorkoutName = savedWorkoutList.getSelectedValue();
        exerciseInputPanel.removeAll();
        exerciseInputs = new ArrayList<>();

        if (selectedWorkoutName != null) {
            List<Map<String, String>> exercises = savedWorkoutGroups.get(selectedWorkoutName);

            for (Map<String, String> exerciseMap : exercises) {
                String exercise = exerciseMap.get("exercise");
                String type = exerciseMap.get("type");
                ExerciseInput input = new ExerciseInput(exercise, type);
                exerciseInputs.add(input);
                exerciseInputPanel.add(input.getPanel());
            }

            exerciseInputPanel.revalidate();
            exerciseInputPanel.repaint();
        }
    }

    // Log the selected workout details with timestamp
    private void logWorkoutDetails() {
        if (selectedWorkoutName == null) {
            JOptionPane.showMessageDialog(this, "Please select a workout group to log details.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String> workoutDetails = new ArrayList<>();
        for (ExerciseInput input : exerciseInputs) {
            workoutDetails.add(input.getExerciseDetails());
        }

        String date = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
        String logEntryName = selectedWorkoutName + " (" + date + ")";

        loggedWorkouts.put(logEntryName, workoutDetails);
        updateLoggedWorkoutList();

        JOptionPane.showMessageDialog(this, "Workout logged successfully!", "Log Confirmation", JOptionPane.INFORMATION_MESSAGE);
    }

    // Update the list of logged workouts display
    private void updateLoggedWorkoutList() {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String workoutName : loggedWorkouts.keySet()) {
            listModel.addElement(workoutName);
        }
        loggedWorkoutList.setModel(listModel);
    }

    // Display detailed information for the selected logged workout
    private void displayLoggedWorkoutDetails(String workoutName) {
        if (workoutName != null && loggedWorkouts.containsKey(workoutName)) {
            StringBuilder details = new StringBuilder("Workout: " + workoutName).append("\n------------------------------------\n");

            for (String exerciseDetail : loggedWorkouts.get(workoutName)) {
                details.append(exerciseDetail.replace("Type:", "").replace("Difficulty:", ""))
                        .append("\n------------------------------------\n");
            }
            workoutDetailsArea.setText(details.toString());
        } else {
            workoutDetailsArea.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CustomGymRoutineApp::new);
    }

    // Helper to create a JLabel with right alignment
    private JLabel createLabel(String text) {
        return new JLabel(text, JLabel.RIGHT);
    }

    // Helper to create a JButton with specified text
    private JButton createButton(String text) {
        return new JButton(text);
    }
}

// Class to represent individual exercise input for logging fields based on type
class ExerciseInput {
    private String exercise;
    private JTextField setsField, repsField, weightField, durationField;
    private JLabel repsLabel, weightLabel, durationLabel;

    public ExerciseInput(String exercise, String type) {
        this.exercise = exercise;
        setsField = new JTextField(5);

        // Check if type is cardio or stretching, then set up fields accordingly
        if (type.equals("cardio") || type.equals("stretching")) {
            // Only sets and duration fields for cardio/stretching
            durationField = new JTextField(5);
            durationLabel = new JLabel("Duration (minutes):");
        } else {
            // Sets, reps, and weight fields for other types
            repsField = new JTextField(5);
            weightField = new JTextField(5);
            repsLabel = new JLabel("Reps:");
            weightLabel = new JLabel("Weight:");
        }
    }

    // Create panel for input fields based on type
    public JPanel getPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.add(new JLabel("<html>" + exercise.replace("\n", "<br>") + "</html>"));
        panel.add(new JLabel("Sets:"));
        panel.add(setsField);

        // Add fields based on type
        if (durationField != null) { // Cardio/stretching: add duration only
            panel.add(durationLabel);
            panel.add(durationField);
        } else { // Other types: add reps and weight fields
            panel.add(repsLabel);
            panel.add(repsField);
            panel.add(weightLabel);
            panel.add(weightField);
        }
        return panel;
    }

    // Generate exercise details based on the type of fields
    public String getExerciseDetails() {
        String details = "Sets: " + setsField.getText();
        if (durationField != null) {
            details += "  Duration: " + durationField.getText();
        } else {
            details += "  Reps: " + repsField.getText() + "  Weight: " + weightField.getText();
        }
        return exercise + "\n" + details;
    }
}
