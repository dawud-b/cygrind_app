package com.example.androidexample.Challenges;

import java.util.List;

/**
 * Model class representing a set of challenges
 */
public class ChallengeSetModel {
    private long id;
    private String title;
    private double progress;
    private int challengesCompleted;
    private int stages;
    private List<ChallengeModel> challenges;

    public ChallengeSetModel(long id, String title, double progress,
                             int challengesCompleted, int stages,
                             List<ChallengeModel> challenges) {
        this.id = id;
        this.title = title;
        this.progress = progress;
        this.challengesCompleted = challengesCompleted;
        this.stages = stages;
        this.challenges = challenges;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public double getProgress() {
        return progress;
    }

    public int getChallengesCompleted() {
        return challengesCompleted;
    }

    public int getStages() {
        return stages;
    }

    public List<ChallengeModel> getChallenges() {
        return challenges;
    }

    public ChallengeModel getNextChallenge() {
        if (challengesCompleted < challenges.size()) {
            return challenges.get(challengesCompleted);
        }
        return null;
    }
}