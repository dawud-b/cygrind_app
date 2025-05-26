package onetoone.Events;

import onetoone.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Event findById(long id);
    List<Event> findByActive(boolean active);
    List<Event> findByAdminId(long adminId);
    List<Event> findByExerciseType(String exerciseType);
    List<Event> findByStartDateAfter(LocalDateTime date);
    List<Event> findByEndDateBefore(LocalDateTime date);
}