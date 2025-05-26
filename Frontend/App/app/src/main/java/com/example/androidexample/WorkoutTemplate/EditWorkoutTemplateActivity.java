package com.example.androidexample.WorkoutTemplate;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.androidexample.ExerciseSelection.ExerciseSelectActivity;
import com.example.androidexample.R;
import com.example.androidexample.URLManager;
import com.example.androidexample.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for editing a workout template, including modifying the title, description,
 * and associated exercise templates.
 *
 * <p>This activity interacts with {@link EditTemplateDataHolder} to persist
 * state during edits and supports updating templates via API requests.
 *
 * <p>It supports both trainer and non-trainer workflows and will attach trainer information
 * to the update request if the current user is a trainer.
 */
public class EditWorkoutTemplateActivity extends AppCompatActivity implements WorkoutTemplateExerciseAdapter.OnExerciseListener{

    private static EditText editTitle, editDescription;
    private RecyclerView exerciseListRecyclerView;
    private WorkoutTemplateExerciseAdapter workoutTemplateExerciseAdapter;
    private Button addExerciseButton, saveButton;

    /**
     * Initializes the activity, populates the form with existing data from {@link EditTemplateDataHolder},
     * sets up listeners for updating the template, and configures RecyclerView with the adapter.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_workout_template);

        // Initialize the exercise list and adapter
        workoutTemplateExerciseAdapter = new WorkoutTemplateExerciseAdapter(EditTemplateDataHolder.exerciseList, this);

        // Initialize views
        editTitle = findViewById(R.id.editTitle);
        editDescription = findViewById(R.id.editDescription);
        exerciseListRecyclerView = findViewById(R.id.templateList);
        addExerciseButton = findViewById(R.id.addExerciseButton);
        saveButton = findViewById(R.id.saveButton);

        exerciseListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        exerciseListRecyclerView.setAdapter(workoutTemplateExerciseAdapter);

        // configure data holder
        Bundle extras = getIntent().getExtras();
        if( extras != null ) {
            // opening new template
            if( extras.getBoolean("fetch exercises")) {
                fetchExerciseTemplates();
            }
            // returning from exercise select
            else if( extras.getBoolean("item added")) {
                workoutTemplateExerciseAdapter.notifyDataSetChanged();
            }
        }

        // sets title and description from data holder
        loadWorkoutTemplateData();

        // opend exercise selection activity
        addExerciseButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditWorkoutTemplateActivity.this, ExerciseSelectActivity.class);
            startActivity(intent);
        });

        // Save button listener
        saveButton.setOnClickListener(v -> {
            saveTemplate();
            /*try {
                saveTemplate();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } this is never thrown so can remove i think */
        });

        // save title to data holder when changed
        editTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // save title
                EditTemplateDataHolder.title = editTitle.getText().toString();
            }
        });

        // save description to data holder when changed
        editDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // save title
                EditTemplateDataHolder.description = editDescription.getText().toString();
            }
        });
    }

    private void loadWorkoutTemplateData() {
        // load title and description data passed previous activity
        editTitle.setText(EditTemplateDataHolder.title);
        editDescription.setText(EditTemplateDataHolder.description);
    }


    /**
     * Saves the workout template by sending a PUT request to the server with updated data.
     * Includes user and optionally trainer metadata in the request payload.
     */
    private void saveTemplate() {
        try {
            // Check if workoutTemplate is null
            if (EditTemplateDataHolder.workoutTemplate == null) {
                Log.e("EditWorkoutTemplate", "Error: workoutTemplate is null");
                Toast.makeText(EditWorkoutTemplateActivity.this, "Error: No template data available", Toast.LENGTH_SHORT).show();
                return;
            }

            // Log the data we're about to send
            Log.d("EditWorkoutTemplate", "Template ID: " + EditTemplateDataHolder.workoutTemplate.getId());
            Log.d("EditWorkoutTemplate", "Template Title: " + EditTemplateDataHolder.title);
            Log.d("EditWorkoutTemplate", "Template Description: " + EditTemplateDataHolder.description);

            String url = URLManager.WORKOUT_TEMPLATE_URL + "/" + EditTemplateDataHolder.workoutTemplate.getId();
            final JSONObject jsonObject = new JSONObject();

            // Add required fields
            jsonObject.put("description", EditTemplateDataHolder.description);
            jsonObject.put("templateName", EditTemplateDataHolder.title);

            // Include the user information in the PUT request
            JSONObject userJson = new JSONObject();
            final SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            long userId = sharedPreferences.getLong("id", -1);
            userJson.put("id", userId);
            jsonObject.put("user", userJson);

            // If workoutTemplate has a date field, include it
            if (EditTemplateDataHolder.workoutTemplate.getDate() != null) {
                jsonObject.put("date", EditTemplateDataHolder.workoutTemplate.getDate());
            }

            // Check if this user is a trainer
            final String username = sharedPreferences.getString("username", "");

            // First check if the user is a trainer
            String trainerCheckUrl = URLManager.getBaseUrl() + "/users/" + username + "/is-trainer";

            StringRequest checkTrainerRequest = new StringRequest(
                    Request.Method.GET,
                    trainerCheckUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            boolean isTrainer = Boolean.parseBoolean(response.trim());
                            Log.d("EditWorkoutTemplate", "Is trainer check response: " + response + ", parsed as: " + isTrainer);

                            if (isTrainer) {
                                // User is a trainer, get trainer ID and update the template
                                getTrainerIdAndUpdateTemplate(username, jsonObject, url);
                            } else {
                                // User is not a trainer, proceed with normal update
                                sendTemplateUpdateRequest(url, jsonObject);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("EditWorkoutTemplate", "Error checking trainer status: " + error.toString());
                            // Proceed with normal update as fallback
                            sendTemplateUpdateRequest(url, jsonObject);
                        }
                    });

            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(checkTrainerRequest);

        } catch (JSONException e) {
            // Log the specific JSON exception
            Log.e("EditWorkoutTemplate", "JSONException in saveTemplate: " + e.getMessage(), e);
            Toast.makeText(EditWorkoutTemplateActivity.this,
                    "Error creating template data: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            // Catch any other exceptions
            Log.e("EditWorkoutTemplate", "Exception in saveTemplate: " + e.getMessage(), e);
            Toast.makeText(EditWorkoutTemplateActivity.this,
                    "Unexpected error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * If the current user is a trainer, fetches the trainer ID and attaches it to the JSON update.
     * Otherwise, continues with the update using user-only metadata.
     *
     * @param username   Username of the currently logged-in user.
     * @param jsonObject JSON object containing the update data.
     * @param url        Endpoint to which the PUT request is made.
     */
    private void getTrainerIdAndUpdateTemplate(final String username, final JSONObject jsonObject, final String url) {
        String trainerUrl = URLManager.getBaseUrl() + "/trainers";

        JsonArrayRequest trainerRequest = new JsonArrayRequest(
                Request.Method.GET,
                trainerUrl,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Find trainer ID by matching username
                            int trainerId = -1;
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject trainerObj = response.getJSONObject(i);
                                if (trainerObj.has("user")) {
                                    JSONObject userObj = trainerObj.getJSONObject("user");
                                    if (username.equals(userObj.optString("username"))) {
                                        trainerId = trainerObj.getInt("id");
                                        Log.d("EditWorkoutTemplate", "Found trainer ID: " + trainerId);
                                        break;
                                    }
                                }
                            }

                            if (trainerId != -1) {
                                try {
                                    // Add trainer information to the JSON
                                    JSONObject trainerJson = new JSONObject();
                                    trainerJson.put("id", trainerId);
                                    jsonObject.put("trainer", trainerJson);
                                    Log.d("EditWorkoutTemplate", "Added trainer info to JSON: " + jsonObject.toString());
                                } catch (JSONException e) {
                                    Log.e("EditWorkoutTemplate", "Error adding trainer to JSON: " + e.getMessage());
                                }
                            } else {
                                Log.w("EditWorkoutTemplate", "Could not find trainer ID for username: " + username);
                            }

                            // Proceed with update regardless of whether we found trainer ID
                            sendTemplateUpdateRequest(url, jsonObject);

                        } catch (JSONException e) {
                            Log.e("EditWorkoutTemplate", "Error parsing trainer response: " + e.getMessage());
                            // Still try to update even if trainer info can't be added
                            sendTemplateUpdateRequest(url, jsonObject);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("EditWorkoutTemplate", "Error fetching trainers: " + error.toString());
                        // Still try to update even if trainer info can't be added
                        sendTemplateUpdateRequest(url, jsonObject);
                    }
                });

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(trainerRequest);
    }

    /**
     * Sends the final template update request to the server via Volley.
     *
     * @param url        The endpoint URL.
     * @param jsonObject The JSON payload for the update.
     */
    private void sendTemplateUpdateRequest(String url, JSONObject jsonObject) {
        // Log the final JSON we're sending
        Log.d("EditWorkoutTemplate", "Sending template update with data: " + jsonObject.toString());

        // Remove trainer field if present, since backend doesn't handle it in PUT request
        try {
            if (jsonObject.has("trainer")) {
                jsonObject.remove("trainer");
                Log.d("EditWorkoutTemplate", "Removed trainer field from request to match backend expectations");
            }
        } catch (Exception e) {
            Log.e("EditWorkoutTemplate", "Error modifying request JSON: " + e.getMessage());
        }

        // Create the request with a StringRequest to handle any response format
        StringRequest putRequest = new StringRequest(
                Request.Method.PUT,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("EditWorkoutTemplate", "Template saved successfully. Response: " +
                                (response.isEmpty() ? "[empty response]" : response));
                        Toast.makeText(EditWorkoutTemplateActivity.this, "Template Saved", Toast.LENGTH_LONG).show();

                        // Navigate back to workout template list
                        Intent intent = new Intent(EditWorkoutTemplateActivity.this, WorkoutTemplateActivity.class);
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Log detailed error information
                        Log.e("EditWorkoutTemplate", "Error saving template: " + error.toString());
                        if (error.networkResponse != null) {
                            Log.e("EditWorkoutTemplate", "Error code: " + error.networkResponse.statusCode);
                            try {
                                String responseData = new String(error.networkResponse.data, "UTF-8");
                                Log.e("EditWorkoutTemplate", "Error data: " + responseData);
                            } catch (Exception e) {
                                Log.e("EditWorkoutTemplate", "Could not parse error data");
                            }
                        }

                        Toast.makeText(EditWorkoutTemplateActivity.this,
                                "Error saving template: " +
                                        (error.getMessage() != null ? error.getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public byte[] getBody() {
                return jsonObject.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public int getMethod() {
                return Method.PUT;
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(putRequest);
    }

    //TODO
    // implement activity of popup that allows users to select from exercises in database
    private void openAddExerciseDialog() {
        // dummy exercise for now
        Intent intent = new Intent(EditWorkoutTemplateActivity.this, ExerciseSelectActivity.class);
        startActivity(intent);
    }

    /**
     * Deletes an exercise template from the server and updates the UI accordingly.
     *
     * @param position Position of the exercise in the list.
     */
    @Override
    public void onDeleteClick(int position) {
        String url = URLManager.EXERCISE_TEMPLATE_URL + "/" + EditTemplateDataHolder.exerciseList.get(position).getId();
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the server's response
                        Log.d("Volley Response", response);
                        // remove workout template from the list
                        EditTemplateDataHolder.exerciseList.remove(position);
                        workoutTemplateExerciseAdapter.notifyDataSetChanged();
                        Toast.makeText(EditWorkoutTemplateActivity.this,"Exercise removed.", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        Log.e("Volley Error", error.toString());
                    }
                });
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }


    /**
     * Allows the user to edit sets, reps, and weight for an exercise in the template via a popup dialog.
     *
     * @param position Position of the exercise in the list.
     */
    @Override
    public void onEditClick(int position) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_input, null);

        final EditText editWeight = dialogView.findViewById(R.id.editWeight);
        final EditText editSets = dialogView.findViewById(R.id.editSets);
        final EditText editReps = dialogView.findViewById(R.id.editReps);

        // Create the AlertDialog using a Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(EditWorkoutTemplateActivity.this);
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
                            Toast.makeText(EditWorkoutTemplateActivity.this,
                                    "Weight: " + weight + "lbs, Sets: " + sets + ", Reps: " + reps,
                                    Toast.LENGTH_SHORT).show();
                            // Show the entered values or do something with them
                            try {
                                int weightInt = Integer.parseInt(weight);
                                int repsInt = Integer.parseInt(reps);
                                int setsInt = Integer.parseInt(sets);

                                putExerciseTemplate(EditTemplateDataHolder.exerciseList.get(position), weightInt, repsInt, setsInt);
                            } catch (NumberFormatException e) {
                                Toast.makeText(EditWorkoutTemplateActivity.this, "Entries must be a number.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // If any field is empty, show an error
                            Toast.makeText(EditWorkoutTemplateActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    /**
     * Sends a PUT request to update a specific exercise template with new sets, reps, and weight values.
     *
     * @param tempEx The exercise template to be updated.
     * @param weight New weight value.
     * @param reps   New reps value.
     * @param sets   New sets value.
     */
    public void putExerciseTemplate(TemplateExercise tempEx, int weight, int reps, int sets) {
        try {
            String url = URLManager.EXERCISE_TEMPLATE_URL + "/" + tempEx.getId();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("weight", weight);
            jsonObject.put("repCount", reps);
            jsonObject.put("setCount", sets);

            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(EditWorkoutTemplateActivity.this, "Exercise Updated", Toast.LENGTH_SHORT).show();
                            tempEx.setWeight(weight);
                            tempEx.setReps(reps);
                            tempEx.setSets(sets);
                            workoutTemplateExerciseAdapter.notifyDataSetChanged();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(com.android.volley.VolleyError error) {
                            // Handle errors
                            Toast.makeText(EditWorkoutTemplateActivity.this, "Connection Error" + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(putRequest);
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        }

    }

    public void fetchExerciseTemplates() {

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                URLManager.getExerciseTempFromWorkoutTempURL(EditTemplateDataHolder.workoutTemplate.getId()),
                null, // Pass null as the request body since it's a GET request
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("Volley Response", response.toString());

                        try {
                            for (int i = 0; i < response.length(); i++) {
                                EditTemplateDataHolder.exerciseList.add(new TemplateExercise((JSONObject) response.get(i)));
                            }
                        } catch (JSONException e) {
                            Log.e("Exercise Template Parse Error", e.toString());
                        }
                        Log.d("Exercise Repo",EditTemplateDataHolder.exerciseList.toString());
                        workoutTemplateExerciseAdapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", error.toString());
                        Toast.makeText(EditWorkoutTemplateActivity.this, "Connection Error", Toast.LENGTH_LONG).show();
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
