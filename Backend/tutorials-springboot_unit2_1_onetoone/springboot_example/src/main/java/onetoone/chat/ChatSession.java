package onetoone.chat;

import jakarta.persistence.*;
import onetoone.WorkoutGroups.WorkoutGroup;
import onetoone.users.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides the Users in a given chat session
 *
 * @author Dawud Benedict
 */
@Entity
public class ChatSession {

    // Generated String Id based on the usernames of the users in the chat session
    @Id
    private String sessionId;

    // Users in this chat session. Could be group chat could be 1 on 1.
    // Table unnecessary since sessionId already just a bunch of User ids
    @ManyToMany(cascade = CascadeType.PERSIST)
    private List<User> users = new ArrayList<>();

    // The messages sent in this chat
    @OneToMany(mappedBy = "chatSession")
    private List<Message> messages;

    @OneToOne
    private Message lastMessage;


    // Constructor
    public ChatSession(List<User> users) {
        this.users = users;

        // Create a sorted list of usernames to ensure order doesn't affect the ID
        this.sessionId = generateSessionId(users);
    }

    // TODO: Must make it so when a workoutGroup is created the chat is created.
    //  Also, adding users to the group should add them to the chat session.
    // Create a workoutGroup chat
    public ChatSession(WorkoutGroup workoutGroup) {
        this.users = workoutGroup.getMembers();
        this.sessionId = workoutGroup.getId() + ":workoutGroup";
    }

    public ChatSession() {}

    public String getId() {return sessionId;}

    public List<User> getUsers() {return users;}
    public void setUsers(List<User> users) {this.users = users;}

    public Message getLastMessage() {return lastMessage;}
    public void setLastMessage(Message lastMessage) {this.lastMessage = lastMessage;}

    public void addUser(User user) {this.users.add(user);}
    public void removeUser(User user) {this.users.remove(user);}

    public static String generateSessionId(List<User> users) {
        return users.stream()
                .map(user -> String.valueOf(user.getId()))
                .sorted()
                .collect(Collectors.joining(":"));
    }
}
