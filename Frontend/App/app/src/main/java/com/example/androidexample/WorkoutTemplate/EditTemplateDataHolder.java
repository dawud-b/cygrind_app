package com.example.androidexample.WorkoutTemplate;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.androidexample.ExerciseSelection.ExerciseSelectActivity;
import com.example.androidexample.URLManager;
import com.example.androidexample.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class used to temporarily store workout template data during editing sessions.
 * <p>
 * This class holds static fields to transfer data between activities, especially for use in
 * {@link EditWorkoutTemplateActivity} when editing a {@link WorkoutTemplate}.
 * It acts as a singleton data holder to maintain state across configuration changes or activity transitions
 * where a ViewModel or Intent extras are not used.
 *
 * <p><strong>Note:</strong> Always call {@link #clearData()} after the edit session ends to prevent memory leaks
 * or unintentional data reuse.
 */
public class EditTemplateDataHolder {

    /** The currently loaded workout template being edited. */
    public static WorkoutTemplate workoutTemplate;

    /** The title of the workout template being edited. */
    public static String title;

    /** The description of the workout template being edited. */
    public static String description;

    /** The list of exercises associated with the workout template. */
    public static List<TemplateExercise> exerciseList = new ArrayList<>();

    /**
     * Clears all stored data from this holder.
     * <p>
     * This should be called when the user finishes or cancels editing to reset the state.
     */
    public static void clearData() {
        EditTemplateDataHolder.workoutTemplate = null;
        EditTemplateDataHolder.title = null;
        EditTemplateDataHolder.description = null;
        EditTemplateDataHolder.exerciseList = new ArrayList<>();
    }

    /**
     * Loads the initial data from a given {@link WorkoutTemplate} into this holder.
     * <p>
     * This initializes the title and description from the template. However, the exercise list must still
     * be explicitly fetched by calling `fetchExerciseTemplates()` in {@link EditWorkoutTemplateActivity}.
     *
     * @param template The workout template to load data from.
     */
    public static void loadData(WorkoutTemplate template) {
        EditTemplateDataHolder.description = template.getDescription();
        EditTemplateDataHolder.title = template.getTitle();
        EditTemplateDataHolder.workoutTemplate = template;

        // Must be manually populated with fetchExerciseTemplates()
        EditTemplateDataHolder.exerciseList = new ArrayList<>();
    }
}
