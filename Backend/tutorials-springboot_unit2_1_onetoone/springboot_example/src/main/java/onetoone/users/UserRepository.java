package onetoone.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Users
 *
 * @author Dawud Benedict, Joey
 */

public interface UserRepository extends JpaRepository<User, Long>{
    User findById(long id);
    void deleteByUsername(String username);
    User findByUsername(String username);
    User findByEmail(String email);
    List<User> findFriendsByUsername(String username);
    Optional<User> findByStripeCustomerId(String stripeCustomerId);
    Optional<User> findBySubscriptionId(String subscriptionId);
}
