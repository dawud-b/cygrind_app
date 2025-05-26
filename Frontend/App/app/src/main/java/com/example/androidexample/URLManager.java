package com.example.androidexample;

/**
 * URLManager is a utility class that manages the URLs used for communication with the backend server.
 * It provides static methods to generate various URLs for different API endpoints based on the provided parameters.
 */
public class URLManager {

    // Base URLs for HTTP and WebSocket connections
    private static final String BASE_HTTP_URL = "http://coms-3090-035.class.las.iastate.edu:8080";
    private static final String BASE_WS_URL = "ws://coms-3090-035.class.las.iastate.edu:8080";

    // Common URLs for specific resources
    public static final String USERS_URL = BASE_HTTP_URL + "/users";
    public static final String EXERCISE_TEMPLATE_URL = BASE_HTTP_URL + "/exerciseTemplate";
    public static final String WORKOUT_TEMPLATE_URL = BASE_HTTP_URL + "/templates";
    public static final String FRIEND_REQUEST_URL = BASE_HTTP_URL + "/friends/request";
    public static final String WORKOUT_GROUP_URL = BASE_HTTP_URL + "/workout-groups";
    public static final String POINTS_URL = BASE_HTTP_URL + "/points";
    public static final String REDEMPTION_URL = POINTS_URL + "/redeem";
    public static final String getPurchaseReactionURL(String username, Long reactionID) {
        return REDEMPTION_URL+ "/reactions/" + username + "/" + reactionID;
    }
    public static final String getPremiumReactionsURL() {
        return REDEMPTION_URL + "/reactions";
    }
    public static final String getPointBalanceURL(String username) {
        return POINTS_URL + "/total/" + username;
    }
    public static String getPointsLoginURL(String username) {
        return POINTS_URL + "/login/" + username;
    }
    public static String getUserUnlockedReactionsURL(String username) {
        return REDEMPTION_URL + "/reactions/" + username;
    }

    /**
     * Constructs the URL for the point reward for creating a template.
     *
     * @param username The username of the user.
     * @param templateId the Id for the created template.
     * @return The URL for the point reward POST request.
     */
    public static String getTemplatePointRewardURL(String username, Long templateId) {
        return POINTS_URL + "/template-created/" + username + "/" + templateId;
    }

    /**
     * Returns URL for payment POST request.
     *
     * @return The URL for the payment POST request.
     */
    public static String getSubscriptionCreateURL() {
        return BASE_HTTP_URL + "/payment/subscription";
    }

    /**
     * Returns URL for payment cancel.
     *
     * @return The URL for the payment cancel request.
     */
    public static String getSubscriptionCancelURL(String subscriptionId) {
        return BASE_HTTP_URL + "/subscription/" + subscriptionId;
    }

    /**
     * Returns URL for a users subscription status.
     *
     * @return The URL for the users subscription status.
     */
    public static String getSubscriptionStatusURL(String username) {
        return BASE_HTTP_URL + "/payment/" + username;
    }

    /**
     * Returns URL for payment POST request.
     *
     * @return The URL for the payment POST request.
     */
    public static String getPaymentSuccessURL(String username) {
        return BASE_HTTP_URL + "/payment/" + username;
    }

    /**
     * Constructs the URL for a user's workout group based on their username.
     *
     * @param username The username of the user.
     * @return The URL for the user's workout group.
     */
    public static String getUserGroupURL(String username) {
        return WORKOUT_GROUP_URL + "/user/" + username + "/group";
    }

    /**
     * Constructs the URL to retrieve the members of a specific workout group.
     *
     * @param groupId The ID of the workout group.
     * @return The URL for retrieving group members.
     */
    public static String getGroupMembersURL(Long groupId) {
        return WORKOUT_GROUP_URL + "/" + groupId + "/members";
    }

    /**
     * Constructs the URL to retrieve the join requests for a specific workout group.
     *
     * @param groupId The ID of the workout group.
     * @param username The username of the group leader.
     * @return The URL for retrieving group join requests.
     */
    public static String getGroupJoinRequestsURL(Long groupId, String username) {
        return WORKOUT_GROUP_URL + "/" + groupId + "/join-requests" + "?leaderUsername=" + username;
    }

