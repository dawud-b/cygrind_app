package com.example.androidexample.Challenges;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidexample.R;
import com.example.androidexample.URLManager;
import com.example.androidexample.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for viewing details of a specific challenge set
 */
public class ChallengeDetailActivity extends AppCompatActivity {

    private static final String TAG = "ChallengeDetailActivity";

    private TextView titleText;
    private TextView progressText;
    private ProgressBar progressBar;
    private RecyclerView challengesRecyclerView;
    private Button completeButton;
    private ProgressBar loadingIndicator;

    private ChallengeAdapter challengeAdapter;
    private List<ChallengeModel> challenges;
    private long challengeSetId;
    private ChallengeSetModel challengeSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_detail);

        // Initialize UI components
        titleText = findViewById(R.id.challenge_detail_title);
        progressText = findViewById(R.id.challenge_detail_progress_text);
        progressBar = findViewById(R.id.challenge_detail_progress_bar);
        challengesRecyclerView = findViewById(R.id.challenge_detail_recycler_view);
        completeButton = findViewById(R.id.challenge_detail_complete_button);
        loadingIndicator = findViewById(R.id.challenge_detail_loading);

        // Get challenge set ID from intent
        challengeSetId = getIntent().getLongExtra("CHALLENGE_SET_ID", -1);
        if (challengeSetId == -1) {
            Toast.makeText(this, "Error: Challenge set not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up RecyclerView
        challenges = new ArrayList<>();
        challengeAdapter = new ChallengeAdapter(this, challenges);
        challengesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        challengesRecyclerView.setAdapter(challengeAdapter);

        // Set up back button
        findViewById(R.id.challenge_detail_back_button).setOnClickListener(v -> finish());

        // Set up complete button
        completeButton.setOnClickListener(v -> completeNextChallenge());

        // Load challenge set details
        loadChallengeSetDetails();
    }

    private void loadChallengeSetDetails() {
        loadingIndicator.setVisibility(View.VISIBLE);

        String url = URLManager.getBaseUrl() + "/challenges/" + challengeSetId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingIndicator.setVisibility(View.GONE);
                        Log.d(TAG, "Challenge set details received: " + response.toString());
                        parseChallengeSetResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingIndicator.setVisibility(View.GONE);
                        Log.e(TAG, "Error loading challenge set details: " + error.toString());
                        Toast.makeText(ChallengeDetailActivity.this,
                                "Failed to load challenge details", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    private void parseChallengeSetResponse(JSONObject response) {
        try {
            long id = response.getLong("id");
            String title = response.getString("title");
            double progress = response.getDouble("progress");
            int challengesCompleted = response.getInt("challengesCompleted");
            int stages = response.getInt("stages");

            challenges.clear();
            JSONArray challengesArray = response.getJSONArray("challenges");

            for (int j = 0; j < challengesArray.length(); j++) {
                JSONObject challengeJson = challengesArray.getJSONObject(j);

                long challengeId = challengeJson.getLong("id");
                String challengeTitle = challengeJson.getString("title");
                String description = challengeJson.getString("description");
                int points = challengeJson.getInt("points");
                boolean completed = challengeJson.getBoolean("completed");

                challenges.add(new ChallengeModel(
                        challengeId,
                        challengeTitle,
                        description,
                        points,
                        completed));
            }

            challengeSet = new ChallengeSetModel(id, title, progress, challengesCompleted, stages, challenges);

            // Update UI
            titleText.setText(title);
            progressText.setText(String.format("Progress: %d/%d (%s)",
                    challengesCompleted, stages, ChallengeUtils.formatProgress(progress)));
            progressBar.setMax(100);
            progressBar.setProgress((int)(progress * 100));

            challengeAdapter.notifyDataSetChanged();

            updateCompleteButton();

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing challenge set details: " + e.getMessage());
            Toast.makeText(this, "Error parsing challenge data", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCompleteButton() {
        ChallengeModel nextChallenge = challengeSet.getNextChallenge();
        if (nextChallenge != null && !nextChallenge.isCompleted()) {
            completeButton.setEnabled(true);
            completeButton.setText("Complete: " + nextChallenge.getTitle());
        } else {
            completeButton.setEnabled(false);
            completeButton.setText("All challenges completed!");
        }
    }

    private void completeNextChallenge() {
        loadingIndicator.setVisibility(View.VISIBLE);

        String url = URLManager.getCompleteChallengeURL(challengeSetId);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingIndicator.setVisibility(View.GONE);
                        Log.d(TAG, "Challenge completion response: " + response.toString());
                        try {
                            parseChallengeSetResponse(response);

                            // Find which challenge was just completed
                            int completedIndex = -1;
                            for (int i = 0; i < challenges.size(); i++) {
                                if (challenges.get(i).isCompleted() &&
                                        i == challengeSet.getChallengesCompleted() - 1) {
                                    completedIndex = i;
                                    break;
                                }
                            }

                            if (completedIndex >= 0) {
                                ChallengeModel completedChallenge = challenges.get(completedIndex);
                                Toast.makeText(ChallengeDetailActivity.this,
                                        "Challenge completed! +" + completedChallenge.getPoints() + " points",
                                        Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing challenge completion: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingIndicator.setVisibility(View.GONE);
                        Log.e(TAG, "Error completing challenge: " + error.toString());
                        Toast.makeText(ChallengeDetailActivity.this,
                                "Failed to complete challenge", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }
}