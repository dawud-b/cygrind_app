package com.example.androidexample.WorkoutTemplate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.androidexample.HomeActivity;
import com.example.androidexample.R;
import com.example.androidexample.URLManager;
import com.example.androidexample.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code WorkoutTemplateActivity} is an activity that displays a list of workout templates
 * associated with a user. Users can create new workout templates, delete existing ones,
 * or navigate to an edit screen. If the user is a trainer, the template is also registered under
 * their trainer profile on creation.
 *
 * This activity uses a {@link RecyclerView} to show the list of templates and handles network
 * interactions via the Volley library.
 */
public class WorkoutTemplateActivity extends AppCompatActivity implements WorkoutTemplateAdapter.OnWorkoutActionListener {

    private static final String TAG = "WorkoutTemplateActivity";

    private RecyclerView recyclerView;
    private Button workoutAddBtn;
    private Button backBtn;
    private WorkoutTemplateAdapter workoutAdapter;
    private ArrayList<WorkoutTemplate> workoutList;
    private boolean isTrainer = false;
    private int trainerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_template);

        // reset data holder for edit activity
        EditTemplateDataHolder.clearData();

        workoutAddBtn = findViewById(R.id.add_template_btn);
        workoutAddBtn.setText("Create");

        backBtn = findViewById(R.id.workout_template_back_btn);

        recyclerView = findViewById(R.id.templateList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sample data for workouts
        workoutList = new ArrayList<>();

        workoutAdapter = new WorkoutTemplateAdapter(workoutList, this);
        recyclerView.setAdapter(workoutAdapter);

        // Check if the user is a trainer
        SharedPreferences sharedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String username = sharedPrefs.getString("username", "");
        checkIfTrainer(username);

        getWorkoutTemplates();

        workoutAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Create an EditText for the user to enter the template name
                final EditText input = new EditText(WorkoutTemplateActivity.this);
                input.setId(0);
                input.setHint("Enter Template Name");

                // Set up the dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(WorkoutTemplateActivity.this);
                builder.setTitle("Add Workout Template")
                        .setView(input)  // Set the EditText in the dialog
                        .setPositiveButton("Add", (dialog, which) -> {
                            String templateName = input.getText().toString().trim();
                            if (!templateName.isEmpty()) {
                                WorkoutTemplate wrktmp = new WorkoutTemplate(templateName);
                                try {
                                    createWorkoutTemplate(wrktmp);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                Toast.makeText(WorkoutTemplateActivity.this, "Template name cannot be empty!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                // Show the dialog
                builder.show();

            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WorkoutTemplateActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkIfTrainer(String username) {
        String url = URLManager.getBaseUrl() + "/users/" + username + "/is-trainer";

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Parse the response to determine if user is a trainer
                        isTrainer = Boolean.parseBoolean(response);
                        Log.d(TAG, "User is a trainer: " + isTrainer);

                        if (isTrainer) {
                            // If user is a trainer, search for all trainers and find the one matching our username
                            findTrainerId(username);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error checking if user is trainer: " + error.toString());
                        // Default to not a trainer in case of error
                        isTrainer = false;
                    }
                });

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void findTrainerId(String username) {
        // Get all trainers and find the one with matching username
        String url = URLManager.getBaseUrl() + "/trainers";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject trainerObj = response.getJSONObject(i);

                                // Check if the trainer has a user field
                                if (trainerObj.has("user")) {
                                    JSONObject userObj = trainerObj.getJSONObject("user");
                                    String trainerUsername = userObj.getString("username");

                                    // If username matches, get the trainer ID
                                    if (username.equals(trainerUsername)) {
                                        trainerId = trainerObj.getInt("id");
                                        Log.d(TAG, "Found trainer ID: " + trainerId);
                                        break;
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing trainers response: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching trainers: " + error.toString());
                    }
                });

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    //TODO
    // take user to workout template edit activity
    @Override
    public void onEditClick(int position) {
        WorkoutTemplate workout = workoutList.get(position);
        EditTemplateDataHolder.loadData(workout);
        Intent intent = new Intent(WorkoutTemplateActivity.this, EditWorkoutTemplateActivity.class);
        intent.putExtra("fetch exercises", true);
        startActivity(intent);
    }


    //TODO
    // add confirmation popup text and send delete request
    @Override
    public void onDeleteClick(int position) {

        String url = URLManager.WORKOUT_TEMPLATE_URL + "/" + workoutList.get(position).getId();
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the server's response
                        Log.d("Volley Response", response);
                        // remove workout template from the list
                        workoutList.remove(position);
                        workoutAdapter.notifyDataSetChanged();
                        Toast.makeText(WorkoutTemplateActivity.this, "Workout deleted", Toast.LENGTH_SHORT).show();
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
     * Fetches workout templates associated with the user from the backend.
     */
    private void getWorkoutTemplates() {
        SharedPreferences sharedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String username = sharedPrefs.getString("username", "");

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                URLManager.getWorkoutTempFromUserURL(username),
                null, // Pass null as the request body since it's a GET request
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("Volley Response", response.toString());

                        // copy response into local exercise repo
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                workoutList.add(new WorkoutTemplate(response.getJSONObject(i)));
                            }
                        } catch (JSONException e) {
                            Log.e("Workout Template Parse Error", e.toString());
                        }
                        Log.d("Templates Repo", workoutList.toString());

                        // update workout adapter
                        workoutAdapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", error.toString());
                        Toast.makeText(WorkoutTemplateActivity.this, "Connection Error", Toast.LENGTH_LONG).show();
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


    /**
     * Sends a POST request to create a new workout template.
     *
     * @param workoutTemplate The template object to be created.
     * @throws JSONException If JSON creation fails.
     */
    private void createWorkoutTemplate(WorkoutTemplate workoutTemplate) throws JSONException {
        // Create a JSONObject representing the WorkoutTemplate
        JSONObject workoutTemplateJson = new JSONObject();

        workoutTemplateJson.put("templateName", workoutTemplate.getTitle());
        workoutTemplateJson.put("description", workoutTemplate.getDescription());
        //workoutTemplateJson.put("date", workoutTemplate.getDate());

        JSONObject user = new JSONObject();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        user.put("id", sharedPreferences.getLong("id", -1));

        // Add user info if necessary, assume User has getId() method
        workoutTemplateJson.put("user", user);

        System.out.println(workoutTemplateJson.toString());
        Log.d("Send object", workoutTemplateJson.toString());

        String url = URLManager.WORKOUT_TEMPLATE_URL;

        // Create the template via normal user template endpoint
        createTemplateRequest(url, workoutTemplateJson);
    }

    /**
     * Constructs and sends a request to create a new template in the backend.
     *
     * @param url                Endpoint to post the template.
     * @param workoutTemplateJson JSON object containing template data.
     */
    private void createTemplateRequest(String url, JSONObject workoutTemplateJson) {
        // Create JSON object POST request and add to Volley request queue
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                workoutTemplateJson,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response == null) {
                            Log.e("WorkoutTemplateActivity", "Received null response when creating template");
                            Toast.makeText(WorkoutTemplateActivity.this, "Creation failed", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Log the response
                        Log.d("WorkoutTemplateActivity", "Template created successfully: " + response.toString());

                        // Add the new template to the list and update the adapter
                        final WorkoutTemplate createdTemplate = new WorkoutTemplate(response);
                        workoutList.add(createdTemplate);
                        awardTemplatePoints(createdTemplate.getId());
                        workoutAdapter.notifyDataSetChanged();

                        Toast.makeText(WorkoutTemplateActivity.this,
                                "Template created successfully",
                                Toast.LENGTH_SHORT).show();

                        // If user is a trainer, also add this template to their trainer templates
                        if (isTrainer && trainerId > 0) {
                            try {
                                // Create request to add template to trainer's templates
                                String trainerUrl = URLManager.getBaseUrl() + "/trainers/" + trainerId + "/templates";

                                // Create JSON for the request - keep it minimal to match backend expectations
                                JSONObject requestJson = new JSONObject();
                                requestJson.put("templateName", createdTemplate.getTitle());
                                requestJson.put("description", createdTemplate.getDescription());

                                // Add trainer info
                                JSONObject trainerJson = new JSONObject();
                                trainerJson.put("id", trainerId);
                                requestJson.put("trainer", trainerJson);

                                // Get the ID from the created template
                                long templateId = createdTemplate.getId();
                                if (templateId > 0) {
                                    requestJson.put("id", templateId);
                                }

                                Log.d("WorkoutTemplateActivity", "Adding template to trainer with data: " + requestJson.toString());

                                JsonObjectRequest trainerRequest = new JsonObjectRequest(
                                        Request.Method.POST,
                                        trainerUrl,
                                        requestJson,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                Log.d("WorkoutTemplateActivity", "Template added to trainer successfully: " + response.toString());
                                                Toast.makeText(WorkoutTemplateActivity.this,
                                                        "Template also added to your trainer profile",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.e("WorkoutTemplateActivity", "Failed to add template to trainer: " + error.toString());
                                                if (error.networkResponse != null) {
                                                    Log.e("WorkoutTemplateActivity", "Error code: " + error.networkResponse.statusCode);
                                                    Log.e("WorkoutTemplateActivity", "Error data: " + new String(error.networkResponse.data));
                                                }
                                                Toast.makeText(WorkoutTemplateActivity.this,
                                                        "Template created but couldn't add to trainer profile",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(trainerRequest);
                            } catch (JSONException e) {
                                Log.e("WorkoutTemplateActivity", "Error creating JSON for trainer template: " + e.getMessage());
                                Toast.makeText(WorkoutTemplateActivity.this,
                                        "Template created but couldn't add to trainer profile due to JSON error",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("WorkoutTemplateActivity", "Error creating template: " + error.getMessage());
                        if (error.networkResponse != null) {
                            Log.e("WorkoutTemplateActivity", "Error code: " + error.networkResponse.statusCode);
                            try {
                                String errorData = new String(error.networkResponse.data, "UTF-8");
                                Log.e("WorkoutTemplateActivity", "Error data: " + errorData);
                            } catch (Exception e) {
                                Log.e("WorkoutTemplateActivity", "Could not parse error response data");
                            }
                        }

                        Toast.makeText(WorkoutTemplateActivity.this,
                                "Request failed: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // Set a higher timeout for the request
        request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                30000, // 30 seconds timeout
                1,     // Max retries
                1.0f   // Backoff multiplier
        ));

        // Add the request to the request queue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void awardTemplatePoints(Long templateId) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");
        String url = URLManager.getTemplatePointRewardURL(username, templateId);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                response -> {
                    try {
                        String status = response.getString("status");
                        if ("success".equals(status)) {
                            int pointsAwarded = response.getInt("pointsAwarded");
                            int totalPoints = response.getInt("totalPoints");
                            Toast.makeText(this, "Points awarded: " + pointsAwarded + "\nTotal: " + totalPoints, Toast.LENGTH_LONG).show();
                        } else {
                            String message = response.getString("message");
                            Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "JSON parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                }
        ) {
            // Optional: Add headers for authentication if required
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer YOUR_TOKEN_HERE"); // If needed
                return headers;
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonRequest);
    }

    private void addTemplateToTrainer(WorkoutTemplate template) {
        try {
            // Create request to add template to trainer's templates
            String url = URLManager.getBaseUrl() + "/trainers/" + trainerId + "/templates";

            // Create JSON for the request
            JSONObject requestJson = new JSONObject();
            requestJson.put("templateName", template.getTitle());
            requestJson.put("description", template.getDescription());

            // If the template has a date, include it
            if(template.getDate() != null && !template.getDate().isEmpty()) {
                requestJson.put("date", template.getDate());
            }

            // Add trainer info
            JSONObject trainerJson = new JSONObject();
            trainerJson.put("id", trainerId);
            requestJson.put("trainer", trainerJson);

            // If the template has an ID already, include it
            if(template.getId() > 0) {
                requestJson.put("id", template.getId());
            }

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    requestJson,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "Template added to trainer successfully");
                            Toast.makeText(WorkoutTemplateActivity.this,
                                    "Template also added to your trainer profile",
                                    Toast.LENGTH_SHORT).show();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Failed to add template to trainer: " + error.toString());
                        }
                    });

            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for trainer template: " + e.getMessage());
        }
    }
}