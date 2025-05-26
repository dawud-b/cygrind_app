package onetoone.WorkoutGroups;

import onetoone.Challenge.Challenge;
import onetoone.Challenge.ChallengeRepository;
import onetoone.Challenge.ChallengeSet;
import onetoone.Challenge.ChallengeSetRepo;
import onetoone.chat.ChatSession;
import onetoone.users.User;
import onetoone.users.UserRepository;
import onetoone.chat.ChatSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/workout-groups")
@Tag(name = "Workout Group Controller", description = "REST APIs related to Workout Groups management")
public class WorkoutGroupController {

    @Autowired
    private WorkoutGroupRepository workoutGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JoinRequestRepository joinRequestRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeSetRepo challengeSetRepo;

    @Operation(summary = "Get all workout groups", description = "Retrieve a list of all workout groups in the system.")
    @GetMapping
    public ResponseEntity<List<WorkoutGroup>> getAllWorkoutGroups() {
        List<WorkoutGroup> groups = workoutGroupRepository.findAll();
        return ResponseEntity.ok(groups);
    }

    @Operation(summary = "Get group members", description = "Retrieve all members of a specific workout group.")
    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> getGroupMembers(@PathVariable Long groupId) {
        Optional<WorkoutGroup> group = workoutGroupRepository.findById(groupId);
        if (group.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
        }
        return ResponseEntity.ok(group.get().getMembers());
    }

    @Operation(summary = "Get group details", description = "Retrieve detailed information about a specific workout group.")
    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroupDetails(@PathVariable Long groupId) {
        Optional<WorkoutGroup> group = workoutGroupRepository.findById(groupId);
        if (group.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
        }
        return ResponseEntity.ok(group.get());
    }

    @Operation(summary = "Get user's workout group", description = "Retrieve the workout group that a specific user belongs to.")
    @GetMapping("/user/{username}/group")
    public ResponseEntity<?> getUserWorkoutGroup(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        List<WorkoutGroup> groups = workoutGroupRepository.findByMembersContaining(user);
        if (groups.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User is not a member of any workout group");
        }

        //a user can only be in one group at a time
        return ResponseEntity.ok(groups.get(0));
    }

