package onetoone.chat;

import onetoone.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    ChatSession findBySessionId(String sessionId);
}
