package com.example.androidexample.WorkoutTemplate;

import android.util.Log;

import com.example.androidexample.Exercise;
import com.example.androidexample.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a workout template which includes metadata such as title, description, and date,
 * along with a list of associated {@link TemplateExercise} objects. A workout template is created
 * by a user and may be edited or stored for future workouts.
 */
public class WorkoutTemplate {

    private Long id;
    private String title; // Title of the workout template
    private String description; // Description of the workout template
    private String date; // Date associated with the workout (optional)
    private ArrayList<TemplateExercise> exerciseList; // List of exercises in the template
    private User user; // The user who created this template

    /**
     * Constructs a {@code WorkoutTemplate} with a title and default description.
     *
     * @param t The title of the workout template.
     */
    public WorkoutTemplate(String t) {
        this.title = t;
        this.description = "No description.";
        this.exerciseList = new ArrayList<>();
    }

    /**
     * Constructs a {@code WorkoutTemplate} with title, description, and a list of exercises.
     *
     * @param t             The title of the workout template.
     * @param description   The description of the workout template.
     * @param exerciseList  The list of {@link TemplateExercise} instances in the template.
     */
    public WorkoutTemplate(String t, String description, ArrayList<TemplateExercise> exerciseList) {
        this.title = t;
        this.description = description;
        this.exerciseList = exerciseList;
    }

    /**
     * Constructs a {@code WorkoutTemplate} from a JSON object, typically received from a backend.
     *
     * @param obj A JSON object representing the workout template.
     */
    public WorkoutTemplate(JSONObject obj) {
        try {
            this.id = obj.getLong("id");
            this.title = obj.getString("templateName");
            this.description = obj.getString("description");
            this.date = obj.getString("id"); // NOTE: Possible bug – date set from "id"
            this.user = new User(obj.getJSONObject("user"));
            this.exerciseList = new ArrayList<>();
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        }
    }

    /** @param s Sets the title of the workout template. */
    public void setTitle(String s) {
        this.title = s;
    }

    /** @return The title of the workout template. */
    public String getTitle() {
        return this.title;
    }

    /** @return The description of the workout template. */
    public String getDescription() {
        return this.description;
    }

    /** @return The date associated with the workout template. */
    public String getDate() {
        return this.date;
    }

    /** @return The unique identifier of the workout template. */
    public Long getId() {
        return this.id;
    }

    /**
     * Returns the list of exercises associated with this workout template.
     *
     * @return A list of {@link TemplateExercise} objects.
     */
    public ArrayList<TemplateExercise> getExercises() {
        return this.exerciseList;
    }
}
