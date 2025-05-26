package com.example.androidexample;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an Exercise entity with information about the exercise name, difficulty, muscle group, type, and ID.
 * This class is commonly used to parse and store exercise data from JSON responses or manually created data.
 */
public class Exercise {
    private long id;                 // Unique identifier of the exercise
    private String name;            // Name of the exercise
    private String type;            // Type of the exercise (e.g., strength, cardio)
    private String difficultly;     // Difficulty level of the exercise (e.g., easy, medium, hard)
    private String muscle;          // Muscle group targeted by the exercise

    /**
     * Constructs an Exercise object from a JSON object.
     *
     * @param obj The JSONObject containing exercise data with keys "id", "name", "difficulty", "type", and "muscle".
     */
    public Exercise(JSONObject obj) {
        try {
            this.id = obj.getLong("id");
            this.name = obj.getString("name");
            this.difficultly = obj.getString("difficulty");
            this.type = obj.getString("type");
            this.muscle = obj.getString("muscle");
        } catch (JSONException e) {
            Log.e("JSON to Exercise Error", e.toString());
        }
    }

    /**
     * Constructs an Exercise with all parameters provided.
     *
     * @param name        The name of the exercise.
     * @param difficultly The difficulty level of the exercise.
     * @param muscle      The muscle group targeted by the exercise.
     * @param type        The type/category of the exercise.
     * @param id          The unique ID of the exercise.
     */
    public Exercise(String name, String difficultly, String muscle, String type, long id) {
        this.id = id;
        this.name = name;
        this.muscle = muscle;
        this.type = type;
        this.difficultly = difficultly;
    }

    /**
     * Constructs a basic Exercise with just a name.
     * Useful for creating placeholder or default exercises.
     *
     * @param name The name of the exercise.
     */
    public Exercise(String name) {
        this.name = name;
        this.difficultly = "";
        this.muscle = "";
    }

    /**
     * Gets the name of the exercise.
     *
     * @return The exercise name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the difficulty level of the exercise.
     *
     * @return The difficulty level.
     */
    public String getDifficultly() {
        return this.difficultly;
    }

    /**
     * Gets the unique ID of the exercise.
     *
     * @return The exercise ID.
     */
    public Long getId() {
        return this.id;
    }
}
