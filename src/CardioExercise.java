public class CardioExercise extends Exercise {
    int duration; // in minutes
    String intensity;

    public CardioExercise(String name, String muscle, String equipment, String difficulty, String instructions, int duration, String intensity) {
        super(name, "cardio", muscle, equipment, difficulty, instructions);
        this.duration = duration;
        this.intensity = intensity;
    }
}
