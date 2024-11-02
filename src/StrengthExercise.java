public class StrengthExercise extends Exercise {
    int sets, reps;

    public StrengthExercise(String name, String muscle, String equipment, String difficulty, String instructions, int sets, int reps) {
        super(name, "strength", muscle, equipment, difficulty, instructions);
        this.sets = sets;
        this.reps = reps;
    }
}
