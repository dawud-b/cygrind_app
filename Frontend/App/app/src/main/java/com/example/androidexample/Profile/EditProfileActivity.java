package com.example.androidexample.Profile;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidexample.LoginActivity;
import com.example.androidexample.R;
import com.example.androidexample.URLManager;

import org.json.JSONObject;
import org.json.JSONException;
/**
 * This activity allows users to edit their profile details, such as their name, age, weight, height, email,
 * and phone number. It also provides options to change the user's password, delete the account, and navigate
 * back to the profile view.
 * <p>
 * The activity fetches the user's existing data from the server, displays it in editable fields, and sends
 * updated data to the server when the user saves their changes. It also includes functionality for account deletion.
 */
public class EditProfileActivity extends AppCompatActivity {

    private EditText editableNameText;  // Editable field for the user's name
    private EditText editableAgeText;   // Editable field for the user's age
    private EditText editableWeightText; // Editable field for the user's weight
    private EditText editableHeightText; // Editable field for the user's height
    private EditText editableEmailText;  // Editable field for the user's email
    private EditText editablePhoneText;  // Editable field for the user's phone number
    private Button saveButton;           // Button to save changes
    private Button backButton;           // Button to go back to the profile
    private Button changePasswordButton; // Button to change password
    private Button deleteAccountButton;  // Button to delete account
    private RequestQueue requestQueue;   // Volley request queue for network requests
    private String originalEmail = "";   // Stores the original email to check for updates
    private String username;             // The username of the currently logged-in user

    /**
     * Called when the activity is first created. It initializes the UI components, retrieves the username
     * from shared preferences, and fetches the user's current profile data.
     *
     * @param savedInstanceState The saved instance state if the activity is being recreated.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize UI elements
        initializeViews();

        // Get username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        this.username = sharedPreferences.getString("username", "");

        // Validate username
        if (username.isEmpty()) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        requestQueue = Volley.newRequestQueue(this);

        // Fetch user data from the server
        fetchUserData();

        // Set up button listeners
        setupButtonListeners();
    }

    /**
     * Initializes the UI components (EditText fields and Buttons).
     */
    private void initializeViews() {
        editableNameText = findViewById(R.id.name_editable);
        editableAgeText = findViewById(R.id.age_editable);
        editableWeightText = findViewById(R.id.weight_editable);
        editableHeightText = findViewById(R.id.height_editable);
        editableEmailText = findViewById(R.id.editTextEmailAddress);
        editablePhoneText = findViewById(R.id.editTextPhone);
        saveButton = findViewById(R.id.save_btn);
        backButton = findViewById(R.id.edit_back);
        changePasswordButton = findViewById(R.id.edit_password_btn);
        deleteAccountButton = findViewById(R.id.delete_acc_btn);
    }

    /**
     * Sets up the button listeners for the save, back, change password, and delete account buttons.
     */
    private void setupButtonListeners() {
        saveButton.setOnClickListener(v -> sendUpdateRequest());

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        deleteAccountButton.setOnClickListener(v -> deleteUserAccount());
    }

