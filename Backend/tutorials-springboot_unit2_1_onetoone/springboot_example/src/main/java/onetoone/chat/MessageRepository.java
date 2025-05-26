package onetoone.chat;

import onetoone.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long>{
        List<Message> findByChatSession(ChatSession chatSession);

        List<Message> findByUser(User user);

        Message findById(long id);
}
