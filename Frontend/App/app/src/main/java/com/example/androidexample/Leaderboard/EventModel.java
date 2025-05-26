package com.example.androidexample.Leaderboard;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
/**
 * A model class that represents an event with details such as ID, title, description, exercise type,
 * start and end dates, active status, and leaderboard entries.
 *
 * This class encapsulates the properties of an event, including the event's duration, status, and associated leaderboard entries.
 * It also provides utility methods for formatting the event's dates and retrieving remaining time.
 */
public class EventModel {

    private long id;  // Event ID
    private String title;  // Event title
    private String description;  // Event description
    private String exerciseType;  // Exercise type associated with the event
    private LocalDateTime startDate;  // Event start date and time
    private LocalDateTime endDate;  // Event end date and time
    private boolean active;  // Status of the event (active or not)
    private List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();  // List of leaderboard entries for the event

    /**
     * Constructs an EventModel object with the specified details.
     *
     * @param id The ID of the event.
     * @param title The title of the event.
     * @param description The description of the event.
     * @param exerciseType The type of exercise for the event.
     * @param startDate The start date and time of the event.
     * @param endDate The end date and time of the event.
     * @param active Whether the event is currently active or not.
     */
    public EventModel(long id, String title, String description, String exerciseType,
                      LocalDateTime startDate, LocalDateTime endDate, boolean active) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.exerciseType = exerciseType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
    }

    /**
     * Returns the ID of the event.
     *
     * @return The event ID.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the ID of the event.
     *
     * @param id The event ID to set.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Returns the title of the event.
     *
     * @return The event title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the event.
     *
     * @param title The event title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the description of the event.
     *
     * @return The event description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the event.
     *
     * @param description The event description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the type of exercise for the event.
     *
     * @return The exercise type for the event.
     */
    public String getExerciseType() {
        return exerciseType;
    }

    /**
     * Sets the exercise type for the event.
     *
     * @param exerciseType The exercise type to set.
     */
    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }

    /**
     * Returns the start date and time of the event.
     *
     * @return The event's start date and time.
     */
    public LocalDateTime getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date and time of the event.
     *
     * @param startDate The event's start date and time to set.
     */
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    /**
     * Returns the end date and time of the event.
     *
     * @return The event's end date and time.
     */
    public LocalDateTime getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date and time of the event.
     *
     * @param endDate The event's end date and time to set.
     */
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    /**
     * Returns whether the event is currently active.
     *
     * @return True if the event is active, false otherwise.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active status of the event.
     *
     * @param active The active status to set.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns the leaderboard entries for the event.
     *
     * @return A list of leaderboard entries.
     */
    public List<LeaderboardEntry> getLeaderboardEntries() {
        return leaderboardEntries;
    }

    /**
     * Sets the leaderboard entries for the event.
     *
     * @param leaderboardEntries The leaderboard entries to set.
     */
    public void setLeaderboardEntries(List<LeaderboardEntry> leaderboardEntries) {
        this.leaderboardEntries = leaderboardEntries;
    }

    /**
     * Returns a string representing the time remaining for the event.
     * Since the backend data for remaining time isn't available, a static message is returned.
     *
     * @return A string representing the remaining time, or a static message if unavailable.
     */
    public String getTimeRemaining() {
        // Since we don't have time frame data from the backend yet,
        // just return a static message
        return "Event in progress";
    }

    /**
     * Returns a formatted string of the event's start and end dates in the pattern "MMM d, yyyy - MMM d, yyyy".
     *
     * @return A string representing the formatted start and end dates of the event, or an empty string if dates are unavailable.
     */
    public String getFormattedDates() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        if (startDate != null && endDate != null) {
            return startDate.format(formatter) + " - " + endDate.format(formatter);
        }
        return "";
    }
}
