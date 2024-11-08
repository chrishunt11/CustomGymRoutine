public class CardioExercise extends Exercise {
    int duration; // in minutes
    String intensity;

    public CardioExercise(String name, String muscle, String equipment, String difficulty, int duration, String intensity) {
        super(name, "cardio", muscle, equipment, difficulty);
        this.duration = duration;
        this.intensity = intensity;
    }
}
