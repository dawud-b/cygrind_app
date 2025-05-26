package onetoone.WorkoutGroups;

import onetoone.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutGroupRepository extends JpaRepository<WorkoutGroup, Long> {
    // Find groups by leader
    List<WorkoutGroup> findByLeader(User leader);

    // Find groups a user is a member of
    List<WorkoutGroup> findByMembersContaining(User user);

    // Find a group by its name
    WorkoutGroup findByGroupName(String groupName);

    // Find groups with pending join requests
    List<WorkoutGroup> findByLeaderAndJoinRequestsIsNotEmpty(User leader);

    WorkoutGroup findById(long id);
}