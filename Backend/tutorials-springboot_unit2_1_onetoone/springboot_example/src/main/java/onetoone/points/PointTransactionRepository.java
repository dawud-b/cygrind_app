package onetoone.points;

import org.springframework.data.jpa.repository.JpaRepository;
import onetoone.users.User;

import java.time.LocalDateTime;
import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    List<PointTransaction> findByUser(User user);
    List<PointTransaction> findByUserAndTimestampBetween(User user, LocalDateTime start, LocalDateTime end);
    List<PointTransaction> findByUserOrderByTimestampDesc(User user);
    List<PointTransaction> findByUserAndAction(User user, String action);
}

