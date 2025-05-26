package onetoone.users;

import onetoone.Challenge.Challenge;
import onetoone.Challenge.ChallengeRepository;
import onetoone.Challenge.ChallengeSet;
import onetoone.Challenge.ChallengeSetRepo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import onetoone.Exercise_Template.ExerciseTemplate;
import onetoone.Exercise_Template.ExerciseTemplateRepo;
import onetoone.Trainer.Trainer;
import onetoone.Trainer.TrainerRepository;
import onetoone.Workout_Template.WorkoutTemplate;
import onetoone.Workout_Template.WorkoutTemplateRepo;
import onetoone.chat.ChatSession;
import onetoone.chat.ChatSessionRepository;
import onetoone.payment.Payment;
import onetoone.payment.PaymentRepository;
import onetoone.points.PointTransaction;
import onetoone.points.PointTransactionRepository;
import onetoone.points.UserPremiumReaction;
import onetoone.points.UserPremiumReactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller used to showcase Create and Read from USER LIST
 *
 * @author Dawud Benedict, Joey
 */
@Tag(name = "User Controller", description = "Endpoints for creating/editing users and retrieving user information.")
@RestController
public class PeopleController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    private WorkoutTemplateRepo workoutTemplateRepo;
    @Autowired
    private ExerciseTemplateRepo exerciseTemplateRepo;
    @Autowired
    private ChatSessionRepository chatSessionRepository;
    @Autowired
    private TrainerRepository trainerRepository;
    @Autowired
    private ChallengeSetRepo challengeSetRepo;
    @Autowired
    private ChallengeRepository challengeRepository;
    @Autowired
    private PointTransactionRepository pointTransactionRepository;
    @Autowired
    private UserPremiumReactionRepository userPremiumReactionRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    //CRUDL (create/read/update/delete/list)
    // use POST, GET, PUT, DELETE, GET methods for CRUDL

    // THIS IS THE LIST OPERATION
    // gets all the users in the list and returns it in JSON format
    @Operation(summary = "Get all users", description = "Returns a list of all the users that are in the database")
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // THIS IS THE CREATE OPERATION
    // Note: To CREATE we use POST method
    @Operation(summary = "Create a User", description = "Creates a new user with the information passed through the request body. Must have unique username and email.")
    @PostMapping("/users")
    public String createUser(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User JSON object that holds the information of the new user") @RequestBody User user) {
        if (user == null)
            return "Failed.";
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return "Failed.";
        }
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return "Failed. Username already exists.";
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return "Failed. Email already in use.";
        }
        // create new User challenges for this user
        createUserChallenges(user);
        userRepository.save(user);
        return "New User Saved: " + user.getUsername() + " " + user.getEmail();
    }

    // THIS IS THE READ OPERATION
    // Springboot gets the PATHVARIABLE from the URL
    // We extract the person from the repo.
    // Note: To READ we use GET method
    @Operation(summary = "Get User by Username", description = "Return the user object that has the given username.")
    @GetMapping("/users/{username}")
    public User getUserByUsername(@Parameter(description = "Username of user to return") @PathVariable String username) {
        return userRepository.findByUsername(username);
    }


    // THIS IS THE UPDATE OPERATION
    // We extract the person from the repo and modify it.
    // Springboot gets the PATHVARIABLE from the URL
    // Note: To UPDATE we use PUT method
    @Operation(summary = "Edit User", description = "Updates the user with username in path with information given in the request body.")
    @PutMapping("/users/{username}")
    public String updateUser(@Parameter(description = "Username of user to update") @PathVariable String username,
                             @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User JSON object that holds the new user information") @RequestBody User u) {

        User user_update = userRepository.findByUsername(username);
        if (user_update == null) {
            return "Failed. User " + username + " not found.";
        }

        if (u.getUsername() != null) {
            String new_username = u.getUsername();
            if (new_username == null || userRepository.findByUsername(new_username) != null) {
                return "Failed. Username " + new_username + " already in use.";
            }
            user_update.setUsername(new_username);
        }

        if (u.getEmail() != null) {
            String new_email = u.getEmail();
            if (new_email == null || userRepository.findByEmail(new_email) != null) {
                return "Failed. Email " + new_email + " already in use.";
            }
            user_update.setEmail(new_email);
        }

        // Update password (if provided)
        if (u.getPassword() != null) {
            String new_password = u.getPassword();
            if (new_password.isEmpty()) {
                return "Failed. Password cannot be empty.";
            }
            user_update.setPassword(new_password); // Update the password
        }

        if (u.getHeight() != 0) {
            user_update.setHeight(u.getHeight());
        }

        if (u.getWeight() != 0) {
            user_update.setWeight(u.getWeight());
        }

        if (u.getAge() != 0) {
            user_update.setAge(u.getAge());
        }

        if (u.getFirstName() != null) {
            user_update.setFirstName(u.getFirstName());
        }

        if (u.getLastName() != null) {
            user_update.setLastName(u.getLastName());
        }

        if (u.getTelephone() != null) {
            user_update.setTelephone(u.getTelephone());
        }

        if (u.getUserRole() != null) {
            user_update.setUserRole(u.getUserRole());
        }


        ChallengeSet userChallenges = challengeSetRepo.findByUserAndTitle(user_update, "New User Challenges");
        if (userChallenges != null && userChallenges.getProgress() != 1) {
            if (user_update.getFirstName() != null && user_update.getLastName() != null && userChallenges.getChallengesCompleted() == 0) {
                Challenge challenge = userChallenges.getChallengeByStage(userChallenges.getChallengesCompleted());
                challenge.setCompleted();
                challenge.setCompletedDate(challenge.getNow());
                challengeRepository.save(challenge);

                user_update.addPoints(challenge.getPoints());
                userRepository.save(user_update);

                userChallenges.setChallengesCompleted();
                challengeSetRepo.save(userChallenges);
            }
            if (user_update.getWeight() != 0 && user_update.getAge() != 0 && user_update.getHeight() != 0 && userChallenges.getChallengesCompleted() == 1) {
                Challenge challenge = userChallenges.getChallengeByStage(userChallenges.getChallengesCompleted());
                challenge.setCompleted();
                challenge.setCompletedDate(challenge.getNow());
                challengeRepository.save(challenge);

                user_update.addPoints(challenge.getPoints());
                userRepository.save(user_update);

                userChallenges.setChallengesCompleted();
                challengeSetRepo.save(userChallenges);
            }
        }

        userRepository.save(user_update);
        return "success";
    }

    // THIS IS THE DELETE OPERATION
    // Springboot gets the PATHVARIABLE from the URL
    // Note: To DELETE we use delete method
    @Operation(summary = "Delete a User", description = "Deletes the user with username in path. Deletes all workoutTemplates and exerciseTemplates associated with this user.")
    @DeleteMapping("/users/{username}")
    public String deleteUser(@Parameter(description = "Username of user to delete") @PathVariable String username) {
        User u = userRepository.findByUsername(username);
        if (u == null) {
            return "User not found";
        }

        // Delete associated Workout/Exercise templates.
        List<WorkoutTemplate> workoutTemplates = workoutTemplateRepo.findByUser(u);
        for (WorkoutTemplate workoutTemplate : workoutTemplates) {
            // First get user WorkoutTemplates and delete associated ExerciseTemplates
            List<ExerciseTemplate> exercises = exerciseTemplateRepo.findByWorkoutTemplate(workoutTemplate);
            for (ExerciseTemplate exerciseTemplate : exercises) {
                exerciseTemplateRepo.delete(exerciseTemplate);
            }
            workoutTemplateRepo.delete(workoutTemplate);
        }

        // delete chat sessions
        List<ChatSession> sessions = u.getChatSessions();
        for (ChatSession chatSession : sessions) {
            u.removeChatSession(chatSession);
            chatSession.removeUser(u);
            chatSessionRepository.save(chatSession);
        }
        userRepository.save(u);

        // remove all friends
        List<User> friends = u.getFriendsList();
        for (User friend : friends) {
            friend.removeFriend(u);
            u.removeFriend(friend);
            userRepository.save(u);
            userRepository.save(friend);
        }

        List<ChallengeSet> challengeSets = new ArrayList<>(u.getChallenges());
        for (ChallengeSet challengeSet : challengeSets) {
            u.removeChallenge(challengeSet);
        }
        userRepository.save(u);

//        // delete payment History
//        List<Payment> payments = paymentRepository.findByUser(u);
//        u.setPaymentHistory(null);
//        for (Payment payment : payments) {
//            payment.setUser(null);
//            paymentRepository.save(payment);
//            paymentRepository.delete(payment);
//        }
//        userRepository.save(u);

        // delete pointTransaction
        List<PointTransaction> pointTransactions = pointTransactionRepository.findByUser(u);
        u.setPointTransactions(null);
        for (PointTransaction pointTransaction : pointTransactions) {
            u.removePointTransaction(pointTransaction);
            pointTransaction.setUser(null);
            pointTransactionRepository.save(pointTransaction);
            pointTransactionRepository.delete(pointTransaction);
        }
        userRepository.save(u);

        //Yo

        // delete premium reactions
        List<UserPremiumReaction> premiumReactions = userPremiumReactionRepository.findByUser(u);
        u.clearPremiumReactions();
        for (UserPremiumReaction userPremiumReaction : premiumReactions) {
            userPremiumReaction.setUser(null);
            userPremiumReactionRepository.save(userPremiumReaction);
            userPremiumReactionRepository.delete(userPremiumReaction);
        }
        userRepository.save(u);

        // delete any trainer on this user
        Trainer trainer = trainerRepository.findByUser(u);
        if (trainer != null) {
            trainerRepository.delete(trainer);
        }

        // delete user
        userRepository.delete(u);
        return "User " + username + " deleted";
    }

    @Operation(summary = "Check Password", description = "Checks that the user associated with the username in path has the same password as given in the request body.")
    @PutMapping("/users/passcheck/{username}")
    public String checkPassword(@Parameter(description = "Username of user to check password") @PathVariable String username, @RequestBody String password) {
        User u = userRepository.findByUsername(username);
        if (u == null) {
            return "User not found";
        }
        if (u.getPassword().equals(password)) {
            return "success";
        } else {
            return "Wrong password";
        }
    }

    //-------------------- Friends -----------------------

    // FOR TESTING ONLY. USE FRIEND REQUEST FOR FRIENDS
