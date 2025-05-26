package onetoone.Exercise_Template;

import onetoone.Workout_Template.WorkoutTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseTemplateRepo extends JpaRepository<ExerciseTemplate, Long> {

    public List<ExerciseTemplate> findByWorkoutTemplate(WorkoutTemplate workoutTemplate);

    public ExerciseTemplate findById(int id);
}
