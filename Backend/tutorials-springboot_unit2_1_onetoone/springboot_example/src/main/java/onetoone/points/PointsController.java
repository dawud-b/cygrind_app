package onetoone.points;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import onetoone.users.User;
import onetoone.users.UserRepository;
import onetoone.Workout_Template.WorkoutTemplate;
import onetoone.Workout_Template.WorkoutTemplateRepo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/points")
@Tag(name = "Points Controller", description = "REST APIs related to Points and Rewards System")
public class PointsController {

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutTemplateRepo workoutTemplateRepo;

    @Operation(summary = "Award login points", description = "Award points to a user for their daily login, limited to once per day.")
    @PostMapping("/login/{username}")
    public Map<String, Object> awardLoginPoints(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("status", "error");
            response.put("message", "User not found");
            return response;
        }

        // Check if user already got login points today
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<PointTransaction> todayLoginPoints = pointTransactionRepository.findByUserAndTimestampBetween(
                user, startOfDay, endOfDay);

        boolean alreadyAwarded = todayLoginPoints.stream()
                .anyMatch(p -> p.getAction().equals(PointsConstants.ACTION_DAILY_LOGIN));

        if (alreadyAwarded) {
            response.put("status", "error");
            response.put("message", "Daily login points already awarded today");
            return response;
        }

        // Create and save new points transaction
        PointTransaction transaction = new PointTransaction(
                user,
                PointsConstants.POINTS_DAILY_LOGIN,
                PointsConstants.ACTION_DAILY_LOGIN,
                "Points for daily login"
        );
        pointTransactionRepository.save(transaction);

        // Update user's total points
        user.addPoints(PointsConstants.POINTS_DAILY_LOGIN);
        userRepository.save(user);

        response.put("status", "success");
        response.put("pointsAwarded", PointsConstants.POINTS_DAILY_LOGIN);
        response.put("totalPoints", user.getTotalPoints());

