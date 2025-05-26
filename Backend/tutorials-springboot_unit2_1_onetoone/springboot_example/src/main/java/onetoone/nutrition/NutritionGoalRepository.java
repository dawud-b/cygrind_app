package onetoone.nutrition;

import onetoone.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionGoalRepository extends JpaRepository<NutritionGoal, Long> {

    NutritionGoal findById(long id);

    NutritionGoal findByUser(User user);
}