//    // PUT request will add the RequestBody user as the path users friend and vise versa.
//    @PostMapping("/users/{username}/friends/{friend_username}")
//    public String addFriend(@PathVariable String username, @PathVariable String friend_username) {
//        User my_user = userRepository.findByUsername(username);
//        User friend = userRepository.findByUsername(friend_username);
//
//        if (my_user == null) {
//            return "User: " + username + " not found";
//        }
//        if (friend == null) {
//            return "User: " + friend_username + " not found";
//        }
//        if (my_user == friend) {
//            return "Cannot add yourself as a friend!";
//        }
//        if (my_user.getFriendByUsername(friend_username) != null) {
//            return "User: " + friend_username + " is already friend";
//        }
//
//        my_user.addFriend(friend);
//        friend.addFriend(my_user);
//
//        userRepository.save(my_user);
//        userRepository.save(friend);
//
//        return "New Friend Added: " + friend_username;
//    }

    // FOR TESTING ONLY. USE FRIEND REQUEST FOR FRIENDS
    // DELETE request will remove current user as the friend_users friend and vise versa.
//    @DeleteMapping("/users/{username}/friends/{friend_username}")
//    public String removeFriend(@PathVariable String username, @PathVariable String friend_username) {
//        System.out.println(friend_username);
//        User my_user = userRepository.findByUsername(username);
//        User friend = userRepository.findByUsername(friend_username);
//
//        if (my_user == null) {
//            return "User: " + username + " not found";
//        }
//        if (friend == null) {
//            return "Friend: " + friend_username + " not found";
//        }
//        if (my_user.getFriendByUsername(friend_username) == null) {
//            return "User: " + friend_username + " is not friended";
//        }
//
//        my_user.removeFriend(friend);
//        friend.removeFriend(my_user);
//
//        userRepository.save(my_user);
//        userRepository.save(friend);
//
//        return "Friend Removed: " + friend_username;
//    }


    // GET request will return the list of friends for the passed user
    @Operation(summary = "Get user's friends", description = "Returns all the users who are friends with the user in the path.")
    @GetMapping("/users/{username}/friends")
    public List<User> getFriends(@Parameter(description = "Username of the user friends list to return") @PathVariable String username) {
        User my_user = userRepository.findByUsername(username);
        // if that user does not exist
        if (my_user == null) {
            return null;
        }
        return my_user.getFriendsList();
    }

    /**
     * Get a user's weight class information
     */
    @Operation(summary = "Get user weightclass", description = "Returns the users weight, weightClass, and weightClassRange for the user in the path.")
    @GetMapping("/users/{username}/weightclass")
    public Map<String, Object> getUserWeightClass(@Parameter(description = "Username of user to return their weightclass") @PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "User not found");
            return error;
        }

        WeightClass weightClass = WeightClass.getWeightClassForWeight(user.getWeight());

        Map<String, Object> result = new HashMap<>();
        result.put("username", user.getUsername());
        result.put("weight", user.getWeight());
        result.put("weightClass", weightClass.getName());
        result.put("weightClassRange", weightClass.toString());

        return result;
    }

    /**
     * Get all available weight classes
     */
    @Operation(summary = "Get weightclasses", description = "Returns all the weightclasses and their weight ranges.")
    @GetMapping("/weightclasses")
    public List<Map<String, Object>> getAllWeightClasses() {
        List<Map<String, Object>> result = new ArrayList<>();

        for (WeightClass weightClass : WeightClass.values()) {
            Map<String, Object> classMap = new HashMap<>();
            classMap.put("name", weightClass.getName());
            classMap.put("minWeight", weightClass.getMinWeight());
            classMap.put("maxWeight", weightClass.getMaxWeight());
            classMap.put("range", weightClass.toString());
            result.add(classMap);
        }

        return result;
    }

    // Create user challenges on user creation
    private void createUserChallenges(User user) {
        ChallengeSet newUserChallenges = new ChallengeSet(user, "New User Challenges");
        challengeSetRepo.save(newUserChallenges);

        ChallengeSet paidUserChallenge = new ChallengeSet(user, "Premium Account");
        challengeSetRepo.save(paidUserChallenge);

        Challenge stage1 = new Challenge("Update Your Name",
                "In your user profile, update your first and last name to earn some points!", 50);
        stage1.setChallengeSet(newUserChallenges);

        Challenge stage2 = new Challenge("Update your Age, Weight, and Height",
                "In your user profile update your age, weight, and height so you can start joining weight-class events!", 50);
        stage2.setChallengeSet(newUserChallenges);

        Challenge stage3 = new Challenge("Gain a friend!",
                "Send or Accept a friend request in the friends tab to complete this challenge!", 100);
        stage3.setChallengeSet(newUserChallenges);

        Challenge stage4 = new Challenge("Join a workout group!",
                "Create or request to join a workout-group in the group tab.", 100);
        stage4.setChallengeSet(newUserChallenges);

        Challenge subscribe = new Challenge("Become a Premium User",
                "Upgrade to a premium account to gain instant points, unlock new features like Nutrition Tracker, and more.",
                2000);
        subscribe.setChallengeSet(paidUserChallenge);

        challengeRepository.save(stage1);
        challengeRepository.save(stage2);
        challengeRepository.save(stage3);
        challengeRepository.save(stage4);
        challengeRepository.save(subscribe);

        newUserChallenges.addChallenge(stage1);
        newUserChallenges.addChallenge(stage2);
        newUserChallenges.addChallenge(stage3);
        newUserChallenges.addChallenge(stage4);
        paidUserChallenge.addChallenge(subscribe);

        challengeSetRepo.save(newUserChallenges);
        challengeSetRepo.save(paidUserChallenge);
        user.addChallenge(newUserChallenges);
        user.addChallenge(paidUserChallenge);
        userRepository.save(user);
    }
}