    @Operation(summary = "Get join requests", description = "Retrieve pending join requests for a workout group (leader access only).")
    @GetMapping("/{groupId}/join-requests")
    public ResponseEntity<?> getJoinRequests(@PathVariable Long groupId, @RequestParam String leaderUsername) {
        Optional<WorkoutGroup> group = workoutGroupRepository.findById(groupId);
        if (group.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
        }

        User leader = userRepository.findByUsername(leaderUsername);
        if (leader == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Leader not found");
        }

        if (!group.get().getLeader().equals(leader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only group leader can view join requests");
        }

        List<JoinRequest> pendingRequests = joinRequestRepository.findByGroupAndStatus(
                group.get(), JoinRequest.RequestStatus.PENDING
        );

        return ResponseEntity.ok(pendingRequests);
    }

    @Operation(summary = "Create workout group", description = "Create a new workout group with the requesting user as the leader.")
    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@RequestParam String username,
                                         @RequestBody WorkoutGroup groupDetails) {
        User leader = userRepository.findByUsername(username);
        if (leader == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Check if group name already exists
        if (workoutGroupRepository.findByGroupName(groupDetails.getGroupName()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Group name already exists");
        }

        // Check if user is already in a group
        List<WorkoutGroup> existingGroups = workoutGroupRepository.findByMembersContaining(leader);
        if (!existingGroups.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is already in a group");
        }

        // Create new group
        WorkoutGroup newGroup = new WorkoutGroup(
                groupDetails.getGroupName(),
                leader,
                groupDetails.getDescription(),
                groupDetails.getGroupType()
        );
        workoutGroupRepository.save(newGroup);

        // After creating a workoutGroup, create a group chat session for it
        createWorkoutGroupSession(newGroup);

        checkChallenge(leader);

        return ResponseEntity.ok(newGroup);
    }

    @Operation(summary = "Update group details", description = "Modify a workout group's information (leader access only).")
    @PutMapping("/{groupId}")
    public ResponseEntity<?> updateGroup(@PathVariable Long groupId,
                                         @RequestParam String leaderUsername,
                                         @RequestBody WorkoutGroup updateDetails) {
        Optional<WorkoutGroup> group = workoutGroupRepository.findById(groupId);
        if (group.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
        }

        User leader = userRepository.findByUsername(leaderUsername);
        if (leader == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Check if the user is the group leader
        if (!group.get().getLeader().equals(leader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only group leader can update group details");
        }

        // Check if new group name is already taken
        if (updateDetails.getGroupName() != null && !updateDetails.getGroupName().isEmpty()) {
            WorkoutGroup existingGroup = workoutGroupRepository.findByGroupName(updateDetails.getGroupName());
            if (existingGroup != null && !existingGroup.getId().equals(groupId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Group name already exists");
            }
        }

        // Update group details
        group.get().updateFromDTO(updateDetails);

        // Save updated group
        workoutGroupRepository.save(group.get());

        return ResponseEntity.ok(group.get());
    }

    @Operation(summary = "Request to join group", description = "Submit a request to join a specific workout group.")
    @PostMapping("/{groupId}/request-join")
    public ResponseEntity<?> requestToJoin(@PathVariable Long groupId, @RequestParam String username) {
        Optional<WorkoutGroup> group = workoutGroupRepository.findById(groupId);
        if (group.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Check if user is already in a group
        List<WorkoutGroup> existingGroups = workoutGroupRepository.findByMembersContaining(user);
        if (!existingGroups.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is already in a group");
        }

        // Check if request already exists
        JoinRequest existingRequest = joinRequestRepository.findByUserAndGroup(user, group.get());
        if (existingRequest != null && existingRequest.getStatus() == JoinRequest.RequestStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Join request already submitted");
        }

        // Create join request
        JoinRequest joinRequest = new JoinRequest(user, group.get());
        joinRequestRepository.save(joinRequest);

        checkChallenge(user);

        return ResponseEntity.ok("Join request submitted");
    }

    @Operation(summary = "Accept join request", description = "Approve a pending request to join a workout group (leader access only).")
    @PostMapping("/{groupId}/accept-request/{requestId}")
    public ResponseEntity<?> acceptJoinRequest(@PathVariable Long groupId,
                                               @PathVariable Long requestId,
                                               @RequestParam String leaderUsername) {
        Optional<WorkoutGroup> group = workoutGroupRepository.findById(groupId);
        Optional<JoinRequest> joinRequest = joinRequestRepository.findById(requestId);

        if (group.isEmpty() || joinRequest.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group or request not found");
        }

        User leader = userRepository.findByUsername(leaderUsername);
        if (leader == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Leader not found");
        }

        // Verify leader
        if (!group.get().getLeader().equals(leader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only group leader can accept requests");
        }

        JoinRequest request = joinRequest.get();

        // Check request status
        if (request.getStatus() != JoinRequest.RequestStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request is not in pending status");
        }

        // Add member to group
        group.get().addMember(request.getUser());
        workoutGroupRepository.save(group.get());

        // Update request status
        request.setStatus(JoinRequest.RequestStatus.ACCEPTED);
        joinRequestRepository.save(request);

        // Add new member to the workoutGroup chat session. Save updated chat session and user
        long id = request.getUser().getId();
        User user = userRepository.findById(id);
        String sessionId = groupId + ":workoutGroup";
        ChatSession chatSession = chatSessionRepository.findBySessionId(sessionId);
        if (chatSession == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Chat session not found");
        }
        chatSession.addUser(user);
        chatSessionRepository.save(chatSession);
        user.addChatSessions(chatSession);
        userRepository.save(user);

        return ResponseEntity.ok("Join request accepted");
    }

    @Operation(summary = "Reject join request", description = "Decline a pending request to join a workout group (leader access only).")
    @DeleteMapping("/{groupId}/reject-request/{requestId}")
    public ResponseEntity<?> rejectJoinRequest(@PathVariable Long groupId,
                                               @PathVariable Long requestId,
                                               @RequestParam String leaderUsername) {
        Optional<WorkoutGroup> group = workoutGroupRepository.findById(groupId);
        Optional<JoinRequest> joinRequest = joinRequestRepository.findById(requestId);

        if (group.isEmpty() || joinRequest.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group or request not found");
        }

        User leader = userRepository.findByUsername(leaderUsername);
        if (leader == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Leader not found");
        }

        // Verify leader
        if (!group.get().getLeader().equals(leader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only group leader can reject requests");
        }

        JoinRequest request = joinRequest.get();

        // Check request status
        if (request.getStatus() != JoinRequest.RequestStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request is not in pending status");
        }

        // Update request status
        request.setStatus(JoinRequest.RequestStatus.REJECTED);
        joinRequestRepository.save(request);

        return ResponseEntity.ok("Join request rejected");
    }

    @Operation(summary = "Promote to leader", description = "Assign a new leader for the workout group (current leader access only).")
    @PutMapping("/{groupId}/promote")
    public ResponseEntity<?> promoteToLeader(@PathVariable Long groupId,
                                             @RequestParam String currentLeaderUsername,
                                             @RequestParam String newLeaderUsername) {
        Optional<WorkoutGroup> group = workoutGroupRepository.findById(groupId);
        if (group.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
        }

        User currentLeader = userRepository.findByUsername(currentLeaderUsername);
        User newLeader = userRepository.findByUsername(newLeaderUsername);
        if (currentLeader == null || newLeader == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Verify current leader
        if (!group.get().getLeader().equals(currentLeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only current leader can promote");
        }

        // Verify new leader is in the group
        if (!group.get().getMembers().contains(newLeader)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("New leader must be a group member");
        }

        // Promote new leader
        group.get().setLeader(newLeader);
        workoutGroupRepository.save(group.get());

        return ResponseEntity.ok("New leader promoted");
    }

    @Operation(summary = "Remove member", description = "Remove a member from the workout group (leader access only).")
    @DeleteMapping("/{groupId}/remove-member")
    public ResponseEntity<?> removeMember(@PathVariable Long groupId,
                                          @RequestParam String leaderUsername,
                                          @RequestParam String memberUsername) {
        Optional<WorkoutGroup> group = workoutGroupRepository.findById(groupId);
        if (group.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
        }

        User leader = userRepository.findByUsername(leaderUsername);
        User member = userRepository.findByUsername(memberUsername);
        if (leader == null || member == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Verify leader
        if (!group.get().getLeader().equals(leader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only group leader can remove members");
        }

        // Add this check to prevent leaders from removing themselves
        if (memberUsername.equals(leaderUsername)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Leaders cannot remove themselves. Use the leave endpoint instead.");
        }

        // Check if member is in the group
        if (!group.get().getMembers().contains(member)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not in this group");
        }

        // Remove member
        group.get().removeMember(member);
        workoutGroupRepository.save(group.get());

        return ResponseEntity.ok("Member removed from the group");
    }

    @Operation(summary = "Leave group", description = "Allow a user to leave their current workout group.")
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<?> leaveGroup(@PathVariable Long groupId,
                                        @RequestParam String username) {
        Optional<WorkoutGroup> group = workoutGroupRepository.findById(groupId);
        if (group.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Check if user is in the group
        if (!group.get().getMembers().contains(user)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not in this group");
        }

        // If user is the leader, they can't leave unless they're the last member
        if (group.get().getLeader().equals(user)) {
            if (group.get().getMembers().size() > 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Leader cannot leave a group with other members. Promote another member first.");
            }
            // If last member, delete all related join requests and the group
            List<JoinRequest> requests = joinRequestRepository.findByGroup(group.get());
            joinRequestRepository.deleteAll(requests);
            workoutGroupRepository.delete(group.get());
            return ResponseEntity.ok("Group deleted as last member left");
        }

        // Remove user from the group
        group.get().removeMember(user);
        workoutGroupRepository.save(group.get());

        return ResponseEntity.ok("User left the group");

    }

    @Operation(summary = "Delete group", description = "Remove a workout group and all related data (leader access only).")
    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long groupId,
                                         @RequestParam String leaderUsername) {
        Optional<WorkoutGroup> group = workoutGroupRepository.findById(groupId);
        if (group.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
        }

        User leader = userRepository.findByUsername(leaderUsername);
        if (leader == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Check if the user deleting is the group leader
        if (!group.get().getLeader().equals(leader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only group leader can delete the group");
        }

        // Delete all join requests for this group
        List<JoinRequest> requests = joinRequestRepository.findByGroup(group.get());
        joinRequestRepository.deleteAll(requests);

        // Delete the group
        workoutGroupRepository.delete(group.get());

        return ResponseEntity.ok("Group deleted successfully");
    }

    @Operation(summary = "Create workout group session", description = "Internal method to create a chat session for a workout group.")
    public void createWorkoutGroupSession(WorkoutGroup workoutGroup) {
        // check if workout group exists
        long id = workoutGroup.getId();
        WorkoutGroup currentGroup = workoutGroupRepository.findById(id);
        if (currentGroup == null) {
            throw new IllegalArgumentException("Group not found");
        }

        // check if session already exists
        String sessionId = currentGroup.getId() + ":workoutGroup";
        ChatSession chatSession = chatSessionRepository.findBySessionId(sessionId);
        if (chatSession != null) {
            throw new IllegalArgumentException("Session already exists");
        }

        ChatSession newChatSession = new ChatSession(currentGroup);
        chatSessionRepository.save(newChatSession);

        List<User> members = currentGroup.getMembers();
        for (User user : members) {
            user.addChatSessions(newChatSession);
            userRepository.save(user);
        }
    }

    private void checkChallenge(User user) {
        ChallengeSet userChallenges = challengeSetRepo.findByUserAndTitle(user, "New User Challenges");
        if (userChallenges != null && userChallenges.getProgress() != 1) {
            if (userChallenges.getChallengesCompleted() == 3) {
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
//This is a test for pipelining
//Remove this if needed
