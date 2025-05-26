package com.example.androidexample.Messaging;

import android.content.SharedPreferences;
import android.util.Log;

import com.example.androidexample.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Message class represents a message in a chat system. It holds information about the message's content,
 * the sender (user), the timestamp when it was sent, and any reactions associated with the message.
 * This class also provides methods for determining if the message was sent by the current user and for adding
 * and managing reactions.
 * <p>
 * This class also handles JSON parsing for message data received from the server and provides utility methods
 * for checking the sender and managing reactions.
 */
public class Message {
    private Long id;  // Unique identifier for the message
    private User user;  // User who sent the message
    private String content;  // The content of the message
    private Date sent;  // Timestamp of when the message was sent
    public boolean isCurrentUser;  // Flag indicating if the message was sent by the current user
    public List<Reaction> reactionList;  // List of reactions associated with the message

    /**
     * Default constructor for Message.
     */
    public Message() {}

    /**
     * Constructor for creating a new message instance.
     *
     * @param user The user who sent the message.
     * @param content The content of the message.
     * @param isCurrentUser A flag indicating if the message was sent by the current user.
     */
    public Message(User user, String content, boolean isCurrentUser) {
        this.user = user;
        this.content = content;
        this.isCurrentUser = isCurrentUser;
        this.reactionList = new ArrayList<>();
    }

    /**
     * Constructor that parses a JSON object to create a message instance.
     * This constructor extracts details like message ID, sender, content, timestamp,
     * and reactions from the provided JSON object.
     *
     * @param obj The JSON object containing message data.
     * @param currentUsername The username of the current user, used to determine if the message was sent by them.
     */
    public Message(JSONObject obj, String currentUsername) {
        try {
            this.id = obj.getLong("id");  // Set message ID
            this.user = new User(obj.getJSONObject("user"));  // Set the sender
            this.isCurrentUser = this.user.getUsername().equals(currentUsername);  // Check if the sender is the current user
            this.content = obj.getString("content");  // Set message content

            // Parse the sent timestamp, handling multiple date formats
            try {
                String timeStamp = obj.getString("sent");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                this.sent = dateFormat.parse(timeStamp);
            } catch (ParseException e) {
                Log.e("Message parsing", "Error parsing message timestamp");
            }

            this.reactionList = new ArrayList<>();  // Initialize reaction list
        } catch (JSONException e) {
            Log.e("Message JSONException", "Error parsing Message JSON");
        }
    }

    /**
     * Checks if the message was sent by the specified user.
     *
     * @param username The username to compare against the sender.
     * @return True if the message was sent by the specified user, false otherwise.
     */
    public boolean isCurrent(String username) {
        return username.equals(user.getUsername());
    }

    /**
     * Adds a reaction to the message.
     *
     * @param reaction The reaction to be added.
     */
    public void addReaction(Reaction reaction) {
        reactionList.add(reaction);
    }

    /**
     * Sets the list of reactions for the message from a JSON array.
     *
     * @param reactions A JSON array containing reaction data.
     */
    public void setReactions(JSONArray reactions) {
        this.reactionList = new ArrayList<>();
        for (int i = 0; i < reactions.length(); i++) {
            try {
                reactionList.add(new Reaction((JSONObject) reactions.get(i)));
            } catch (JSONException e) {
                Log.e("Message JSONException", "Error parsing Message JSON");
            }
        }
    }

    // Getters and Setters

    /**
     * Gets the user who sent the message.
     *
     * @return The sender of the message.
     */
    public User getSender() {
        return user;
    }

    /**
     * Sets the user who sent the message.
     *
     * @param user The user who sent the message.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Gets the list of reactions associated with the message.
     *
     * @return The list of reactions.
     */
    public List<Reaction> getReactions() {
        return reactionList;
    }

    /**
     * Gets the unique identifier for the message.
     *
     * @return The message ID.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the unique identifier for the message.
     *
     * @param id The message ID.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the content of the message.
     *
     * @return The message content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of the message.
     *
     * @param content The message content.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the timestamp when the message was sent.
     *
     * @return The sent timestamp.
     */
    public Date getSent() {
        return sent;
    }

    /**
     * Sets the timestamp when the message was sent.
     *
     * @param sent The sent timestamp.
     */
    public void setSent(Date sent) {
        this.sent = sent;
    }
}
