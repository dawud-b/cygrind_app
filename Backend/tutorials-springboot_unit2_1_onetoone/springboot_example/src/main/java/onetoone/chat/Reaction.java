package onetoone.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import onetoone.points.PremiumReaction;
import onetoone.users.User;

/**
 * Reaction class for adding reactions to messages.
 *
 * @author Dawud Benedict
 */
@Entity
public class Reaction {

    // Standard reaction types (free)
    final static int LIKED = 0;
    final static int DISLIKED = 1;
    final static int LOVED = 2;
    final static int LAUGHING = 3;
    final static int STRONG = 4;
    final static int COOL = 5;
    final static int CRY = 6;

    // Special indicator for premium reactions
    final static int PREMIUM = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private User user;

    @ManyToOne
    @JsonIgnore
    private Message message;

    @Column
    private int reactionType;

    // For premium reactions
    @ManyToOne
    @JsonIgnore
    private PremiumReaction premiumReaction;

    public Reaction(User user, int reactionType) {
        this.user = user;
        this.reactionType = reactionType;
    }

    public Reaction(User user, PremiumReaction premiumReaction) {
        this.user = user;
        this.reactionType = premiumReaction.getEmojiCode();  // Using the code instead of PREMIUM constant
        this.premiumReaction = premiumReaction;
    }

    public Reaction(){}

    public Long getId() {
        return id;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getReactionType() {
        return reactionType;
    }

    public void setReactionType(int reactionType) {
        this.reactionType = reactionType;
    }

    public Message getMessage() {
        return this.message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public PremiumReaction getPremiumReaction() {
        return premiumReaction;
    }

    public void setPremiumReaction(PremiumReaction premiumReaction) {
        this.premiumReaction = premiumReaction;
        if (premiumReaction != null) {
            this.reactionType = PREMIUM;
        }
    }

    public boolean isPremium() {
        return this.reactionType == PREMIUM;
    }
}