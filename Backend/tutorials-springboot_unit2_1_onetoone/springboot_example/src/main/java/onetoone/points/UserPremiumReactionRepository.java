package onetoone.points;

import onetoone.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserPremiumReactionRepository extends JpaRepository<UserPremiumReaction, Long> {
    List<UserPremiumReaction> findByUser(User user);
    UserPremiumReaction findByUserAndPremiumReaction(User user, PremiumReaction premiumReaction);
}