    /**
     * Constructs the URL to accept a join request for a specific workout group.
     *
     * @param groupId The ID of the workout group.
     * @param requestId The ID of the join request.
     * @param username The username of the group leader.
     * @return The URL for accepting the join request.
     */
    public static String acceptGroupJoinRequestsURL(Long groupId, Long requestId, String username) {
        return WORKOUT_GROUP_URL + "/" + groupId + "/accept-request/" + requestId + "?leaderUsername=" + username;
    }

    /**
     * Constructs the URL to kick a member from a specific workout group.
     *
     * @param groupId The ID of the workout group.
     * @param memberUsername The username of the member to be kicked.
     * @param leaderUsername The username of the group leader.
     * @return The URL for kicking a group member.
     */
    public static String kickGroupMemberURL(Long groupId, String memberUsername, String leaderUsername) {
        return WORKOUT_GROUP_URL + "/" + groupId + "/remove-member" + "?memberUsername=" + memberUsername + "&leaderUsername=" + leaderUsername;
    }

    /**
     * Constructs the URL to decline a join request for a specific workout group.
     *
     * @param groupId The ID of the workout group.
     * @param requestId The ID of the join request.
     * @param username The username of the group leader.
     * @return The URL for declining the join request.
     */
    public static String declineGroupJoinRequestsURL(Long groupId, Long requestId, String username) {
        return WORKOUT_GROUP_URL + "/" + groupId + "/accept-request/" + requestId + "?leaderUsername=" + username;
    }

    /**
     * Constructs the URL for a user to leave a workout group.
     *
     * @param groupId The ID of the workout group.
     * @param username The username of the user leaving the group.
     * @return The URL for the user to leave the group.
     */
    public static String leaveGroupURL(Long groupId, String username) {
        return WORKOUT_GROUP_URL + "/" + groupId + "/leave?username=" + username;
    }

    /**
     * Constructs the WebSocket URL for chatting in a specific session.
     *
     * @param sessionId The ID of the chat session.
     * @param username The username of the participant.
     * @return The WebSocket URL for the chat session.
     */
    public static String chatURL(String sessionId, String username) {
        return BASE_WS_URL + "/chat/" + sessionId + "/" + username;
    }

    /**
     * Constructs the URL to remove a friend from the user's friend list.
     *
     * @param username The username of the user.
     * @param friend_username The username of the friend to be removed.
     * @return The URL for removing a friend.
     */
    public static String removeFriendURL(String username, String friend_username) {
        return USERS_URL + "/" + username + "/friends/" + friend_username;
    }

    /**
     * Constructs the URL for a user to request to join a workout group.
     *
     * @param id The ID of the workout group.
     * @param username The username of the user requesting to join.
     * @return The URL for the group join request.
     */
    public static String getGroupJoinURL(Long id, String username) {
        return WORKOUT_GROUP_URL + "/" + id + "/request-join";
    }

    /**
     * Constructs the URL to accept a friend request.
     *
     * @param requestId The ID of the friend request.
     * @return The URL for accepting the friend request.
     */
    public static String getFriendAccept(Long requestId) {
        return FRIEND_REQUEST_URL + "/accept/" + requestId;
    }

    /**
     * Constructs the URL to get the list of sent friend requests for a specific user.
     *
     * @param username The username of the user.
     * @return The URL for retrieving sent friend requests.
     */
    public static String getSentRequestsURL(String username) {
        return FRIEND_REQUEST_URL + "s/" + username + "/sent";
    }

    /**
     * Constructs the URL for creating a new chat session.
     *
     * @return The URL for posting a new chat session.
     */
    public static String postChatSessionURL() {
        return BASE_HTTP_URL + "/chatSession";
    }

    /**
     * Constructs the URL to retrieve the list of friends for a specific user.
     *
     * @param username The username of the user.
     * @return The URL for retrieving the user's friends.
     */
    public static String getFriendsURL(String username) {
        return BASE_HTTP_URL + "/users/" + username + "/friends";
    }

