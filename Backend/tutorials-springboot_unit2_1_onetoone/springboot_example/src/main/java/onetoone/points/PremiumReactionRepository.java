package onetoone.points;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PremiumReactionRepository extends JpaRepository<PremiumReaction, Long> {
    PremiumReaction findByName(String name);
    PremiumReaction findByEmojiCode(int emojiCode);
}