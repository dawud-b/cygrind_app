package com.example.androidexample.Profile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidexample.HomeActivity;
import com.example.androidexample.R;
import com.example.androidexample.URLManager;

import org.json.JSONObject;
import android.content.SharedPreferences;

/**
 * This activity displays the user's profile information, including their name, age, weight, height, email,
 * and phone number. The user can view their details and navigate to the Edit Profile Activity or back to the Home Activity.
 * <p>
 * The profile information is fetched from the server when the activity is created, and displayed in the respective
 * TextView elements. The user has the option to navigate to the EditProfileActivity to update their profile information
 * or to the HomeActivity using the back button.
 */
public class ProfileActivity extends AppCompatActivity {

    private TextView nameText;   // TextView to display the user's full name
    private TextView ageText;    // TextView to display the user's age
    private TextView weightText; // TextView to display the user's weight
    private TextView heightText; // TextView to display the user's height
    private TextView emailText;  // TextView to display the user's email address
    private TextView phoneText;  // TextView to display the user's phone number
    private Button backButton;   // Button to navigate back to the Home Activity
    private Button editButton;   // Button to navigate to the Edit Profile Activity

    private String username;     // The username of the currently logged-in user

    /**
     * Called when the activity is first created. This method sets up the UI, fetches the user's profile data
     * from the server, and sets up listeners for the back and edit buttons.
     *
     * @param savedInstanceState The saved instance state if the activity is being recreated.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);  // Link to the profile activity layout

        // Initialize UI elements
        backButton = findViewById(R.id.profile_back);
        editButton = findViewById(R.id.edit_profile_btn);
        nameText = findViewById(R.id.display_name);
        ageText = findViewById(R.id.display_age);
        weightText = findViewById(R.id.display_weight);
        heightText = findViewById(R.id.display_height);
        emailText = findViewById(R.id.editTextEmailAddress);
        phoneText = findViewById(R.id.editTextPhone);

        // Fetch user data from the server
        fetchUserData();

        // Set up listeners for the buttons
        setupButtonListeners();
    }

    /**
     * Sets up the listeners for the back and edit buttons.
     * The back button navigates to the HomeActivity, while the edit button navigates to the EditProfileActivity.
     */
    private void setupButtonListeners() {
        // Back button listener
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        // Edit button listener
        editButton.setOnClickListener(v -> {
            if (editButton != null) {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(ProfileActivity.this, "Button is null", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Fetches the user's profile data from the server using the stored username in SharedPreferences.
     * The data is then displayed in the respective TextViews on the profile screen.
     */
    private void fetchUserData() {
        // Get username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        if (username == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request queue for network requests
        RequestQueue queue = Volley.newRequestQueue(this);

        // Construct the URL for the user data request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URLManager.USERS_URL + "/" + username, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Extract data from the JSON response with null checks and default values
                            String firstName = response.optString("firstName", "Not Set");
                            String lastName = response.optString("lastName", "Not Set");
                            int age = response.optInt("age", 0);
                            int height = response.optInt("height", 0);
                            int weight = response.optInt("weight", 0);
                            String email = response.optString("email", "Not Set");
                            String phone = response.optString("telephone", "Not Set");

                            // Set the text views with the fetched data
                            nameText.setText(firstName.equals("Not Set") && lastName.equals("Not Set")
                                    ? "Name Not Set"
                                    : firstName + " " + lastName);

                            ageText.setText(age > 0 ? age + " years" : "Age Not Set");

                            heightText.setText(height > 0
                                    ? height / 12 + "ft " + height % 12 + "in"
                                    : "Height Not Set");

                            weightText.setText(weight > 0 ? weight + " lbs" : "Weight Not Set");

                            emailText.setText(email);
                            phoneText.setText(phone);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ProfileActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.e("VolleyError", "Error: " + error.toString());

                        // Handle network response errors
                        if (error.networkResponse != null) {
                            Log.e("VolleyError", "Status Code: " + error.networkResponse.statusCode);
                        }

                        Toast.makeText(ProfileActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the request queue
        queue.add(request);
    }
}
