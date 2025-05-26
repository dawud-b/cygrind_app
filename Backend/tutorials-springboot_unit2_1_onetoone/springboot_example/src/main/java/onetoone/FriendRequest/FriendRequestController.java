package onetoone.FriendRequest;

import onetoone.Challenge.Challenge;
import onetoone.Challenge.ChallengeRepository;
import onetoone.Challenge.ChallengeSet;
import onetoone.Challenge.ChallengeSetRepo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import onetoone.users.User;
import onetoone.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

/**
 * Controller for creating, updating, retrieving, and deleting friend requests.
 *
 * @author Dawud Benedict
 */
@Tag(name = "Friend Request Controller", description = "Controller used to send, update, and view friend requests.")
@RestController
public class FriendRequestController {

    int PENDING = 0;
    int ACCEPTED = 1;
    int IGNORED = -1;

    @Autowired
    FriendRequestRepo friendRequestRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeSetRepo challengeSetRepo;

    // Returns friend requests sent to the given user that are PENDING
    @Operation(summary = "Get received and pending friend requests", description = "Returns a list of all friend request objects that the path user has received and are pending")
    @GetMapping("/friends/requests/{username}/received")
    public Set<FriendRequest> getFriendRequests(@Parameter(description = "Username of the current user") @PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {return null;}
        return friendRequestRepo.findByReceiverAndStatus(user, PENDING);
    }

    // Returns friend requests that the given user has sent and are PENDING
    @Operation(summary = "Get sent and pending friend requests", description = "Returns a list of all friend request objects that the path user has sent and are pending")
    @GetMapping("/friends/requests/{username}/sent")
    public Set<FriendRequest> getSentFriendRequests(@Parameter(description = "Username of the current user") @PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {return null;}
        return friendRequestRepo.findBySenderAndStatus(user, PENDING);
    }

    // Returns friend requests that the given user has sent and were ACCEPTED
    @Operation(summary = "Get sent and accepted friend requests", description = "Returns a list of all friend request objects that the path user has sent and were accepted")
    @GetMapping("/friends/requests/{username}/sent/accepted")
    public Set<FriendRequest> getAcceptedSentFriendRequests(@Parameter(description = "Username of the current user") @PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {return null;}
        return friendRequestRepo.findBySenderAndStatus(user, ACCEPTED);
    }

    // Returns friend requests that the given user has received and ACCEPTED
    @GetMapping("/friends/requests/{username}/received/accepted")
    @Operation(summary = "Get received and pending friend requests", description = "Returns a list of all friend request objects that the path user has received and they accepted")
    public Set<FriendRequest> getAcceptedReceivedFriendRequests(@Parameter(description = "Username of the current user") @PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {return null;}
        return friendRequestRepo.findByReceiverAndStatus(user, ACCEPTED);
    }

    // Returns friend requests that the given user has received and IGNORED
    @GetMapping("/friends/requests/{username}/received/ignored")
    @Operation(summary = "Get received and ignored friend requests", description = "Returns a list of all friend request objects that the path user has received and they ignored")
    public Set<FriendRequest> getIgnoredReceivedFriendRequests(@Parameter(description = "Username of the current user") @PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {return null;}
        return friendRequestRepo.findByReceiverAndStatus(user, IGNORED);
    }

    // Used to create and send friend request. Sets the initial status as PENDING.
    @PostMapping("/friends/request")
    @Operation(summary = "Send a friend request", description = "Creates a friend request object with the sender and receiver user id. Assigns a date to the request and sets status to PENDING")
    public String createFriendRequest(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Friend Request to create", required = true, content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = FriendRequest.class),
            examples = @ExampleObject(value = "{ \"sender\": {\"id\": sender_user_id}, \"receiver\": {\"id\": receiver_user_id} }")))
                                          @RequestBody FriendRequest friendRequest) {
        long sender_id = friendRequest.getSender().getId();
        long receiver_id = friendRequest.getReceiver().getId();
        User sender = userRepository.findById(sender_id);
        User receiver = userRepository.findById(receiver_id);

        if (sender == null) {return "Sender does not exist";}
        if (receiver == null) {return "Receiver does not exist";}
        if (sender == receiver) {return "Sender and Receiver are the same";}

        if (sender.getFriendByUsername(receiver.getUsername()) != null) {
            return "Already Friended";
        }

        if (friendRequestRepo.findByReceiverAndSender(sender, receiver) != null
                | friendRequestRepo.findByReceiverAndSender(receiver, sender) != null) {
            return "Friend request already sent or received";
        }

        LocalDateTime now = LocalDateTime.now();
        friendRequest.setDate(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), now.getHour(), now.getMinute());

        friendRequestRepo.save(friendRequest);
        return "Friend request created";
    }

    // Test for checking if friends can be returned properly
