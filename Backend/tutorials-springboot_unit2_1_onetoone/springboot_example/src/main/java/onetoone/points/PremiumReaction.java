package onetoone.points;

import jakarta.persistence.*;

/**
 * Defines premium reaction types that can be purchased with points
 */
@Entity
public class PremiumReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private int emojiCode;  // Integer code instead of emoji character

    private int pointCost;

    private String description;

    // Constructor
    public PremiumReaction() {}

    public PremiumReaction(String name, int emojiCode, int pointCost, String description) {
        this.name = name;
        this.emojiCode = emojiCode;
        this.pointCost = pointCost;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getEmojiCode() {
        return emojiCode;
    }

    public void setEmojiCode(int emojiCode) {
        this.emojiCode = emojiCode;
    }

    public int getPointCost() {
        return pointCost;
    }

    public void setPointCost(int pointCost) {
        this.pointCost = pointCost;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}