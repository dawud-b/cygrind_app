package onetoone.Events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import onetoone.users.User;
import onetoone.users.WeightClass;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;
    private String description;
    private String exerciseType; // e.g., "Bench Press", "Squat", etc.
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;

    // Store admin who created the event
    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;

    // Store participants and their scores
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_participants",
            joinColumns = @JoinColumn(name = "event_id"))
    @MapKeyJoinColumn(name = "user_id")
    @Column(name = "score")
    private Map<User, Integer> participants = new HashMap<>();

    // Cache of leaderboard by weight class
    @Transient
    @JsonIgnore
    private Map<WeightClass, Map<User, Integer>> leaderboardByWeightClass;

    public Event() {
    }

    public Event(String title, String description, String exerciseType,
                 User admin, LocalDateTime startDate, LocalDateTime endDate) {
        this.title = title;
        this.description = description;
        this.exerciseType = exerciseType;
        this.admin = admin;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = true;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }

    public User getAdmin() {
        return admin;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Map<User, Integer> getParticipants() {
        return participants;
    }

    public void setParticipants(Map<User, Integer> participants) {
        this.participants = participants;
    }

    // Methods to manage participants
    public void addParticipant(User user, int score) {
        participants.put(user, score);
        // Invalidate cached leaderboard
        leaderboardByWeightClass = null;
    }

    public void updateParticipantScore(User user, int newScore) {
        if (participants.containsKey(user)) {
            // Only update if the new score is better (higher in this case)
            if (newScore > participants.get(user)) {
                participants.put(user, newScore);
                // Invalidate cached leaderboard
                leaderboardByWeightClass = null;
            }
        } else {
            addParticipant(user, newScore);
        }
    }

    public void removeParticipant(User user) {
        participants.remove(user);
        // Invalidate cached leaderboard
        leaderboardByWeightClass = null;
    }

    // Generate leaderboard by weight class
    public Map<WeightClass, Map<User, Integer>> getLeaderboardByWeightClass() {
        if (leaderboardByWeightClass == null) {
            leaderboardByWeightClass = new HashMap<>();

            // Initialize all weight classes
            for (WeightClass weightClass : WeightClass.values()) {
                leaderboardByWeightClass.put(weightClass, new HashMap<>());
            }

            // Populate leaderboard
            for (Map.Entry<User, Integer> entry : participants.entrySet()) {
                User user = entry.getKey();
                Integer score = entry.getValue();
                WeightClass weightClass = WeightClass.getWeightClassForWeight(user.getWeight());
                leaderboardByWeightClass.get(weightClass).put(user, score);
            }
        }

        return leaderboardByWeightClass;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", exerciseType='" + exerciseType + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", active=" + active +
                ", participantsCount=" + participants.size() +
                '}';
    }
}