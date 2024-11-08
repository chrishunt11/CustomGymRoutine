public class CardioExercise extends Exercise {
    private int duration; // in minutes
    private String intensity;

    public CardioExercise(String name, String muscle, String equipment, String difficulty, int duration, String intensity) {
        super(name, "cardio", muscle, equipment, difficulty, ""); // Assuming an empty string for instructions in superclass
        this.duration = duration;
        this.intensity = intensity;
    }

    // Additional methods and properties (if necessary)
}
