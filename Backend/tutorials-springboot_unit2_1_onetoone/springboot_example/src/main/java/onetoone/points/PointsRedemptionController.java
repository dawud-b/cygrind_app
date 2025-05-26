package onetoone.points;

import onetoone.users.User;
import onetoone.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/points/redeem")
@Tag(name = "Points Redemption Controller", description = "REST APIs related to redeeming points for rewards")
public class PointsRedemptionController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PremiumReactionRepository premiumReactionRepository;

    @Autowired
    private UserPremiumReactionRepository userPremiumReactionRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @Operation(summary = "Get all premium reactions", description = "Retrieve a list of all available premium reactions that users can purchase with points.")
    @GetMapping("/reactions")
    public List<Map<String, Object>> getAllPremiumReactions() {
        List<PremiumReaction> premiumReactions = premiumReactionRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (PremiumReaction reaction : premiumReactions) {
            Map<String, Object> reactionMap = new HashMap<>();
            reactionMap.put("id", reaction.getId());
            reactionMap.put("name", reaction.getName());
            reactionMap.put("emoji", reaction.getEmojiCode());
            reactionMap.put("pointCost", reaction.getPointCost());
            reactionMap.put("description", reaction.getDescription());

            result.add(reactionMap);
        }

        return result;
    }

    @Operation(summary = "Get user's premium reactions", description = "Retrieve all premium reactions that a specific user has unlocked.")
    @GetMapping("/reactions/{username}")
    public Map<String, Object> getUserPremiumReactions(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("status", "error");
            response.put("message", "User not found");
            return response;
        }

        List<UserPremiumReaction> userReactions = userPremiumReactionRepository.findByUser(user);
        List<Map<String, Object>> unlockedReactions = new ArrayList<>();

        for (UserPremiumReaction userReaction : userReactions) {
            PremiumReaction reaction = userReaction.getPremiumReaction();

            Map<String, Object> reactionMap = new HashMap<>();
            reactionMap.put("id", reaction.getId());
            reactionMap.put("name", reaction.getName());
            reactionMap.put("emoji", reaction.getEmojiCode());
            reactionMap.put("purchaseDate", userReaction.getPurchaseDate());

            unlockedReactions.add(reactionMap);
        }

        response.put("status", "success");
        response.put("username", username);
        response.put("unlockedReactions", unlockedReactions);

        return response;
    }

    @Operation(summary = "Purchase premium reaction", description = "Allow a user to spend points to unlock a premium reaction.")
    @PostMapping("/reactions/{username}/{reactionId}")
    public Map<String, Object> purchasePremiumReaction(
            @PathVariable String username,
            @PathVariable Long reactionId) {

        User user = userRepository.findByUsername(username);
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("status", "error");
            response.put("message", "User not found");
            return response;
        }

        PremiumReaction reaction = premiumReactionRepository.findById(reactionId).orElse(null);

        if (reaction == null) {
            response.put("status", "error");
            response.put("message", "Premium reaction not found");
            return response;
        }

        // Check if user already has this reaction
        UserPremiumReaction existingPurchase =
                userPremiumReactionRepository.findByUserAndPremiumReaction(user, reaction);

        if (existingPurchase != null) {
            response.put("status", "error");
            response.put("message", "You already own this reaction");
            return response;
        }

        // Check if user has enough points
        if (user.getTotalPoints() < reaction.getPointCost()) {
            response.put("status", "error");
            response.put("message", "Not enough points to purchase this reaction");
            response.put("required", reaction.getPointCost());
            response.put("available", user.getTotalPoints());
            return response;
        }

        // Deduct points
        user.addPoints(-reaction.getPointCost());
        userRepository.save(user);

        // Record the transaction
        PointTransaction transaction = new PointTransaction(
                user,
                -reaction.getPointCost(),
                "redeem_reaction",
                "Purchased premium reaction: " + reaction.getName()
        );
        pointTransactionRepository.save(transaction);

        // Add reaction to user's unlocked reactions
        UserPremiumReaction userReaction = new UserPremiumReaction(user, reaction);
        userPremiumReactionRepository.save(userReaction);

        response.put("status", "success");
        response.put("message", "Successfully purchased " + reaction.getName() + " reaction");
        response.put("reaction", reaction.getName());
        response.put("emoji", reaction.getEmojiCode());
        response.put("pointsSpent", reaction.getPointCost());
        response.put("remainingPoints", user.getTotalPoints());

        return response;
    }

    @Operation(summary = "Initialize premium reactions", description = "Admin endpoint to set up the initial set of premium reactions in the system.")
    @PostMapping("/reactions/init")
    public Map<String, Object> initializePremiumReactions() {
        Map<String, Object> response = new HashMap<>();
        List<PremiumReaction> initialReactions = new ArrayList<>();

        // Define premium reaction codes (starting from where standard reactions end)
        final int FIRE = 7;        // Standard reactions ended at 6 (CRY)
        final int MIND_BLOWN = 8;
        final int FLEXING = 9;
        final int TROPHY = 10;
        final int CROWN = 11;
        final int DIAMOND = 12;
        final int ROCKET = 13;
        final int UNICORN = 14;

        // Only add if they don't exist yet
        if (premiumReactionRepository.findByName("Fire") == null) {
            initialReactions.add(new PremiumReaction("Fire", FIRE, 50, "Show something is fire or lit"));
        }

        if (premiumReactionRepository.findByName("Mind Blown") == null) {
            initialReactions.add(new PremiumReaction("Mind Blown", MIND_BLOWN, 75, "When something blows your mind"));
        }

        if (premiumReactionRepository.findByName("Flexing") == null) {
            initialReactions.add(new PremiumReaction("Flexing", FLEXING, 100, "Show off those gains"));
        }

        if (premiumReactionRepository.findByName("Trophy") == null) {
            initialReactions.add(new PremiumReaction("Trophy", TROPHY, 150, "Award for outstanding achievement"));
        }

        if (premiumReactionRepository.findByName("Crown") == null) {
            initialReactions.add(new PremiumReaction("Crown", CROWN, 200, "For royalty status"));
        }

        if (premiumReactionRepository.findByName("Diamond") == null) {
            initialReactions.add(new PremiumReaction("Diamond", DIAMOND, 250, "Rare and precious"));
        }

        if (premiumReactionRepository.findByName("Rocket") == null) {
            initialReactions.add(new PremiumReaction("Rocket", ROCKET, 300, "To the moon!"));
        }

        if (premiumReactionRepository.findByName("Unicorn") == null) {
            initialReactions.add(new PremiumReaction("Unicorn", UNICORN, 500, "Magical and rare"));
        }

        premiumReactionRepository.saveAll(initialReactions);

        response.put("status", "success");
        response.put("message", initialReactions.size() + " premium reactions initialized");
        return response;
    }
}