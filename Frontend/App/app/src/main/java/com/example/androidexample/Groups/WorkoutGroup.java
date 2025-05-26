package com.example.androidexample.Groups;

import android.util.Log;

import com.example.androidexample.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Represents a workout group in the application.
 * This class encapsulates details about the workout group such as its ID, name, description,
 * type, leader, and list of members.
 */
public class WorkoutGroup {

    private Long id;  // The unique identifier for the workout group
    private String groupName;  // The name of the workout group
    private String description;  // A brief description of the workout group
    private String groupType;  // The type/category of the workout group (e.g., Fitness, Yoga, etc.)
    private User leader;  // The leader of the workout group
    private List<User> members;  // The list of users who are members of the group

    /**
     * Constructs a WorkoutGroup object from the provided JSONObject.
     * The JSON object is expected to contain the necessary fields such as 'id', 'groupName',
     * 'description', 'groupType', and 'leader'. These fields are parsed and assigned to the
     * corresponding class members.
     *
     * @param groupJson The JSON object containing the details of the workout group.
     */
    public WorkoutGroup(JSONObject groupJson) {
        try {
            // Parse each field from the JSON object
            this.id = groupJson.getLong("id");
            this.groupName = groupJson.getString("groupName");
            this.description = groupJson.getString("description");
            this.groupType = groupJson.getString("groupType");

            // Parse the 'leader' field, assuming it's a nested JSON object
            this.leader = new User(groupJson.getJSONObject("leader"));
        } catch (JSONException e) {
            // Handle errors if any field is missing or the JSON structure is incorrect
            Log.e("JSONException", "Error parsing workout group JSON");
        }
    }

    /**
     * Gets the unique identifier for the workout group.
     *
     * @return The ID of the workout group
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier for the workout group.
     *
     * @param id The ID of the workout group
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the name of the workout group.
     *
     * @return The name of the workout group
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Sets the name of the workout group.
     *
     * @param groupName The name of the workout group
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * Gets the description of the workout group.
     *
     * @return The description of the workout group
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the workout group.
     *
     * @param description The description of the workout group
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the type of the workout group.
     *
     * @return The type of the workout group (e.g., Fitness, Yoga, etc.)
     */
    public String getGroupType() {
        return groupType;
    }

    /**
     * Sets the type of the workout group.
     *
     * @param groupType The type of the workout group
     */
    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    /**
     * Gets the leader of the workout group.
     *
     * @return The leader of the workout group
     */
    public User getLeader() {
        return leader;
    }

    /**
     * Sets the leader of the workout group.
     *
     * @param leader The leader of the workout group
     */
    public void setLeader(User leader) {
        this.leader = leader;
    }

    /**
     * Gets the list of members of the workout group.
     *
     * @return The list of members of the workout group
     */
    public List<User> getMembers() {
        return members;
    }

    /**
     * Sets the list of members of the workout group.
     *
     * @param members The list of members of the workout group
     */
    public void setMembers(List<User> members) {
        this.members = members;
    }
}
