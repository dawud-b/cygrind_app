package onetoone.Trainer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import onetoone.Workout_Template.WorkoutTemplate;
import onetoone.users.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a trainer in the workout app.
 * A trainer is a user who can create and share workout templates.
 * Users can follow trainers to access their workout templates.
 */
@Entity
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // Trainer-specific information
    private String specialization;  // Area of expertise (e.g., "Weight Training", "Cardio")
    private String bio;             // Short description of the trainer's background
    private boolean isActive = true; // Flag to enable/disable trainer functionality

    // Link to the user account - every trainer must have a user account
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    // List of users who follow this trainer
    @ManyToMany
    @JoinTable(
            name = "trainer_followers",
            joinColumns = @JoinColumn(name = "trainer_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private Set<User> followers = new HashSet<>();

    // Workout templates created by this trainer
    @OneToMany
    @JoinColumn(name = "trainer_id")
    @JsonIgnore
    private List<WorkoutTemplate> trainerTemplates = new ArrayList<>();

    // Default constructor required by JPA
    public Trainer() {
        this.followers = new HashSet<>();
        this.trainerTemplates = new ArrayList<>();
    }

    // Create a trainer linked to an existing user
    public Trainer(User user) {
        this();
        this.user = user;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    // Check if trainer account is active
    public boolean isActive() {
        return isActive;
    }

    // Enable or disable trainer account
    public void setActive(boolean active) {
        isActive = active;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Get all users following this trainer
    public Set<User> getFollowers() {
        return followers;
    }

    // Add a user as a follower
    public void addFollower(User user) {
        this.followers.add(user);
    }

    // Remove a user from followers
    public void removeFollower(User user) {
        this.followers.remove(user);
    }

    // Get all workout templates created by this trainer
    public List<WorkoutTemplate> getTrainerTemplates() {
        return trainerTemplates;
    }

    // Add a workout template to this trainer
    public void addWorkoutTemplate(WorkoutTemplate template) {
        this.trainerTemplates.add(template);
    }

    // Remove a workout template from this trainer
    public void removeWorkoutTemplate(WorkoutTemplate template) {
        this.trainerTemplates.remove(template);
    }
}