import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomGymRoutine {

    public static void main(String[] args) {
        // Get user preferences
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter muscle group (e.g., biceps, triceps, back): ");
        String muscle = scanner.nextLine().trim();
        System.out.println("Enter difficulty (beginner, intermediate, expert): "); // Updated option
        String difficulty = scanner.nextLine().trim();
        System.out.println("Enter equipment (e.g., dumbbell, barbell, machine): ");
        String equipment = scanner.nextLine().trim();

        // Generate workout based on preferences
        List<Exercise> customWorkout = generateCustomWorkout(muscle, difficulty, equipment);

        // Display the workout
        System.out.println("\nYour Custom Workout Routine:");
        if (customWorkout.isEmpty()) {
            System.out.println("No exercises found matching your criteria.");
        } else {
            for (Exercise exercise : customWorkout) {
                System.out.println("Exercise: " + exercise.name);
                System.out.println("Type: " + exercise.type);
                System.out.println("Muscle: " + exercise.muscle);
                System.out.println("Equipment: " + exercise.equipment);
                System.out.println("Difficulty: " + exercise.difficulty);
                System.out.println("Instructions: " + exercise.instructions);
                System.out.println();
            }
        }
    }

    /**
     * This method fetches exercises from an external API, filters them based on user preferences,
     * and returns a list of matching exercises.
     *
     * @param muscle     the muscle group to target
     * @param difficulty the difficulty level of the exercises
     * @param equipment  the equipment required for the exercises
     * @return List of exercises that match the given preferences
     */
    public static List<Exercise> generateCustomWorkout(String muscle, String difficulty, String equipment) {
        List<Exercise> exactMatches = new ArrayList<>();
        List<Exercise> muscleMatches = new ArrayList<>();

        try {
            // Connect to the API
            URL url = new URL("https://api.api-ninjas.com/v1/exercises?muscle=" + muscle);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("accept", "application/json");
            connection.setRequestProperty("X-Api-Key", "ViDZePNbkVmheKpy62fxYQ==xIjZAovy9dXD0DR5"); // Replace with your actual API key

            InputStream responseStream = connection.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseStream);

            // Print the full API response to inspect what’s being returned
            System.out.println("API Response: " + root.toString());

            // Iterate through each exercise in the API response
            for (JsonNode node : root) {
                String name = node.get("name").asText();
                String type = node.get("type").asText();
                String muscleGroup = node.get("muscle").asText();
                String equipmentRequired = node.get("equipment").asText();
                String difficultyLevel = node.get("difficulty").asText();
                String instructions = node.get("instructions").asText();

                // Check for exact match on all criteria
                if (muscleGroup.equalsIgnoreCase(muscle) && difficultyLevel.equalsIgnoreCase(difficulty)
                        && equipmentRequired.equalsIgnoreCase(equipment)) {
                    if (type.equalsIgnoreCase("strength")) {
                        exactMatches.add(new StrengthExercise(name, muscleGroup, equipmentRequired, difficultyLevel, instructions, 3, 12));
                    } else if (type.equalsIgnoreCase("cardio")) {
                        exactMatches.add(new CardioExercise(name, muscleGroup, equipmentRequired, difficultyLevel, instructions, 30, "moderate"));
                    } else {
                        exactMatches.add(new Exercise(name, type, muscleGroup, equipmentRequired, difficultyLevel, instructions));
                    }
                } else if (muscleGroup.equalsIgnoreCase(muscle)) {
                    // If exact match isn't found, add to muscle-only match list
                    muscleMatches.add(new Exercise(name, type, muscleGroup, equipmentRequired, difficultyLevel, instructions));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return exact matches if available, otherwise return muscle-only matches
        return !exactMatches.isEmpty() ? exactMatches : muscleMatches;
    }
}
