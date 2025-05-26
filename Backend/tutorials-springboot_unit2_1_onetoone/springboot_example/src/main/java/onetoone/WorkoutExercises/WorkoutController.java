package onetoone.WorkoutExercises;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/workouts")
@Tag(name = "Workout Controller", description = "REST APIs related to Workout Exercises and External API Integration")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @Operation(summary = "Get exercises by muscle", description = "Fetch exercises for a specific muscle group from the external API Ninja service.")
    @GetMapping("/api/muscle/{muscle}")
    public ResponseEntity<List<Exercise>> getExercisesByMuscleFromApi(@PathVariable String muscle) {
        List<Exercise> exercises = workoutService.getExercisesByMuscleFromApi(muscle);
        return ResponseEntity.ok(exercises);
    }

    @Operation(summary = "Save all exercises", description = "Fetch and save exercises for all muscle groups from the external API to the local database.")
    @PostMapping("/api/save/all")
    public ResponseEntity<List<Exercise>> saveAllExercises() {
        List<Exercise> allExercises = new ArrayList<>();

        // Iterate through all muscle groups
        for (String muscle : workoutService.getMuscleGroups()) {
            // Fetch and save exercises for the current muscle group
            List<Exercise> exercises = workoutService.getExercisesByMuscleFromApi(muscle);
            List<Exercise> savedExercises = workoutService.saveExercisesToDatabase(exercises);

            // Add the saved exercises to the list of all exercises
            allExercises.addAll(savedExercises);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(allExercises);
    }

    @Operation(summary = "Save exercises by muscle", description = "Fetch and save exercises for a specific muscle group from the external API to the local database.")
    @PostMapping("/api/save/muscle/{muscle}")
    public ResponseEntity<List<Exercise>> saveExercisesByMuscle(@PathVariable String muscle) {
        List<Exercise> exercises = workoutService.getExercisesByMuscleFromApi(muscle);
        List<Exercise> savedExercises = workoutService.saveExercisesToDatabase(exercises);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedExercises);
    }

    @Operation(summary = "Get all exercises", description = "Retrieve all exercises stored in the local database.")
    @GetMapping("/database/exercises")
    public ResponseEntity<List<Exercise>> getAllExercisesFromDatabase() {
        List<Exercise> exercises = workoutService.getAllExercisesFromDatabase();
        return ResponseEntity.ok(exercises);
    }

    @Operation(summary = "Add new exercise", description = "Create a new exercise entry in the local database.")
    @PostMapping("/database/exercises")
    public ResponseEntity<Exercise> addExerciseToDatabase(@RequestBody Exercise exercise) {
        Exercise newExercise = workoutService.addExerciseToDatabase(exercise);
        return ResponseEntity.status(HttpStatus.CREATED).body(newExercise);
    }

    @Operation(summary = "Get exercise by ID", description = "Retrieve a specific exercise from the local database using its unique identifier.")
    @GetMapping("/database/exercises/{id}")
    public ResponseEntity<Exercise> getExerciseByIdFromDatabase(@PathVariable Long id) {
        Optional<Exercise> exercise = workoutService.getExerciseByIdFromDatabase(id);
        return exercise.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @Operation(summary = "Update exercise", description = "Modify an existing exercise in the local database.")
    @PutMapping("/database/exercises/{id}")
    public ResponseEntity<Exercise> updateExerciseInDatabase(@PathVariable Long id, @RequestBody Exercise updatedExercise) {
        Optional<Exercise> exercise = workoutService.updateExerciseInDatabase(id, updatedExercise);
        return exercise.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @Operation(summary = "Delete exercise", description = "Remove an exercise from the local database.")
    @DeleteMapping("/database/exercises/{id}")
    public ResponseEntity<Void> deleteExerciseFromDatabase(@PathVariable Long id) {
        boolean isDeleted = workoutService.deleteExerciseFromDatabase(id);
        return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}