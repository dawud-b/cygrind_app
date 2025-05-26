package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.androidexample.Leaderboard.EventActivity;
import com.example.androidexample.Friends.FriendPageActivity;
import com.example.androidexample.Groups.GroupPageActivity;
import com.example.androidexample.Messaging.MessageInboxActivity;
import com.example.androidexample.Payment.PaymentActivity;
import com.example.androidexample.Profile.ProfileActivity;
import com.example.androidexample.Nutrition.NutritionActivity;
import com.example.androidexample.Redemption.RedeemActivity;
import com.example.androidexample.WorkoutTemplate.WorkoutTemplateActivity;
import com.example.androidexample.Trainers.TrainerPageActivity;
import com.example.androidexample.Challenges.ChallengesActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The main landing screen for logged-in users. Displays various navigation options
 * including profile, friends, workouts, trainers, messages, leaderboard, and group pages.
 * Also manages user session data such as username and user ID via SharedPreferences.
 */
public class HomeActivity extends AppCompatActivity {

    // UI elements
    private TextView usernameText;
    private TextView pointsText;
    private ImageButton logOutButton;
    private ImageButton profileButton;
    private ImageButton friendsButton;
    private ImageButton calendarButton;
    private ImageButton trainerButton;
    private ImageButton messageButton;
    private ImageButton workoutButton;
    private Button leaderboardButton;
    private ImageButton nutritionButton; // New button for nutrition feature

    private ImageButton groupButton;
    private ImageButton storeButton;
    private Button premiumButton;

    private Button achievementsButton;

    /**
     * Initializes the activity, sets up all button click listeners,
     * and extracts/stores user data passed from previous activity or preferences.
     *
     * @param savedInstanceState the saved state bundle for this activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_new);

        // Initialize UI components
        usernameText = findViewById(R.id.home_username_txt);
        pointsText = findViewById(R.id.home_points_display);
        profileButton = findViewById(R.id.home_profile);
        logOutButton = findViewById(R.id.home_logout);
        workoutButton = findViewById(R.id.home_workout);
        friendsButton = findViewById(R.id.home_friends);
        calendarButton = findViewById(R.id.home_calendar);
        messageButton = findViewById(R.id.home_messages);
        groupButton = findViewById(R.id.home_groups);
        trainerButton = findViewById(R.id.home_trainer);
        storeButton = findViewById(R.id.home_store);
        leaderboardButton = findViewById(R.id.view_leaderboard_button);
        premiumButton = findViewById(R.id.home_premium);
        achievementsButton = findViewById(R.id.view_achievements_button);
        nutritionButton = findViewById(R.id.home_nutrition_tracker); // Initialize the nutrition button

        // updates points text with user balance
        getBalance();

        // Extract user data and save to SharedPreferences
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String username = extras.getString("username");
            usernameText.setText(username);
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", username);
            storeIdAndEmail(username);
            editor.apply();

            usernameText.setText(username);

            registerLogin(username);
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            usernameText.setText(sharedPreferences.getString("username", ""));
        }

        // Set up navigation listeners
        logOutButton.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));

        profileButton.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        workoutButton.setOnClickListener(v -> startActivity(new Intent(this, WorkoutTemplateActivity.class)));

        friendsButton.setOnClickListener(v -> startActivity(new Intent(this, FriendPageActivity.class)));

        trainerButton.setOnClickListener(v -> startActivity(new Intent(this, TrainerPageActivity.class)));

        premiumButton.setOnClickListener(v -> startActivity(new Intent(this, PaymentActivity.class)));

        storeButton.setOnClickListener(v -> startActivity(new Intent(this, RedeemActivity.class)));

        achievementsButton.setOnClickListener(v -> startActivity(new Intent(this, ChallengesActivity.class)));

        // Add nutrition button click listener
        nutritionButton.setOnClickListener(v -> startActivity(new Intent(this, NutritionActivity.class)));

        leaderboardButton.setOnClickListener(v -> {
            String username = usernameText.getText().toString();
            Intent intent = new Intent(this, EventActivity.class);
            intent.putExtra("EVENT_ID", 1L);
            intent.putExtra("EVENT_TITLE", "Active Event");
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        });

        messageButton.setOnClickListener(v -> startActivity(new Intent(this, MessageInboxActivity.class)));

        groupButton.setOnClickListener(v -> startActivity(new Intent(this, GroupPageActivity.class)));
    }

    /**
     * Sends a GET request to fetch the user ID for a given username and stores it in SharedPreferences.
     *
     * @param username the username for which to retrieve and store the ID
     */
    private void storeIdAndEmail(String username) {
        String url = "http://coms-3090-035.class.las.iastate.edu:8080/users/" + username;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("VolleyResponse", "User ID stored: " + response.toString());
                        try {
                            long userId = response.getLong("id");
                            String email = response.getString("email");
                            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putLong("id", userId);
                            editor.putString("email", email);
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VolleyError", error.toString());
                    }
                });

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    private static final String TAG = "HomeActivity";

    private void getBalance() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");
        String url = URLManager.getPointBalanceURL(username);

        Log.d(TAG, "Fetching point balance for user: " + username);
        Log.d(TAG, "Request URL: " + url);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d(TAG, "Response received: " + response.toString());

                        String status = response.getString("status");
                        if (status.equals("success")) {
                            int points = response.getInt("totalPoints");
                            Log.d(TAG, "User has " + points + " points");
                            pointsText.setText(String.valueOf(points));
                        } else {
                            String message = response.getString("message");
                            Log.e(TAG, "Error response: " + message);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Parsing Error: " + e.getMessage());
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley Error: " + error.toString());
                    error.printStackTrace();
                });

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonRequest);
    }

    private void registerLogin(String username) {
        String url = URLManager.getPointsLoginURL(username);

        /// Create a new POST request (no body needed here, as the username is in the URL)
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle success
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                int pointsAwarded = jsonResponse.getInt("pointsAwarded");
                                int totalPoints = jsonResponse.getInt("totalPoints");

                                Toast.makeText(HomeActivity.this, "Daily login: +10 points!", Toast.LENGTH_SHORT).show();
                                Log.d("Volley", "Points Awarded: " + pointsAwarded);
                                Log.d("Volley", "Total Points: " + totalPoints);
                                pointsText.setText("" + totalPoints);
                            } else {
                                String message = jsonResponse.getString("message");
                                Log.d("Volley", "Error: " + message);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        error.printStackTrace();
                        Log.d("Volley", "Request error: " + error.getMessage());
                    }
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(postRequest);
    }

}