    /**
     * Shows a confirmation dialog before deleting the user's account.
     */
    private void deleteUserAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> sendDeleteRequest())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Sends a request to delete the user's account.
     */
    private void sendDeleteRequest() {
        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "No user to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        String deleteUrl = URLManager.USERS_URL + "/" + username;

        StringRequest deleteRequest = new StringRequest(
                Request.Method.DELETE,
                deleteUrl,
                response -> {
                    Log.d("DeleteRequest", "Delete Response: " + response);
                    // Clear SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    sharedPreferences.edit().clear().apply();

                    Toast.makeText(EditProfileActivity.this, "Account Deleted Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    Log.e("DeleteError", "Error details:", error);
                    if (error.networkResponse != null) {
                        Log.e("DeleteError", "Status Code: " + error.networkResponse.statusCode);
                        try {
                            String errorBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e("DeleteError", "Error Response Body: " + errorBody);
                        } catch (Exception e) {
                            Log.e("DeleteError", "Error parsing error response", e);
                        }
                    }
                    Toast.makeText(EditProfileActivity.this, "Delete Failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        );

        requestQueue.add(deleteRequest);
    }

    /**
     * Fetches the user's data from the server and populates the input fields.
     */
    private void fetchUserData() {
        String url = URLManager.USERS_URL + "/" + username;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        // Extract data from the response JSON object
                        String firstName = response.optString("firstName", "");
                        String lastName = response.optString("lastName", "");
                        int age = response.optInt("age", 0);
                        int height = response.optInt("height", 0);
                        int weight = response.optInt("weight", 0);
                        String email = response.optString("email", "");
                        String phone = response.optString("telephone", "");

                        // Store the original email for later comparison
                        originalEmail = email;

                        // Set the values in the editable fields
                        editableNameText.setText(formatName(firstName, lastName));
                        editableAgeText.setText(age > 0 ? age + " years" : "");
                        editableHeightText.setText(formatHeight(height));
                        editableWeightText.setText(weight > 0 ? weight + " lbs" : "");
                        editableEmailText.setText(email);
                        editablePhoneText.setText(phone);

                    } catch (Exception e) {
                        Log.e("FetchUserData", "Error parsing user data", e);
                        Toast.makeText(EditProfileActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> handleNetworkError(error, "Error fetching data")
        );

        requestQueue.add(request);
    }

    /**
     * Sends the updated profile data to the server.
     */
    private void sendUpdateRequest() {
        if (!validateInputs()) {
            return;
        }

        String putUrl = URLManager.USERS_URL + "/" + username;

        JSONObject jsonBody = new JSONObject();
        try {
            // Name
            String[] nameParts = parseNameInput();
            jsonBody.put("firstName", nameParts[0]);
            jsonBody.put("lastName", nameParts[1]);

            // Age
            int age = parseAge();
            if (age > 0) jsonBody.put("age", age);

            // Weight
            int weight = parseWeight();
            if (weight > 0) jsonBody.put("weight", weight);

            // Height
            int height = parseHeight();
            if (height > 0) jsonBody.put("height", height);

            // Email - only include if changed
            String currentEmail = editableEmailText.getText().toString().trim();
            if (!currentEmail.equals(originalEmail)) {
                jsonBody.put("email", currentEmail);
            }

            // Phone
            String phone = editablePhoneText.getText().toString().trim();
            if (!phone.isEmpty()) jsonBody.put("telephone", phone);

        } catch (JSONException e) {
            Log.e("UpdateRequest", "Error creating JSON body", e);
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send the update request using PUT method
        StringRequest putRequest = new StringRequest(
                Request.Method.PUT,
                putUrl,
                response -> {
                    Log.d("UpdateRequest", "Update Response: " + response);
                    Toast.makeText(EditProfileActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("UpdateError", "Error details:", error);
                    if (error.networkResponse != null) {
                        Log.e("UpdateError", "Status Code: " + error.networkResponse.statusCode);
                        try {
                            String errorBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e("UpdateError", "Error Response Body: " + errorBody);
                        } catch (Exception e) {
                            Log.e("UpdateError", "Error parsing error response", e);
                        }
                    }
                    Toast.makeText(EditProfileActivity.this, "Update Failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return jsonBody.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        requestQueue.add(putRequest);
    }

    /**
     * Validates the user's input for email and phone number.
     *
     * @return true if the inputs are valid, false otherwise.
     */
    private boolean validateInputs() {
        // Email validation
        String email = editableEmailText.getText().toString().trim();
        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editableEmailText.setError("Invalid email address");
            return false;
        }

        // Phone validation (basic)
        String phone = editablePhoneText.getText().toString().trim();
        if (!phone.isEmpty() && !Patterns.PHONE.matcher(phone).matches()) {
            editablePhoneText.setError("Invalid phone number");
            return false;
        }

        return true;
    }

    /**
     * Parses the name input into first and last name.
     *
     * @return a string array containing the first name and last name.
     */
    private String[] parseNameInput() {
        String nameInput = editableNameText.getText().toString().trim();
        String[] parts = nameInput.split("\\s+", 2);
        return new String[]{
                parts.length > 0 ? parts[0] : "",
                parts.length > 1 ? parts[1] : ""
        };
    }

    /**
     * Parses the age input and returns it as an integer.
     *
     * @return the parsed age.
     */
    private int parseAge() {
        try {
            String ageText = editableAgeText.getText().toString()
                    .replace(" years", "")
                    .trim();
            return ageText.isEmpty() ? 0 : Integer.parseInt(ageText);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Parses the weight input and returns it as an integer.
     *
     * @return the parsed weight.
     */
    private int parseWeight() {
        try {
            String weightText = editableWeightText.getText().toString()
                    .replace(" lbs", "")
                    .trim();
            return weightText.isEmpty() ? 0 : Integer.parseInt(weightText);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Parses the height input and returns it in inches.
     *
     * @return the parsed height in inches.
     */
    private int parseHeight() {
        try {
            String heightText = editableHeightText.getText().toString();
            String[] parts = heightText.split("ft|in");
            if (parts.length == 2) {
                int feet = Integer.parseInt(parts[0].trim());
                int inches = Integer.parseInt(parts[1].trim());
                return feet * 12 + inches;
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Handles network errors by displaying a Toast message.
     *
     * @param error The VolleyError received from the network request.
     * @param defaultMessage The default message to display in case of an error.
     */
    private void handleNetworkError(VolleyError error, String defaultMessage) {
        String errorMsg = defaultMessage;
        if (error.networkResponse != null) {
            errorMsg += " (Status Code: " + error.networkResponse.statusCode + ")";
            try {
                String responseBody = new String(error.networkResponse.data, "utf-8");
                Log.e("NetworkError", "Response Body: " + responseBody);
            } catch (Exception e) {
                Log.e("NetworkError", "Error parsing error response", e);
            }
        }
        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        error.printStackTrace();
    }

    /**
     * Formats the user's name by concatenating the first name and last name.
     *
     * @param firstName The user's first name.
     * @param lastName The user's last name.
     * @return The full formatted name.
     */
    private String formatName(String firstName, String lastName) {
        return (firstName + " " + lastName).trim();
    }

    /**
     * Formats the height from inches to feet and inches.
     *
     * @param totalInches The height in inches.
     * @return The formatted height as a string in feet and inches.
     */
    private String formatHeight(int totalInches) {
        if (totalInches <= 0) return "";
        int feet = totalInches / 12;
        int inches = totalInches % 12;
        return feet + "ft " + inches + "in";
    }
}