        return response;
    }

    @Operation(summary = "Award workout points", description = "Award points to a user for completing a specific workout template.")
    @PostMapping("/workout-completed/{username}/{templateId}")
    public Map<String, Object> awardWorkoutPoints(@PathVariable String username, @PathVariable int templateId) {
        User user = userRepository.findByUsername(username);
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("status", "error");
            response.put("message", "User not found");
            return response;
        }

        WorkoutTemplate template = workoutTemplateRepo.findById(templateId);
        if (template == null) {
            response.put("status", "error");
            response.put("message", "Workout template not found");
            return response;
        }

        // Create and save new points transaction
        String description = "Points for completing a workout";
        if (template.getTemplateName() != null) {
            description += ": " + template.getTemplateName();
        }

        PointTransaction transaction = new PointTransaction(
                user,
                PointsConstants.POINTS_COMPLETED_WORKOUT,
                PointsConstants.ACTION_WORKOUT_COMPLETE,
                description
        );
        pointTransactionRepository.save(transaction);

        // Update user's total points
        user.addPoints(PointsConstants.POINTS_COMPLETED_WORKOUT);
        userRepository.save(user);

        response.put("status", "success");
        response.put("pointsAwarded", PointsConstants.POINTS_COMPLETED_WORKOUT);
        response.put("totalPoints", user.getTotalPoints());

        return response;
    }

    @Operation(summary = "Award template creation points", description = "Award points to a user for creating a new workout template.")
    @PostMapping("/template-created/{username}/{templateId}")
    public Map<String, Object> awardTemplatePoints(@PathVariable String username, @PathVariable int templateId) {
        User user = userRepository.findByUsername(username);
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("status", "error");
            response.put("message", "User not found");
            return response;
        }

        WorkoutTemplate template = workoutTemplateRepo.findById(templateId);
        if (template == null) {
            response.put("status", "error");
            response.put("message", "Workout template not found");
            return response;
        }

        // Create and save new points transaction
        String description = "Points for creating a workout template";
        if (template.getTemplateName() != null) {
            description += ": " + template.getTemplateName();
        }

        PointTransaction transaction = new PointTransaction(
                user,
                PointsConstants.POINTS_CREATED_WORKOUT_TEMPLATE,
                PointsConstants.ACTION_CREATE_TEMPLATE,
                description
        );
        pointTransactionRepository.save(transaction);

        // Update user's total points
        user.addPoints(PointsConstants.POINTS_CREATED_WORKOUT_TEMPLATE);
        userRepository.save(user);

        response.put("status", "success");
        response.put("pointsAwarded", PointsConstants.POINTS_CREATED_WORKOUT_TEMPLATE);
        response.put("totalPoints", user.getTotalPoints());

        return response;
    }

    @Operation(summary = "Award custom points", description = "Award custom points to a user for specified actions with flexible point values.")
    @PostMapping("/custom/{username}")
    public Map<String, Object> awardCustomPoints(
            @PathVariable String username,
            @RequestBody Map<String, Object> pointsData) {

        User user = userRepository.findByUsername(username);
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("status", "error");
            response.put("message", "User not found");
            return response;
        }

        // Extract points, action and description from request
        Integer pointsAmount = (Integer) pointsData.get("points");
        String action = (String) pointsData.get("action");
        String description = (String) pointsData.get("description");

        if (pointsAmount == null || action == null || description == null) {
            response.put("status", "error");
            response.put("message", "Points amount, action, and description are required");
            return response;
        }

        // Create and save new points transaction
        PointTransaction transaction = new PointTransaction(user, pointsAmount, action, description);
        pointTransactionRepository.save(transaction);

        // Update user's total points
        user.addPoints(pointsAmount);
        userRepository.save(user);

        response.put("status", "success");
        response.put("pointsAwarded", pointsAmount);
        response.put("totalPoints", user.getTotalPoints());

        return response;
    }

    @Operation(summary = "Get total points", description = "Retrieve the total points accumulated by a specific user.")
    @GetMapping("/total/{username}")
    public Map<String, Object> getTotalPoints(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("status", "error");
            response.put("message", "User not found");
            return response;
        }

        response.put("status", "success");
        response.put("username", username);
        response.put("totalPoints", user.getTotalPoints());

        return response;
    }

    @Operation(summary = "Get points history", description = "Retrieve the complete transaction history of points for a specific user.")
    @GetMapping("/history/{username}")
    public Map<String, Object> getPointsHistory(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("status", "error");
            response.put("message", "User not found");
            return response;
        }

        List<PointTransaction> history = pointTransactionRepository.findByUserOrderByTimestampDesc(user);

        response.put("status", "success");
        response.put("username", username);
        response.put("pointsHistory", history);

        return response;
    }

    @Operation(summary = "Get points leaderboard", description = "Retrieve a ranked leaderboard of all users ordered by their total points.")
    @GetMapping("/leaderboard")
    public List<Map<String, Object>> getLeaderboard() {
        List<User> allUsers = userRepository.findAll();

        // Sort users by points (descending)
        allUsers.sort((u1, u2) -> Integer.compare(u2.getTotalPoints(), u1.getTotalPoints()));

        // Create leaderboard response
        List<Map<String, Object>> leaderboard = new java.util.ArrayList<>();

        int rank = 1;
        for (User user : allUsers) {
            Map<String, Object> userEntry = new HashMap<>();
            userEntry.put("rank", rank++);
            userEntry.put("username", user.getUsername());
            userEntry.put("points", user.getTotalPoints());

            leaderboard.add(userEntry);
        }

        return leaderboard;
    }

    @Operation(summary = "Delete points transaction", description = "Remove a specific points transaction and adjust the user's total points accordingly.")
    @DeleteMapping("/{transactionId}")
    public Map<String, Object> deletePointsEntry(@PathVariable Long transactionId) {
        Map<String, Object> response = new HashMap<>();

        PointTransaction transaction = pointTransactionRepository.findById(transactionId).orElse(null);
        if (transaction == null) {
            response.put("status", "error");
            response.put("message", "Points transaction not found");
            return response;
        }

        User user = transaction.getUser();
        user.addPoints(-transaction.getPoints());
        user.removePointTransaction(transaction); // proper orphan handling
        userRepository.save(user); // this will cascade the deletion

        response.put("status", "success");
        response.put("message", "Points transaction deleted");
        response.put("totalPoints", user.getTotalPoints());

        return response;
    }

}