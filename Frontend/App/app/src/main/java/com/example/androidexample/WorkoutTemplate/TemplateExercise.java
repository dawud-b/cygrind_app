package com.example.androidexample.WorkoutTemplate;

import android.util.Log;

import com.example.androidexample.Exercise;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a single exercise template within a workout template.
 * This class holds details like weight, reps, sets, and associated exercise and workout template objects.
 * It also supports serialization to and from JSON for use with remote APIs.
 */
public class TemplateExercise {

    private long id;
    private int weight;
    private int repCount;
    private int setCount;
    private int duration; // Duration in seconds (optional or reserved for future use)
    private Exercise exercise;
    private WorkoutTemplate template;

    /**
     * Constructs a new {@code TemplateExercise} with basic details.
     *
     * @param ex    The {@link Exercise} associated with this template.
     * @param weight The weight to be used.
     * @param reps   The number of repetitions.
     * @param sets   The number of sets.
     */
    public TemplateExercise(Exercise ex, int weight, int reps, int sets) {
        this.exercise = ex;
        this.weight = weight;
        this.repCount = reps;
        this.setCount = sets;
    }

    /**
     * Constructs a {@code TemplateExercise} from a JSON object, typically received from a backend.
     *
     * @param obj The JSON representation of a template exercise.
     * @throws JSONException if parsing fails.
     */
    public TemplateExercise(JSONObject obj) throws JSONException {
        try {
            this.id = obj.getLong("id");
            this.weight = obj.getInt("weight");
            this.repCount = obj.getInt("repCount");
            this.setCount = obj.getInt("setCount");
            this.exercise = new Exercise(obj.getJSONObject("exercise"));
            this.template = new WorkoutTemplate(obj.getJSONObject("workoutTemplate"));
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        }
    }

    /**
     * Converts this {@code TemplateExercise} into a JSON object for API communication.
     *
     * @return A {@link JSONObject} representing this exercise.
     * @throws JSONException if an error occurs during conversion.
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject exerciseTemplateObj = new JSONObject();
        exerciseTemplateObj.put("weight", this.getWeight());
        exerciseTemplateObj.put("repCount", this.getRepCount());
        exerciseTemplateObj.put("setCount", this.getSetCount());

        JSONObject workoutTemplateObj = new JSONObject();
        workoutTemplateObj.put("id", EditTemplateDataHolder.workoutTemplate.getId());
        exerciseTemplateObj.put("workoutTemplate", workoutTemplateObj);

        JSONObject exerciseObj = new JSONObject();
        exerciseObj.put("id", this.getExercise().getId());
        exerciseTemplateObj.put("exercise", exerciseObj);

        return exerciseTemplateObj;
    }

    /** @return the unique identifier of this template exercise */
    public long getId() {
        return id;
    }

    /** @param id the identifier to set */
    public void setId(long id) {
        this.id = id;
    }

    /** @return the weight associated with this exercise */
    public int getWeight() {
        return weight;
    }

    /** @param weight the weight to set */
    public void setWeight(int weight) {
        this.weight = weight;
    }

    /** @param reps the repetition count to set */
    public void setReps(int reps) {
        this.repCount = reps;
    }

    /** @param sets the set count to set */
    public void setSets(int sets) {
        this.setCount = sets;
    }

    /** @return the number of repetitions */
    public int getRepCount() {
        return repCount;
    }

    /** @return the number of sets */
    public int getSetCount() {
        return setCount;
    }

    /** @return the duration of this exercise in seconds */
    public int getDuration() {
        return duration;
    }

    /** @return the {@link Exercise} object linked to this template */
    public Exercise getExercise() {
        return exercise;
    }
}
