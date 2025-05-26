package onetoone.WorkoutExercises;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    // Custom query methods can be added here if needed
    Exercise findById(int id);
    Optional<Exercise> findByNameAndMuscle(String name, String muscle);
}
