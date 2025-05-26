package onetoone.WorkoutGroups;

import jakarta.persistence.*;
import onetoone.users.User;
import java.time.LocalDateTime;

@Entity
public class JoinRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private WorkoutGroup group;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    public enum RequestStatus {
        PENDING,
        ACCEPTED,
        REJECTED
    }

    public JoinRequest() {
        this.requestedAt = LocalDateTime.now();
        this.status = RequestStatus.PENDING;
    }

    public JoinRequest(User user, WorkoutGroup group) {
        this.user = user;
        this.group = group;
        this.requestedAt = LocalDateTime.now();
        this.status = RequestStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public WorkoutGroup getGroup() {
        return group;
    }

    public void setGroup(WorkoutGroup group) {
        this.group = group;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}