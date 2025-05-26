package com.example.androidexample.Trainers;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * Model class representing a trainer's information on the client side.
 * <p>
 * This class contains details such as the trainer's ID, specialization, bio, activity status, and personal information like username, first name, and last name.
 * The data is typically populated by parsing a JSON object received from an API response.
 */
public class Trainer {

    private long id;                // Trainer's unique ID
    private String specialization;  // Trainer's area of expertise (e.g., fitness, yoga)
    private String bio;             // Trainer's biography
    private boolean isActive;       // Status indicating if the trainer is active
    private String username;        // Username of the trainer
    private String firstName;       // Trainer's first name
    private String lastName;        // Trainer's last name

    /**
     * Constructor to initialize the Trainer object from a JSON response.
     *
     * @param trainerJson The JSON object containing the trainer data.
     * @throws JSONException If the JSON object cannot be parsed correctly.
     */
    public Trainer(JSONObject trainerJson) throws JSONException {
        this.id = trainerJson.getLong("id");
        this.specialization = trainerJson.optString("specialization", "");
        this.bio = trainerJson.optString("bio", "");
        this.isActive = trainerJson.optBoolean("isActive", true);

        // Extract user information if available
        if (trainerJson.has("user")) {
            JSONObject userJson = trainerJson.getJSONObject("user");
            this.username = userJson.getString("username");
            this.firstName = userJson.optString("firstName", "");
            this.lastName = userJson.optString("lastName", "");
        }
    }

    /**
     * Gets the trainer's ID.
     *
     * @return The trainer's ID.
     */
    public long getId() {
        return id;
    }

    /**
     * Gets the trainer's specialization.
     *
     * @return The trainer's specialization.
     */
    public String getSpecialization() {
        return specialization;
    }

    /**
     * Gets the trainer's biography.
     *
     * @return The trainer's bio.
     */
    public String getBio() {
        return bio;
    }

    /**
     * Checks if the trainer is currently active.
     *
     * @return {@code true} if the trainer is active, {@code false} otherwise.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Gets the trainer's username.
     *
     * @return The trainer's username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the trainer's first name.
     *
     * @return The trainer's first name.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Gets the trainer's last name.
     *
     * @return The trainer's last name.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Helper method to get the trainer's display name, which is either the full name
     * (first and last name) or the username if the names are not provided.
     *
     * @return The trainer's display name.
     */
    public String getDisplayName() {
        if (firstName.isEmpty() && lastName.isEmpty()) {
            return username;
        } else {
            return firstName + " " + lastName;
        }
    }
}
