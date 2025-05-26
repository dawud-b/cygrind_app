package onetoone.FriendRequest;

import onetoone.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface FriendRequestRepo extends JpaRepository<FriendRequest, Long> {
    FriendRequest findById(long id);
    Set<FriendRequest> findByReceiverAndStatus(User receiver, int status);
    Set<FriendRequest> findBySenderAndStatus(User sender, int status);
    FriendRequest findByReceiverAndSender(User receiver, User sender);
}
