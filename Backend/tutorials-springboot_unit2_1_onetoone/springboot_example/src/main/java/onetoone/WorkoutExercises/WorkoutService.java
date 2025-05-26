package onetoone.WorkoutExercises;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class WorkoutService {

    // List of all muscle groups supported by API Ninja
    private static final List<String> MUSCLE_GROUPS = List.of(
            "abdominals", "abductors", "adductors", "biceps", "calves",
            "chest", "forearms", "glutes", "hamstrings", "lats",
            "lower_back", "middle_back", "neck", "quadriceps", "traps",
            "triceps"
    );

    private final ApiNinjaConfig apiNinjaConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ExerciseRepository exerciseRepository;

    @Autowired
    public WorkoutService(ApiNinjaConfig apiNinjaConfig, ExerciseRepository exerciseRepository) {
        this.apiNinjaConfig = apiNinjaConfig;
        this.exerciseRepository = exerciseRepository;
    }

    // Fetch exercises by muscle group from API Ninja
    public List<Exercise> getExercisesByMuscleFromApi(String muscle) {
        String url = apiNinjaConfig.getApiUrl() + "?muscle=" + muscle;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", apiNinjaConfig.getApiKey()); // Add API key to headers

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Exercise[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Exercise[].class);

        return Arrays.asList(response.getBody());
    }

    // Save exercises to the local database (with duplicate check)
    public List<Exercise> saveExercisesToDatabase(List<Exercise> exercises) {
        List<Exercise> savedExercises = new ArrayList<>();

        for (Exercise exercise : exercises) {
            // Check if the exercise already exists in the database
            Optional<Exercise> existingExercise = exerciseRepository.findByNameAndMuscle(exercise.getName(), exercise.getMuscle());

            if (existingExercise.isEmpty()) {
                // Save the exercise only if it doesn't already exist
                savedExercises.add(exerciseRepository.save(exercise));
            }
        }

        return savedExercises;
    }

    // Fetch exercises from the local database
    public List<Exercise> getAllExercisesFromDatabase() {
        return exerciseRepository.findAll();
    }

    // Add a new exercise to the local database
    public Exercise addExerciseToDatabase(Exercise exercise) {
        return exerciseRepository.save(exercise);
    }

    // Get exercise by ID from the local database
    public Optional<Exercise> getExerciseByIdFromDatabase(Long id) {
        return exerciseRepository.findById(id);
    }

    // Update an existing exercise in the local database
    public Optional<Exercise> updateExerciseInDatabase(Long id, Exercise updatedExercise) {
        return exerciseRepository.findById(id).map(exercise -> {
            exercise.setName(updatedExercise.getName());
            exercise.setType(updatedExercise.getType());
            exercise.setMuscle(updatedExercise.getMuscle());
            exercise.setDifficulty(updatedExercise.getDifficulty());
            return exerciseRepository.save(exercise);
        });
    }

    // Delete an exercise by ID from the local database
    public boolean deleteExerciseFromDatabase(Long id) {
        if (exerciseRepository.existsById(id)) {
            exerciseRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Get the list of muscle groups
    public List<String> getMuscleGroups() {
        return MUSCLE_GROUPS;
    }
}