package com.example.androidexample.Messaging;

import android.util.Log;

import com.example.androidexample.User;

import org.json.JSONException;
import org.json.JSONObject;

public class Reaction {

    static final int LIKED = 0;
    static final int DISLIKED = 1;
    static final int LOVED = 2;
    static final int LAUGHING = 3;
    static final int STRONG = 4;
    static final int COOL = 5;
    static final int CRY = 6;
    static final int FIRE = 7;
    static final int TROPHY = 10;
    static final int CROWN = 11;
    static final int DIAMOND = 12;
    static final int ROCKET = 13;
    static final int UNICORN = 14;

    private Long id;
    private User user;

    private Message message;

    private int reactionType;
    private Boolean isPremium;

    public Reaction(User user, int reactionType ) {
        this.user = user;
        this.reactionType = reactionType;
        isPremium = false;
    }
    public Reaction(int reactionType) {
        this.reactionType = reactionType;
    }
    public Reaction(JSONObject reactionObj) {
        try {
            reactionType = reactionObj.getInt("reactionType");
            isPremium = reactionObj.getBoolean("premium");
        } catch (JSONException e) {
            Log.e("Reaction", "JSONException parsing message reaction");
        }
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return this.user;
    }
    public void setUser(User user) {this.user = user;}

    public int getReactionType() {
        return reactionType;
    }
    public void setReactionType(int reactionType) {this.reactionType = reactionType;}

    public Boolean isPremium() {
        return isPremium;
    }

    public void setPremium(Boolean isPremium) {
        this.isPremium = isPremium;
    }

    public Message getMessage() {return this.message;}
    public void setMessage( Message message ) {this.message = message;}
}
