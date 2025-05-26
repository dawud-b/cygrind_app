package com.example.androidexample.Challenges;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
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
import com.android.volley.toolbox.JsonArrayRequest;
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
 * Displays user challenges and allows the user to view and complete challenges.
 * Shows progress on challenge sets and provides interface to mark challenges as complete.
 */
public class ChallengesActivity extends AppCompatActivity {

    private static final String TAG = "ChallengesActivity";

    private TextView titleText;
    private ProgressBar loadingIndicator;
    private RecyclerView challengeSetsRecyclerView;
    private ChallengeSetAdapter challengeSetAdapter;
    private List<ChallengeSetModel> challengeSets;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);

        // Initialize UI components
        titleText = findViewById(R.id.challenges_title);
        loadingIndicator = findViewById(R.id.challenges_loading);
        challengeSetsRecyclerView = findViewById(R.id.challenge_sets_recycler_view);

        // Get username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");

        // Set up RecyclerView
        challengeSets = new ArrayList<>();
        challengeSetAdapter = new ChallengeSetAdapter(this, challengeSets, this::completeChallenge);
        challengeSetsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        challengeSetsRecyclerView.setAdapter(challengeSetAdapter);

        // Load challenge sets
        loadChallengeSets();

        // Set up back button
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
    }

    private void loadChallengeSets() {
        loadingIndicator.setVisibility(View.VISIBLE);

        String url = URLManager.getBaseUrl() + "/challenges/user/" + username;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        loadingIndicator.setVisibility(View.GONE);
                        Log.d(TAG, "Challenge sets received: " + response.toString());
                        parseChallengeSetResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingIndicator.setVisibility(View.GONE);
                        Log.e(TAG, "Error loading challenge sets: " + error.toString());
                        Toast.makeText(ChallengesActivity.this,
                                "Failed to load challenges", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);
    }

    private void parseChallengeSetResponse(JSONArray response) {
        challengeSets.clear();

        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject challengeSetJson = response.getJSONObject(i);

                long id = challengeSetJson.getLong("id");
                String title = challengeSetJson.getString("title");
                double progress = challengeSetJson.getDouble("progress");
                int challengesCompleted = challengeSetJson.getInt("challengesCompleted");
                int stages = challengeSetJson.getInt("stages");

                List<ChallengeModel> challenges = new ArrayList<>();
                JSONArray challengesArray = challengeSetJson.getJSONArray("challenges");

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

                challengeSets.add(new ChallengeSetModel(
                        id,
                        title,
                        progress,
                        challengesCompleted,
                        stages,
                        challenges));
            }

            challengeSetAdapter.notifyDataSetChanged();

            if (challengeSets.isEmpty()) {
                Toast.makeText(this, "No challenge sets available", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing challenge sets: " + e.getMessage());
            Toast.makeText(this, "Error parsing challenge data", Toast.LENGTH_SHORT).show();
        }
    }

    private void completeChallenge(long challengeSetId, int position) {
        String url = URLManager.getBaseUrl() + "/challenges/" + challengeSetId + "/complete";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Challenge completion response: " + response.toString());
                        try {
                            // Update local challenge set model
                            ChallengeSetModel updatedSet = parseUpdatedChallengeSet(response);
                            challengeSets.set(position, updatedSet);
                            challengeSetAdapter.notifyItemChanged(position);

                            // Find which challenge was just completed
                            int completedIndex = -1;
                            List<ChallengeModel> challenges = updatedSet.getChallenges();
                            for (int i = 0; i < challenges.size(); i++) {
                                if (challenges.get(i).isCompleted() &&
                                        i == updatedSet.getChallengesCompleted() - 1) {
                                    completedIndex = i;
                                    break;
                                }
                            }

                            if (completedIndex >= 0) {
                                ChallengeModel completedChallenge = challenges.get(completedIndex);
                                Toast.makeText(ChallengesActivity.this,
                                        "Challenge completed! +" + completedChallenge.getPoints() + " points",
                                        Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing challenge completion: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error completing challenge: " + error.toString());
                        Toast.makeText(ChallengesActivity.this,
                                "Failed to complete challenge", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    private ChallengeSetModel parseUpdatedChallengeSet(JSONObject challengeSetJson) throws JSONException {
        long id = challengeSetJson.getLong("id");
        String title = challengeSetJson.getString("title");
        double progress = challengeSetJson.getDouble("progress");
        int challengesCompleted = challengeSetJson.getInt("challengesCompleted");
        int stages = challengeSetJson.getInt("stages");

        List<ChallengeModel> challenges = new ArrayList<>();
        JSONArray challengesArray = challengeSetJson.getJSONArray("challenges");

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

        return new ChallengeSetModel(id, title, progress, challengesCompleted, stages, challenges);
    }
}