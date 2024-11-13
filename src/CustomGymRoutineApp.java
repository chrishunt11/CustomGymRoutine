import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * CustomGymRoutineApp class is a Swing-based GUI application to manage custom gym routines.
 * It allows users to generate, save, and log workouts using an external API.
 */
public class CustomGymRoutineApp extends JFrame {

    private JTabbedPane tabbedPane;
    private JPanel workoutGeneratorPanel, savedWorkoutsPanel, loggedWorkoutsPanel;
    private JComboBox<String> muscleComboBox, difficultyComboBox, typeComboBox;
    private JTextField workoutNameField;
    private JPanel resultPanel, exerciseInputPanel;
    private JTextArea workoutDetailsArea;
    private java.util.List<JCheckBox> workoutCheckBoxes;
    private java.util.List<JCheckBox> selectedWorkoutCheckBoxes;
    private Map<String, java.util.List<Exercise>> savedWorkoutGroups;
    private LinkedList<Map<String, java.util.List<String>>> loggedWorkoutsList;
    private HashSet<String> uniqueMuscleGroups;
    private JList<String> savedWorkoutList, loggedWorkoutList;
    private String selectedWorkoutName;
    private java.util.List<ExerciseInput> exerciseInputs;

    public CustomGymRoutineApp() {
        setTitle("Custom Gym Routine");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        workoutCheckBoxes = new ArrayList<>();
        selectedWorkoutCheckBoxes = new ArrayList<>();
        savedWorkoutGroups = new LinkedHashMap<>();
        loggedWorkoutsList = new LinkedList<>();
        uniqueMuscleGroups = new HashSet<>();

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

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

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Enter Workout Details"));

        typeComboBox = new JComboBox<>(new String[]{"", "cardio", "strength", "stretching"});
        muscleComboBox = new JComboBox<>(new String[]{"", "abdominals", "biceps", "chest", "glutes", "hamstrings"});
        difficultyComboBox = new JComboBox<>(new String[]{"", "beginner", "intermediate", "expert"});
        workoutNameField = new JTextField();

        inputPanel.add(createLabel("Exercise Type:"));
        inputPanel.add(typeComboBox);
        inputPanel.add(createLabel("Muscle Group:"));
        inputPanel.add(muscleComboBox);
        inputPanel.add(createLabel("Difficulty:"));
        inputPanel.add(difficultyComboBox);
        inputPanel.add(createLabel("Workout Name:"));
        inputPanel.add(workoutNameField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton generateButton = createButton("Generate Workout");
        JButton saveButton = createButton("Save Selected as Custom Workout");

        generateButton.addActionListener(e -> fetchWorkoutData());
        saveButton.addActionListener(e -> saveCustomWorkout());

        buttonPanel.add(generateButton);
        buttonPanel.add(saveButton);

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

        JScrollPane listScrollPane = new JScrollPane(loggedWorkoutList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("Logged Workouts"));
        listScrollPane.setPreferredSize(new Dimension(300, 0));

        workoutDetailsArea = new JTextArea(15, 30);
        workoutDetailsArea.setEditable(false);
        JScrollPane detailsScrollPane = new JScrollPane(workoutDetailsArea);
        detailsScrollPane.setBorder(BorderFactory.createTitledBorder("Workout Details"));

        updateLoggedWorkoutList();

        panel.add(listScrollPane, BorderLayout.WEST);
        panel.add(detailsScrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Fetch data from API based on selected options
    private void fetchWorkoutData() {
        String type = (String) typeComboBox.getSelectedItem();
        String muscle = (String) muscleComboBox.getSelectedItem();
        String difficulty = (String) difficultyComboBox.getSelectedItem();

        StringBuilder apiUrl = new StringBuilder("https://api.api-ninjas.com/v1/exercises?");
        if (type != null && !type.isEmpty()) apiUrl.append("type=").append(type).append("&");
        if (muscle != null && !muscle.isEmpty()) apiUrl.append("muscle=").append(muscle).append("&");
        if (difficulty != null && !difficulty.isEmpty()) apiUrl.append("difficulty=").append(difficulty).append("&");
        if (apiUrl.charAt(apiUrl.length() - 1) == '&') apiUrl.setLength(apiUrl.length() - 1);

        try {
            URL url = new URL(apiUrl.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Api-Key", "ViDZePNbkVmheKpy62fxYQ==xIjZAovy9dXD0DR5");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) content.append(inputLine);
            in.close();
            connection.disconnect();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(content.toString());

            resultPanel.removeAll();
            workoutCheckBoxes.clear();
            for (JsonNode node : root) {
                String name = node.get("name").asText();
                String exerciseType = node.has("type") ? node.get("type").asText() : "N/A";
                String muscleGroup = node.get("muscle").asText();
                String equipmentRequired = node.get("equipment").asText();

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

        java.util.List<Exercise> selectedExercises = new ArrayList<>();
        for (JCheckBox checkBox : selectedWorkoutCheckBoxes) {
            String[] lines = checkBox.getText().replace("<html>", "").replace("</html>", "").split("<br>");
            String exerciseName = lines[0].replace("Exercise: ", "").trim();
            String exerciseType = lines[1].replace("Type: ", "").trim();

            if (exerciseType.equalsIgnoreCase("cardio")) {
                selectedExercises.add(new CardioExercise(exerciseName, exerciseType, 30)); // example duration
            } else {
                selectedExercises.add(new StrengthExercise(exerciseName, exerciseType, 10, 50)); // example reps and weight
            }
        }

        savedWorkoutGroups.put(workoutName, selectedExercises);
        updateSavedWorkoutList();
        JOptionPane.showMessageDialog(this, "Workout saved successfully as " + workoutName + "!", "Save Confirmation", JOptionPane.INFORMATION_MESSAGE);

        workoutNameField.setText("");
        selectedWorkoutCheckBoxes.clear();
    }

    private void updateSavedWorkoutList() {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String workoutName : savedWorkoutGroups.keySet()) listModel.addElement(workoutName);
        savedWorkoutList.setModel(listModel);
    }

    private void displayWorkoutExercises() {
        selectedWorkoutName = savedWorkoutList.getSelectedValue();
        exerciseInputPanel.removeAll();
        exerciseInputs = new ArrayList<>();
        if (selectedWorkoutName != null) {
            java.util.List<Exercise> exercises = savedWorkoutGroups.get(selectedWorkoutName);
            for (Exercise exercise : exercises) {
                ExerciseInput input = new ExerciseInput(exercise);
                exerciseInputs.add(input);
                exerciseInputPanel.add(input.getPanel());
            }
            exerciseInputPanel.revalidate();
            exerciseInputPanel.repaint();
        }
    }

    private void logWorkoutDetails() {
        if (selectedWorkoutName == null) {
            JOptionPane.showMessageDialog(this, "Please select a workout group to log details.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        java.util.List<String> workoutDetails = new ArrayList<>();
        for (ExerciseInput input : exerciseInputs) {
            workoutDetails.add(input.getExerciseDetails());
        }
        String date = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
        String logEntryName = selectedWorkoutName + " (" + date + ")";
        Map<String, java.util.List<String>> logEntry = new HashMap<>();
        logEntry.put(logEntryName, workoutDetails);
        loggedWorkoutsList.add(logEntry);
        updateLoggedWorkoutList();
        JOptionPane.showMessageDialog(this, "Workout logged successfully!", "Log Confirmation", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateLoggedWorkoutList() {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Map<String, java.util.List<String>> entry : loggedWorkoutsList) {
            for (String workoutName : entry.keySet()) listModel.addElement(workoutName);
        }
        loggedWorkoutList.setModel(listModel);
    }

    private void displayLoggedWorkoutDetails(String workoutName) {
        for (Map<String, java.util.List<String>> entry : loggedWorkoutsList) {
            if (entry.containsKey(workoutName)) {
                StringBuilder details = new StringBuilder("Workout: " + workoutName).append("\n------------------------------------\n");
                for (String exerciseDetail : entry.get(workoutName)) {
                    details.append(exerciseDetail.replace("Type:", "").replace("Difficulty:", ""))
                            .append("\n------------------------------------\n");
                }
                workoutDetailsArea.setText(details.toString());
                return;
            }
        }
        workoutDetailsArea.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CustomGymRoutineApp::new);
    }

    private JLabel createLabel(String text) {
        return new JLabel(text, JLabel.RIGHT);
    }

    private JButton createButton(String text) {
        return new JButton(text);
    }
}

// Abstract class for exercises
abstract class Exercise {
    protected String name;
    protected String muscleGroup;

    public Exercise(String name, String muscleGroup) {
        this.name = name;
        this.muscleGroup = muscleGroup;
    }

    public abstract String getDetails();
}

class StrengthExercise extends Exercise {
    private int reps;
    private int weight;

    public StrengthExercise(String name, String muscleGroup, int reps, int weight) {
        super(name, muscleGroup);
        this.reps = reps;
        this.weight = weight;
    }

    @Override
    public String getDetails() {
        return "Strength Exercise: " + name + ", Muscle Group: " + muscleGroup + ", Reps: " + reps + ", Weight: " + weight + "lbs";
    }
}

class CardioExercise extends Exercise {
    private int duration;

    public CardioExercise(String name, String muscleGroup, int duration) {
        super(name, muscleGroup);
        this.duration = duration;
    }

    @Override
    public String getDetails() {
        return "Cardio Exercise: " + name + ", Muscle Group: " + muscleGroup + ", Duration: " + duration + " mins";
    }
}

class ExerciseInput {
    private Exercise exercise;

    public ExerciseInput(Exercise exercise) {
        this.exercise = exercise;
    }

    public JPanel getPanel() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel(exercise.getDetails());
        panel.add(label);
        return panel;
    }

    public String getExerciseDetails() {
        return exercise.getDetails();
    }
}



