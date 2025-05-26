package com.example.androidexample.Challenges;

/**
 * Model class representing a single challenge
 */
public class ChallengeModel {
    private long id;
    private String title;
    private String description;
    private int points;
    private boolean completed;

    public ChallengeModel(long id, String title, String description, int points, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.points = points;
        this.completed = completed;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getPoints() {
        return points;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}