//    @GetMapping("/friends/request/test/{username1}/{username2}")
//    public String checkIfFriends(@PathVariable String username1, @PathVariable String username2) {
//        User user1 = userRepository.findByUsername(username1);
//        User user2 = userRepository.findByUsername(username2);
//        if (user1.getFriendByUsername(user2.getUsername()) != null) {
//            return "Friends";
//        }
//        return "Not Friends";
//    }

    // Used to accept received friend request. Sets FriendRequest status as ACCEPTED
    @PostMapping("/friends/request/accept/{request_id}")
    @Operation(summary = "Accept Friend request", description = "Updates the friend request with the path variable id with ACCEPTED status")
    public String acceptFriendRequest(@Parameter(description = "Friend request id to accept") @PathVariable long request_id) {
        FriendRequest request = friendRequestRepo.findById(request_id);
        if (request == null) {return "Friend request does not exist";}

        User sender = request.getSender();
        User accepter = request.getReceiver();

        if (sender == null) {
            return "Sender not found";
        }
        if (accepter == null) {
            return " not found";
        }
        if (sender == accepter) {
            return "Cannot add yourself as a friend!";
        }
        if (accepter.getFriendByUsername(sender.getUsername()) != null) {
            return "User: " + sender.getUsername() + " is already friended";
        }

        sender.addFriend(accepter);
        accepter.addFriend(sender);
        userRepository.save(sender);
        userRepository.save(accepter);

        request.setStatus(request.ACCEPTED);

        friendRequestRepo.save(request);

        checkChallenge(accepter);
        checkChallenge(sender);

        return "Friend request accepted";
    }

    // Used to ignore received friend request. Sets the FriendRequest status as IGNORED.
    @Operation(summary = "Ignore Friend request", description = "Updates the friend request with the path variable id with IGNORED status")
    @PostMapping("/friends/request/ignore/{id}")
    public String ignoreFriendRequest(@Parameter(description = "Friend request id to ignore") @PathVariable long id) {
        FriendRequest request = friendRequestRepo.findById(id);
        if (request == null) {return "Friend request does not exist";}

        request.setStatus(request.IGNORED);
        friendRequestRepo.save(request);

        return "Friend request revoked";
    }

    // Used to revoke sent friend request. Fully deletes FriendRequest
    @Operation(summary = "Delete Friend request", description = "Deletes the friend request with the path variable id. Removes from Schema")
    @DeleteMapping("/friends/request/{id}")
    public String removeFriendRequest(@Parameter(description = "Friend Request id to delete") @PathVariable long id) {
        FriendRequest request = friendRequestRepo.findById(id);
        if (request == null) {return "Friend request does not exist";}

        friendRequestRepo.delete(request);

        return "Friend request revoked";
    }

    private void checkChallenge(User user) {
        ChallengeSet userChallenges = challengeSetRepo.findByUserAndTitle(user, "New User Challenges");
        if (userChallenges != null && userChallenges.getProgress() != 1) {
            if (user.getFriendCount() > 0 && userChallenges.getChallengesCompleted() == 2) {
                Challenge challenge = userChallenges.getChallengeByStage(userChallenges.getChallengesCompleted());
                challenge.setCompleted();
                challenge.setCompletedDate(challenge.getNow());
                challengeRepository.save(challenge);

                user.addPoints(challenge.getPoints());
                userRepository.save(user);

                userChallenges.setChallengesCompleted();
                challengeSetRepo.save(userChallenges);
            }
        }
    }
}
