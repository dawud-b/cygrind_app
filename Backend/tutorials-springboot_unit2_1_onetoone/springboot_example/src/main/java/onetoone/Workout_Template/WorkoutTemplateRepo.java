package onetoone.Workout_Template;

import onetoone.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutTemplateRepo extends JpaRepository<WorkoutTemplate, Long> {
    WorkoutTemplate findByTemplateName(String name);

    WorkoutTemplate findById(int id);

    List<WorkoutTemplate> findByUser(User user);

    public void deleteByUser(User user);
}

