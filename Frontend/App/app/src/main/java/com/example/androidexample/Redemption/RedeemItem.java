package com.example.androidexample.Redemption;

public class RedeemItem {
    private long reactionId;
    private String name;
    private String emoji;
    private int cost;

    public RedeemItem(long reactionId, String name, String emoji, int cost) {
        this.reactionId = reactionId;
        this.name = name;
        this.emoji = emoji;
        this.cost = cost;
    }

    public long getReactionId() {
        return reactionId;
    }

    public String getName() {
        return name;
    }

    public String getEmoji() {
        return emoji;
    }

    public int getCost() {
        return cost;
    }

    public String getDisplayName() {
        return name + " " + emoji;
    }
}
