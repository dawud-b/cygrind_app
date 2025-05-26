package com.example.androidexample.Trainers;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.androidexample.R;
import com.example.androidexample.URLManager;
import com.example.androidexample.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
/**
 * Activity for viewing a trainer's profile, including their bio, specialization,
 * and workout templates they've created. Allows the user to follow or unfollow a trainer.
 * <p>
 * The activity fetches and displays detailed information about a trainer, such as:
 * - The trainer's name (displayed from the user's first and last name or username)
 * - Specialization and bio
 * - A list of workout templates created by the trainer.
 * <p>
 * Users can also follow or unfollow the trainer through the follow/unfollow button.
 * </p>
 */
public class TrainerProfileActivity extends AppCompatActivity {

    private static final String TAG = "TrainerProfileActivity";

    private TextView trainerNameTextView;   // TextView for displaying trainer's name
    private TextView specializationTextView; // TextView for displaying trainer's specialization
    private TextView bioTextView;            // TextView for displaying trainer's bio
    private Button followUnfollowButton;     // Button to follow or unfollow the trainer
    private RecyclerView templatesRecyclerView; // RecyclerView to display trainer's workout templates

    private long trainerId;  // ID of the trainer
    private Trainer trainer;  // Trainer object holding the trainer's data
    private boolean isFollowing = false; // Flag indicating whether the user is following the trainer

    /**
     * Called when the activity is created. Initializes the UI components,
     * retrieves the trainer's data from the server, and sets up follow/unfollow functionality.
     *
     * @param savedInstanceState The saved instance state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_profile);

        // Get trainer ID from the intent
        trainerId = getIntent().getLongExtra("TRAINER_ID", -1);
        if (trainerId == -1) {
            Toast.makeText(this, "Error: No trainer specified", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        trainerNameTextView = findViewById(R.id.trainerName);
        specializationTextView = findViewById(R.id.trainerSpecialization);
        bioTextView = findViewById(R.id.trainerBio);
        followUnfollowButton = findViewById(R.id.followUnfollowButton);
        templatesRecyclerView = findViewById(R.id.templatesRecyclerView);

        // Set up RecyclerView for workout templates
        templatesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up follow/unfollow button
        followUnfollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFollowing) {
                    unfollowTrainer();
                } else {
                    followTrainer();
                }
            }
        });

        // Load trainer data
        fetchTrainerData();

        // Check if user is following this trainer
        checkFollowingStatus();
    }

    /**
     * Fetches the trainer's data from the server.
     */
    private void fetchTrainerData() {
        String url = URLManager.getBaseUrl() + "/trainers/" + trainerId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            trainer = new Trainer(response);
                            updateUI();

                            // Now fetch the templates
                            fetchTrainerTemplates();
                        } catch (JSONException e) {
                            Log.e(TAG, "Failed to parse trainer data: " + e.getMessage());
                            Toast.makeText(TrainerProfileActivity.this,
                                    "Failed to load trainer data", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching trainer data: " + error.toString());
                        Toast.makeText(TrainerProfileActivity.this,
                                "Connection Error", Toast.LENGTH_SHORT).show();
                    }
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * Updates the UI with the fetched trainer data.
     */
    private void updateUI() {
        if (trainer != null) {
            trainerNameTextView.setText(trainer.getDisplayName());
            specializationTextView.setText(trainer.getSpecialization());

            if (trainer.getBio() != null && !trainer.getBio().isEmpty()) {
                bioTextView.setText(trainer.getBio());
                bioTextView.setVisibility(View.VISIBLE);
            } else {
                bioTextView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Fetches the workout templates created by the trainer.
     */
    private void fetchTrainerTemplates() {
        String url = URLManager.getBaseUrl() + "/trainers/" + trainerId + "/templates";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Handle templates here
                        // This would require creating a WorkoutTemplate model and adapter
                        // Similar to how we've set up the Trainer classes
                        Log.d(TAG, "Templates received: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching templates: " + error.toString());
                    }
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * Checks if the user is currently following this trainer.
     */
    private void checkFollowingStatus() {
        // Get current username
        String username = getSharedPreferences("MyPrefs", 0).getString("username", "");
        if (username.isEmpty()) {
            return;
        }

        // Fetch followed trainers to check if this trainer is among them
        String url = URLManager.getBaseUrl() + "/users/" + username + "/followed-trainers";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        isFollowing = false;

                        // Check if current trainer is in the list
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject trainerObj = response.getJSONObject(i);
                                long id = trainerObj.getLong("id");
                                if (id == trainerId) {
                                    isFollowing = true;
                                    break;
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing followed trainers: " + e.getMessage());
                            }
                        }

                        // Update button text
                        updateFollowButton();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error checking follow status: " + error.toString());
                    }
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * Updates the text of the follow/unfollow button based on the following status.
     */
    private void updateFollowButton() {
        if (isFollowing) {
            followUnfollowButton.setText("Unfollow");
        } else {
            followUnfollowButton.setText("Follow");
        }
    }

    /**
     * Follows the trainer.
     */
    private void followTrainer() {
        String username = getSharedPreferences("MyPrefs", 0).getString("username", "");
        String url = URLManager.getBaseUrl() + "/trainers/" + trainerId + "/followers/" + username;

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(TrainerProfileActivity.this,
                                "Now following trainer", Toast.LENGTH_SHORT).show();
                        isFollowing = true;
                        updateFollowButton();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error following trainer: " + error.toString());
                        Toast.makeText(TrainerProfileActivity.this,
                                "Connection Error", Toast.LENGTH_SHORT).show();
                    }
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * Unfollows the trainer.
     */
    private void unfollowTrainer() {
        String username = getSharedPreferences("MyPrefs", 0).getString("username", "");
        String url = URLManager.getBaseUrl() + "/trainers/" + trainerId + "/followers/" + username;

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(TrainerProfileActivity.this,
                                "Unfollowed trainer", Toast.LENGTH_SHORT).show();
                        isFollowing = false;
                        updateFollowButton();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error unfollowing trainer: " + error.toString());
                        Toast.makeText(TrainerProfileActivity.this,
                                "Connection Error", Toast.LENGTH_SHORT).show();
                    }
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
