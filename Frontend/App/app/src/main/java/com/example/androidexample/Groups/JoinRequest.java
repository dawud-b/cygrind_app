package com.example.androidexample.Groups;

import android.os.Build;

import com.example.androidexample.User;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.json.JSONObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a join request made by a user to join a workout group.
 * This class encapsulates the details of a join request including the request ID, user details,
 * group details, request time, and the status of the request (pending, accepted, or rejected).
 */
public class JoinRequest {

    private Long id;  // The unique identifier for the join request
    private User user;  // The user who made the join request
    private WorkoutGroup group;  // The workout group to which the join request is made
    private LocalDateTime requestedAt;  // The time when the join request was made
    private RequestStatus status;  // The status of the join request (PENDING, ACCEPTED, REJECTED)

    /**
     * Enum representing the possible status values for a join request.
     * The status can be one of the following:
     * <ul>
     *   <li>PENDING: The request is still pending approval</li>
     *   <li>ACCEPTED: The request has been accepted</li>
     *   <li>REJECTED: The request has been rejected</li>
     * </ul>
     */
    public enum RequestStatus {
        PENDING,
        ACCEPTED,
        REJECTED
    }

    /**
     * Constructs a JoinRequest object from the provided JSONObject.
     * The JSON object is expected to contain the necessary fields such as 'id', 'user', 'group',
     * 'requestedAt', and 'status'. These fields are parsed and assigned to the corresponding class members.
     *
     * @param jsonObject The JSON object containing the details of the join request.
     */
    public JoinRequest(JSONObject jsonObject) {
        try {
            // Parse the 'id' field
            this.id = jsonObject.optLong("id", -1); // Default to -1 if not present

            // Parse the 'user' field, assuming it's a nested JSON object
            JSONObject userJson = jsonObject.optJSONObject("user");
            if (userJson != null) {
                this.user = new User(userJson); // Assuming User has a constructor that takes JSONObject
            }

            // Parse the 'group' field, assuming it's a nested JSON object
            JSONObject groupJson = jsonObject.optJSONObject("group");
            if (groupJson != null) {
                this.group = new WorkoutGroup(groupJson); // Assuming WorkoutGroup has a constructor that takes JSONObject
            }

            // Parse the 'requestedAt' field (date/time as a string)
            String requestedAtStr = jsonObject.optString("requestedAt", null);
            if (requestedAtStr != null) {
                DateTimeFormatter formatter = null; // Use an appropriate date format
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    formatter = DateTimeFormatter.ISO_DATE_TIME;
                    this.requestedAt = LocalDateTime.parse(requestedAtStr, formatter);
                }
            }

            // Parse the 'status' field
            String statusStr = jsonObject.optString("status", "PENDING"); // Default to PENDING if not present
            this.status = RequestStatus.valueOf(statusStr.toUpperCase()); // Convert string to enum

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the unique identifier for the join request.
     *
     * @return The ID of the join request
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier for the join request.
     *
     * @param id The ID of the join request
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the user who made the join request.
     *
     * @return The user who made the join request
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user who made the join request.
     *
     * @param user The user who made the join request
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Gets the workout group to which the join request is made.
     *
     * @return The workout group to which the join request is made
     */
    public WorkoutGroup getGroup() {
        return group;
    }

    /**
     * Sets the workout group to which the join request is made.
     *
     * @param group The workout group to which the join request is made
     */
    public void setGroup(WorkoutGroup group) {
        this.group = group;
    }

    /**
     * Gets the time when the join request was made.
     *
     * @return The time when the join request was made
     */
    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    /**
     * Sets the time when the join request was made.
     *
     * @param requestedAt The time when the join request was made
     */
    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    /**
     * Gets the status of the join request.
     *
     * @return The status of the join request (PENDING, ACCEPTED, REJECTED)
     */
    public RequestStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the join request.
     *
     * @param status The status of the join request (PENDING, ACCEPTED, REJECTED)
     */
    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
