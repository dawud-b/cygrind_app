package onetoone.points;

import jakarta.persistence.*;
import onetoone.users.User;
import java.time.LocalDateTime;

@Entity
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    private int points;
    private String action;
    private String description;
    private LocalDateTime timestamp;

    // Constructor
    public PointTransaction() {
        this.timestamp = LocalDateTime.now();
    }

    public PointTransaction(User user, int points, String action, String description) {
        this.user = user;
        this.points = points;
        this.action = action;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
