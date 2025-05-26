package com.example.androidexample.Friends;

import android.util.Log;

import com.example.androidexample.User;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a friend request between two users.
 * <p>
 * This class holds the details of a friend request including the sender,
 * recipient, and the request ID (if available). It can also be instantiated
 * from a {@link JSONObject} received from a backend service.
 */
public class FriendRequest {
    private Long id;
    private User sender;
    private User recipient;

    /**
     * Constructs a new {@code Request} with the specified sender and recipient.
     *
     * @param sender    The user who sent the friend request.
     * @param recipient The user who received the friend request.
     */
    public FriendRequest(User sender, User recipient) {
        this.sender = sender;
        this.recipient = recipient;
    }

    /**
     * Constructs a {@code Request} object from a {@link JSONObject}.
     *
     * @param request A JSON object containing the friend request data.
     * @throws JSONException if the JSON parsing fails.
     */
    public FriendRequest(JSONObject request) throws JSONException {
        try {
            JSONObject senderObj = request.getJSONObject("sender");
            JSONObject recipientObj = request.getJSONObject("receiver");
            id = request.getLong("friendRequestId");
            this.sender = new User(senderObj);
            this.recipient = new User(recipientObj);
        } catch (JSONException e) {
            Log.e("Request JSON conversion failed", e.toString());
        }
    }

    /**
     * Returns the unique ID of the friend request.
     *
     * @return the friend request ID, or {@code null} if not set.
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the username of the sender.
     *
     * @return the sender's username.
     */
    public String getSender() {
        return sender.getUsername();
    }

    /**
     * Returns the username of the recipient.
     *
     * @return the recipient's username.
     */
    public String getRecipient() {
        return recipient.getUsername();
    }
}
