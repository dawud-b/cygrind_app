package com.example.androidexample.Leaderboard;
/**
 * Represents an entry in the leaderboard.
 * Each entry contains information about a participant's username, full name (first and last name),
 * weight class, and score. The class also provides methods to retrieve the participant's display name,
 * either combining the first and last name or falling back to the username.
 */
public class LeaderboardEntry {
    private String userName;  // The username of the participant
    private String firstName;  // The first name of the participant
    private String lastName;  // The last name of the participant
    private String weightClass;  // The weight class of the participant
    private int score;  // The score of the participant

    /**
     * Constructor to initialize a new leaderboard entry with the given participant details.
     *
     * @param userName The username of the participant.
     * @param firstName The first name of the participant.
     * @param lastName The last name of the participant.
     * @param weightClass The weight class of the participant.
     * @param score The score of the participant.
     */
    public LeaderboardEntry(String userName, String firstName, String lastName, String weightClass, int score) {
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.weightClass = weightClass;
        this.score = score;
    }

    /**
     * Gets the username of the participant.
     *
     * @return The username of the participant.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the username of the participant.
     *
     * @param userName The new username to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets the first name of the participant.
     *
     * @return The first name of the participant.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of the participant.
     *
     * @param firstName The new first name to set.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the last name of the participant.
     *
     * @return The last name of the participant.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of the participant.
     *
     * @param lastName The new last name to set.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the weight class of the participant.
     *
     * @return The weight class of the participant.
     */
    public String getWeightClass() {
        return weightClass;
    }

    /**
     * Sets the weight class of the participant.
     *
     * @param weightClass The new weight class to set.
     */
    public void setWeightClass(String weightClass) {
        this.weightClass = weightClass;
    }

    /**
     * Gets the score of the participant.
     *
     * @return The score of the participant.
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the score of the participant.
     *
     * @param score The new score to set.
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Returns the display name of the participant. This is the participant's first and last name
     * if both are available, otherwise, it returns the username.
     *
     * @return The display name of the participant (either first and last name or username).
     */
    public String getDisplayName() {
        if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
            return firstName + " " + lastName;
        }
        return userName;
    }
}
