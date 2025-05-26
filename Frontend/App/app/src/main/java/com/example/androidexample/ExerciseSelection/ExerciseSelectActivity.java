package com.example.androidexample.ExerciseSelection;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidexample.Exercise;
import com.example.androidexample.R;
import com.example.androidexample.URLManager;
import com.example.androidexample.VolleySingleton;
import com.example.androidexample.WorkoutTemplate.EditTemplateDataHolder;
import com.example.androidexample.WorkoutTemplate.EditWorkoutTemplateActivity;
import com.example.androidexample.WorkoutTemplate.TemplateExercise;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity that allows users to select and add exercises to a workout template.
 * Displays a list of exercises fetched from a server and filtered by muscle group.
 * Handles user interaction for selecting exercises and inputting workout details.
 * Sends selected data back to the previous activity.
 *
 * Implements {@link ExerciseListAdapter.OnExerciseListener} to handle click events.
 */
public class ExerciseSelectActivity extends AppCompatActivity implements ExerciseListAdapter.OnExerciseListener {

    private RecyclerView exerciseListRecyclerView;
    private ExerciseListAdapter exerciseListAdapter;
    private List<JSONObject> exerciseRepo;
    private List<Exercise> exerciseList;
    private Button  backBtn;
    private TextView activityTitle;
    private Spinner muscleGroupSpinner;
    private String[] muscleGroups;

    /**
     * Initializes the activity, fetches exercises, sets up UI components,
     * and defines listeners.
     *
     * @param savedInstanceState The saved state of the activity if previously created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exerciselist);

        exerciseRepo = new ArrayList<>();
        exerciseList = new ArrayList<>();
        muscleGroups = new String[]{};

        // Initialize views
        activityTitle = findViewById(R.id.exerciselist_title);
        exerciseListRecyclerView = findViewById(R.id.templateList);
        backBtn = findViewById(R.id.exerciseList_back_btn);
        muscleGroupSpinner = findViewById(R.id.exerciselist_spinner);

        exerciseListAdapter = new ExerciseListAdapter(exerciseList, this);
        exerciseListRecyclerView.setAdapter(exerciseListAdapter);
        exerciseListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExerciseSelectActivity.this, EditWorkoutTemplateActivity.class);
                startActivity(intent);
            }
        });

        // populates exerciseRepo with get request
        fetchExercises();
    }

    /**
     * Returns an array of distinct muscle groups from the fetched exercise repository.
     *
     * @return An array of muscle group names including "all".
     */
    private String[] getMuscleGroups() {
        ArrayList<String> groups = new ArrayList<>();
        groups.add("all");
        if(exerciseRepo != null) {
            for(JSONObject ex : exerciseRepo) {
                try {
                    String g = ex.getString("muscle");
                    if( !groups.contains(g) ) {
                        groups.add(g);
                    }
                } catch (JSONException e) {
                    Log.e("JSONException", e.toString());
                }
            }
        }
        return groups.toArray(new String[groups.size()]);
    }

    /**
     * Sets up the spinner dropdown with available muscle groups
     * and filters the exercise list when a selection is made.
     */
    private void setDropDownMenu() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ExerciseSelectActivity.this, android.R.layout.simple_spinner_item, muscleGroups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        muscleGroupSpinner.setAdapter(adapter);

        muscleGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // filter based on selection
                try {
                    setExerciseObjs(muscleGroups[position]);
                } catch (JSONException e) {
                    Log.e("Error setting exercise", e.toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                try {
                    setExerciseObjs("all");
                } catch (JSONException e) {
                    Log.e("Error setting exercise", e.toString());
                }
            }
        });
    }


    /**
     * Filters and populates the exercise list based on the selected muscle group.
     *
     * @param filter The muscle group to filter exercises by. Use "all" for no filtering.
     * @throws JSONException If there's an error parsing JSON data from exerciseRepo.
     */
    private void setExerciseObjs(String filter) throws JSONException {
        exerciseList.clear();
        if (filter.equals("all")) {
            for (int i = 0; i < exerciseRepo.size(); i++) {
                exerciseList.add(new Exercise(exerciseRepo.get(i)));
            }
        } else {
            // Add filter logic for different muscle groups
            for (int i = 0; i < exerciseRepo.size(); i++) {
                JSONObject exercise = exerciseRepo.get(i);
                if (exercise.getString("muscle").equalsIgnoreCase(filter)) {
                    exerciseList.add(new Exercise(exercise));
                }
            }
        }
        exerciseListAdapter.notifyDataSetChanged(); // Notify adapter after list is updated
    }


    /**
     * Callback invoked when an exercise is selected to be added.
     * Prompts user for workout details (weight, reps, sets) via dialog.
     *
     * @param position The position of the selected exercise in the list.
     */
    @Override
    public void onAddClick(int position) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_input, null);

        final EditText editWeight = dialogView.findViewById(R.id.editWeight);
        final EditText editSets = dialogView.findViewById(R.id.editSets);
        final EditText editReps = dialogView.findViewById(R.id.editReps);

        // Create the AlertDialog using a Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseSelectActivity.this);
        builder.setTitle("Enter Workout Details")
                .setView(dialogView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get the input data
                        String weight = editWeight.getText().toString();
                        String sets = editSets.getText().toString();
                        String reps = editReps.getText().toString();

                        // Handle input (you can add validation here)
                        if (!weight.isEmpty() && !sets.isEmpty() && !reps.isEmpty()) {
                            Toast.makeText(ExerciseSelectActivity.this,
                                    "Weight: " + weight + "lbs, Sets: " + sets + ", Reps: " + reps,
                                    Toast.LENGTH_SHORT).show();
                            // Show the entered values or do something with them
                            try {
                                int weightInt = Integer.parseInt(weight);
                                int repsInt = Integer.parseInt(reps);
                                int setsInt = Integer.parseInt(sets);

                                postExerciseTemplate(new TemplateExercise(exerciseList.get(position), weightInt, repsInt, setsInt));
                            } catch (NumberFormatException e) {
                                Toast.makeText(ExerciseSelectActivity.this, "Entries must be a number.", Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                Log.e("Error converting ExerciseTemlate to JSON", e.toString());
                            }
                        } else {
                            // If any field is empty, show an error
                            Toast.makeText(ExerciseSelectActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    /**
     * Posts the selected exercise with user-defined attributes to the server.
     *
     * @param tempEx The {@link TemplateExercise} object to post.
     * @throws JSONException If JSON conversion fails.
     */
    private void postExerciseTemplate(TemplateExercise tempEx) throws JSONException {

        JSONObject exerciseTemplateObj = tempEx.toJSON();
        Log.d("ExerciseTempObj", exerciseTemplateObj.toString());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URLManager.EXERCISE_TEMPLATE_URL, exerciseTemplateObj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", response.toString());
                        if( response == null ) {
                            Toast.makeText(ExerciseSelectActivity.this, "creation failed" , Toast.LENGTH_SHORT).show();
                        } else {
                            // backend response when user is already created
                            Toast.makeText(ExerciseSelectActivity.this, "workout saved" , Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ExerciseSelectActivity.this, EditWorkoutTemplateActivity.class);
                            try {
                                tempEx.setId(response.getLong("id"));
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            EditTemplateDataHolder.exerciseList.add(tempEx);
                            intent.putExtra("item added", true);
                            startActivity(intent);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Response Error", "Error: " + error.getMessage());
                        Toast.makeText(ExerciseSelectActivity.this, "Request failed.", Toast.LENGTH_SHORT).show();
                    }
                }) {
        };

        // Add the request to the request queue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    /**
     * Fetches exercise data from the server, populates the repository,
     * and updates the exercise list and dropdown menu.
     */
    private void fetchExercises() {
        String url = "http://coms-3090-035.class.las.iastate.edu:8080/workouts/database/exercises";
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null, // Pass null as the request body since it's a GET request
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("Volley Response", response.toString());

                        // copy response into local exercise repo
                        exerciseRepo = new ArrayList<>(); // reset exercise repo
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                exerciseRepo.add(response.getJSONObject(i));
                            }
                        } catch (JSONException e) {
                            Log.e("Exercise Repo Parse Error", e.toString());
                        }
                        Log.d("Exercise Repo", exerciseRepo.toString());

                        // update exerciseList and adapter once data is received
                        try {
                            muscleGroups = getMuscleGroups();
                            setExerciseObjs("all");
                            setDropDownMenu();
                        } catch(JSONException e) {
                            Log.e("Error setting exercise", e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", error.toString());
                        Toast.makeText(ExerciseSelectActivity.this, "Connection Error", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
//                headers.put("Authorization", "Bearer YOUR_ACCESS_TOKEN");
//                headers.put("Content-Type", "application/json");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
//                params.put("param1", "value1");
//                params.put("param2", "value2");
                return params;
            }
        };

        // Add the request to the queue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
}