    /**
     * Constructs the URL to get the list of received friend requests for a specific user.
     *
     * @param username The username of the user.
     * @return The URL for retrieving received friend requests.
     */
    public static String getReceivedRequestsURL(String username) {
        return FRIEND_REQUEST_URL + "s/" + username + "/received";
    }

    /**
     * Constructs the URL to get the exercises associated with a specific workout template.
     *
     * @param id The ID of the workout template.
     * @return The URL for retrieving exercises from a workout template.
     */
    public static String getExerciseTempFromWorkoutTempURL(Long id) {
        return WORKOUT_TEMPLATE_URL + "/" + id + "/exercises";
    }

    /**
     * Constructs the URL to retrieve the workout templates associated with a specific user.
     *
     * @param username The username of the user.
     * @return The URL for retrieving the user's workout templates.
     */
    public static String getWorkoutTempFromUserURL(String username) {
        return BASE_HTTP_URL + "/" + username + "/templates";
    }

    /**
     * Constructs the URL to retrieve the chat sessions for a specific user.
     *
     * @param username The username of the user.
     * @return The URL for retrieving the user's chat sessions.
     */
    public static String getUserChatSessionsURL(String username) {
        return USERS_URL + "/" + username + "/chatSessions";
    }

    /**
     * Constructs the URL for a login check based on the username.
     *
     * @param username The username for the login check.
     * @return The URL for checking login credentials.
     */
    public static String getLoginPutReqURL(String username) {
        return USERS_URL + "/passcheck/" + username;
    }

    /**
     * Gets the base HTTP URL.
     *
     * @return The base HTTP URL.
     */
    public static String getBaseUrl() {
        return BASE_HTTP_URL;
    }

    // Trainer related endpoints

    /**
     * Constructs the URL to retrieve all active trainers.
     *
     * @return The URL for retrieving active trainers.
     */
    public static String getTrainersURL() {
        return BASE_HTTP_URL + "/trainers";
    }

    /**
     * Constructs the URL to retrieve a specific trainer by ID.
     *
     * @param trainerId The ID of the trainer.
     * @return The URL for retrieving the trainer by ID.
     */
    public static String getTrainerURL(long trainerId) {
        return BASE_HTTP_URL + "/trainers/" + trainerId;
    }

    /**
     * Constructs the URL to retrieve the trainers followed by a specific user.
     *
     * @param username The username of the user.
     * @return The URL for retrieving followed trainers.
     */
    public static String getFollowedTrainersURL(String username) {
        return BASE_HTTP_URL + "/users/" + username + "/followed-trainers";
    }

    /**
     * Constructs the URL to follow a specific trainer.
     *
     * @param trainerId The ID of the trainer.
     * @param username The username of the user following the trainer.
     * @return The URL for following the trainer.
     */
    public static String getFollowTrainerURL(long trainerId, String username) {
        return BASE_HTTP_URL + "/trainers/" + trainerId + "/followers/" + username;
    }

    /**
     * Constructs the URL to check if a user is a trainer.
     *
     * @param username The username of the user.
     * @return The URL for checking if the user is a trainer.
     */
    public static String getIsTrainerURL(String username) {
        return BASE_HTTP_URL + "/users/" + username + "/is-trainer";
    }

    /**
     * Constructs the URL to retrieve a trainer's workout templates.
     *
     * @param trainerId The ID of the trainer.
     * @return The URL for retrieving the trainer's workout templates.
     */
    public static String getTrainerTemplatesURL(long trainerId) {
        return BASE_HTTP_URL + "/trainers/" + trainerId + "/templates";
    }

    public static String getChallengesForUserURL(String username) {
        return BASE_HTTP_URL + "/challenges/user/" + username;
    }

    public static String getCompletedChallengesForUserURL(String username) {
        return BASE_HTTP_URL + "/challenges/" + username + "/completed";
    }

    public static String getCompleteChallengeURL(long challengeSetId) {
        return BASE_HTTP_URL + "/challenges/" + challengeSetId + "/complete";
    }

    public static final String EXERCISE_DATABASE_URL = BASE_HTTP_URL + "/database/exercises";
}
