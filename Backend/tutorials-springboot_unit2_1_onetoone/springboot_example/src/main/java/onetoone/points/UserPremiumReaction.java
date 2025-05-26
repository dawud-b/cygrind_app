package onetoone.points;

import jakarta.persistence.*;
import onetoone.users.User;

import java.time.LocalDateTime;

/**
 * Tracks which premium reactions a user has unlocked
 */
@Entity
public class UserPremiumReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "premium_reaction_id")
    private PremiumReaction premiumReaction;

    private LocalDateTime purchaseDate;

    // Constructor
    public UserPremiumReaction() {
        this.purchaseDate = LocalDateTime.now();
    }

    public UserPremiumReaction(User user, PremiumReaction premiumReaction) {
        this.user = user;
        this.premiumReaction = premiumReaction;
        this.purchaseDate = LocalDateTime.now();
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

    public PremiumReaction getPremiumReaction() {
        return premiumReaction;
    }

    public void setPremiumReaction(PremiumReaction premiumReaction) {
        this.premiumReaction = premiumReaction;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
}