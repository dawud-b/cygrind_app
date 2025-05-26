package onetoone.nutrition;

import onetoone.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    FoodItem findById(long id);

    List<FoodItem> findByUser(User user);

    List<FoodItem> findByUserAndConsumptionDate(User user, LocalDate date);

    void deleteByUserAndConsumptionDateBefore(User user, LocalDate date);
}