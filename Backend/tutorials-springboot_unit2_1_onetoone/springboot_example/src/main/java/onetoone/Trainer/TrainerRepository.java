package onetoone.Trainer;

import onetoone.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Trainer findById(int id);
    Trainer findByUser(User user);
    List<Trainer> findBySpecialization(String specialization);
    List<Trainer> findByIsActiveTrue();
    List<Trainer> findByFollowersContaining(User user);
}