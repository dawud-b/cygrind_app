package onetoone.Challenge;

import onetoone.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeSetRepo extends JpaRepository<ChallengeSet, Long> {

    ChallengeSet findById(long id);

    List<ChallengeSet> findByUser(User user);

    ChallengeSet findByUserAndTitle(User user, String title);

}
