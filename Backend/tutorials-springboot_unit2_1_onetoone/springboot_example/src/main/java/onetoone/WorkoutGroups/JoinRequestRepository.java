package onetoone.WorkoutGroups;

import onetoone.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    List<JoinRequest> findByGroupAndStatus(WorkoutGroup group, JoinRequest.RequestStatus status);
    JoinRequest findByUserAndGroup(User user, WorkoutGroup group);
    List<JoinRequest> findByUser(User user);
    List<JoinRequest> findByGroup(WorkoutGroup group);
}