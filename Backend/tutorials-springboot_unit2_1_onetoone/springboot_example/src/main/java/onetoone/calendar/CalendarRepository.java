package onetoone.calendar;

import onetoone.Workout_Template.WorkoutTemplate;
import onetoone.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarRepository extends JpaRepository<WorkoutDate, Long> {

    List<WorkoutDate> findByUser(User user);

    WorkoutDate findById(long id);

    List<WorkoutDate> findByWorkout(WorkoutTemplate workoutTemplate);
}
