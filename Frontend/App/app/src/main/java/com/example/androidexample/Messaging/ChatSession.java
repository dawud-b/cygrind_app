package com.example.androidexample.Messaging;

import android.util.Log;

import com.example.androidexample.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The ChatSession class represents a chat session, which can be either a one-on-one conversation
 * or a group chat. It contains details about the session, such as its ID, the participants (users),
 * and the last message sent in the session.
 * <p>
 * This class provides constructors for creating a session either by passing individual parameters or
 * by parsing data from a JSONObject. The class also includes methods to retrieve session information
 * such as the session ID, the last message, and the group name (in the case of group chats).
 */
public class ChatSession {

    private String id;  // Unique identifier for the chat session
    private List<User> users;  // List of users in the chat session
    private Message lastMessage;  // Last message sent in the chat session
    private String groupName;  // Name of the group (if the session is a group chat)

    /**
     * Constructor that creates a ChatSession using individual parameters for the session ID,
     * the last message, and the list of users.
     *
     * @param id The unique identifier for the chat session.
     * @param lastMessage The last message sent in the session.
     * @param users The list of users in the session.
     */
    public ChatSession(String id, Message lastMessage, List<User> users) {
        this.id = id;
        this.lastMessage = lastMessage;
        this.users = users;
    }

    /**
     * Constructor that creates a ChatSession by parsing a JSONObject. The session details such as
     * the ID, users, and the last message are extracted from the JSON object. The group name is
     * generated by concatenating the usernames of all users except the current user.
     *
     * @param jsonObject The JSONObject containing the chat session data.
     * @param username The username of the current user (used to exclude the current user from the group name).
     */
    public ChatSession(JSONObject jsonObject, String username) {
        try {
            // Parse the session ID from the JSON object
            this.id = jsonObject.optString("id");

            // Parse the list of users in the session
            JSONArray userArr = jsonObject.getJSONArray("users");
            lastMessage = new Message(jsonObject.getJSONObject("lastMessage"), "");  // No need for the current user's username for this message

            // Initialize users list and group name
            users = new ArrayList<>();
            groupName = "";
            for (int i = 0; i < userArr.length(); i++) {
                // Create a User object for each user in the session
                User user = new User((JSONObject) userArr.get(i));

                // Exclude the current user from the group name and the user list
                if (!user.getUsername().equals(username)) {
                    users.add(user);
                    groupName += user.getUsername() + " ";
                }
            }
        } catch (JSONException e) {
            Log.e("ChatSession JSONException", "Failed to parse ChatSession JSON");
        }
    }

    /**
     * Retrieves the unique identifier for the chat session.
     *
     * @return The session ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves the group name for the chat session. This is applicable only for group chats.
     * The group name is a concatenation of the usernames of all users in the group.
     *
     * @return The group name, or an empty string for one-on-one chats.
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Retrieves the last message sent in the chat session.
     *
     * @return The last message in the session.
     */
    public Message getLastMessage() {
        return lastMessage;
    }

    /*
    public String getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }
    */
}
