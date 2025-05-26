package onetoone.chat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.Entity;
import onetoone.WorkoutGroups.WorkoutGroup;
import onetoone.WorkoutGroups.WorkoutGroupRepository;
import onetoone.users.User;
import onetoone.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static onetoone.chat.ChatSession.generateSessionId;

/**
 * Controller for creating and reading ChatSessions
 *
 * @author Dawud Benedict
 */
@Tag(name = "Chat Session Controller", description = "Handles creation and retrieval of chat sessions")
@RestController
public class ChatSessionController {

    @Autowired
    ChatSessionRepository chatSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutGroupRepository workoutGroupRepository;

    @Operation(
            summary = "Create a chat session",
            description = "Creates a new chat session between a list of users. If a session already exists, it is returned."
    )
    @PostMapping("/chatSession")
    public String createSession(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of users to create a chat session with")
            @RequestBody List<User> users) {
        // check if users exist
        List<User> userList = new ArrayList<>();
        for (User user : users) {
            long id = user.getId();
            User current = userRepository.findById(id);
            if (current == null) {
                return "One or more users are null!";
            }
            userList.add(current);
        }

        // check if session already exists
        String sessionId = generateSessionId(userList);
        ChatSession chatSession = chatSessionRepository.findBySessionId(sessionId);
        if (chatSession != null) {
            return chatSession.getId();
        }

        ChatSession newChatSession = new ChatSession(users);
        chatSessionRepository.save(newChatSession);

        for (User user : userList) {
            user.addChatSessions(newChatSession);
            userRepository.save(user);
        }
        return newChatSession.getId();
    }

    @Operation(
            summary = "Get chat sessions for a user",
            description = "Returns all chat sessions the given user is part of"
    )
    @GetMapping("/users/{username}/chatSessions")
    public List<ChatSession> getChatSessions(
            @Parameter(description = "Username of the user") @PathVariable String username) {
        User user = userRepository.findByUsername(username);
        return user.getChatSessions();
    }

    @Operation(
            summary = "Get a chat session by session ID",
            description = "Returns a specific chat session given its session ID"
    )
    @GetMapping("/chatSessions/{session_id}")
    public ChatSession getChatSession(
            @Parameter(description = "Session ID of the chat session") @PathVariable String session_id) {
        return chatSessionRepository.findBySessionId(session_id);
    }
}
