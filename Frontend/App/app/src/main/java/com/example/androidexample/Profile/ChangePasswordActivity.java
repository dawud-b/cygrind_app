package com.example.androidexample.Profile;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidexample.R;
import com.example.androidexample.URLManager;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This activity allows the user to change their password. It provides a user interface where the user
 * can enter their old password, a new password, and confirm the new password. The activity verifies the
 * provided data, including checking the old password, ensuring that the new password meets security requirements,
 * and updating the user's password if the validation passes.
 * <p>
 * The password change functionality includes fetching the user's current password from the server, validating
 * the old password, ensuring the new password meets strength requirements, and then updating the password
 * on the server if all conditions are satisfied.
 */
public class ChangePasswordActivity extends AppCompatActivity {

    private EditText editableOldPasswordText, editableNewPasswordText, editableConfirmPasswordText;
    private Button changePasswordButton;
    private RequestQueue requestQueue;
    private String storedPassword = "";  // Store the user's current password
    private String username;  // The username of the currently logged-in user

    /**
     * Called when the activity is first created. It sets up the UI elements, initializes required components,
     * and handles the user's session.
     *
     * @param savedInstanceState The saved instance state if the activity is being recreated.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Initialize UI elements
        editableOldPasswordText = findViewById(R.id.old_password);
        editableNewPasswordText = findViewById(R.id.new_password);
        editableConfirmPasswordText = findViewById(R.id.confirm_password);
        changePasswordButton = findViewById(R.id.change_password_btn);

        // Get username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        this.username = sharedPreferences.getString("username", "");

        // Validate user session
        if (username.isEmpty()) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        requestQueue = Volley.newRequestQueue(this);

        // Fetch the user's current password from the server
        fetchCurrentPassword();

        // Set up the Change Password Button action
        changePasswordButton.setOnClickListener(v -> verifyAndChangePassword());
    }

    /**
     * Fetches the current password of the user from the server.
     */
    private void fetchCurrentPassword() {
        String url = URLManager.USERS_URL + "/" + username;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    storedPassword = response.optString("password", "");  // Store current password
                },
                error -> Log.e("PasswordFetchError", "Error fetching password", error)
        );

        requestQueue.add(request);
    }

    /**
     * Verifies the provided old password, checks if the new password and confirmation match,
     * and validates the strength of the new password. If all validations pass, it updates the password.
     */
    private void verifyAndChangePassword() {
        String oldPassword = editableOldPasswordText.getText().toString().trim();
        String newPassword = editableNewPasswordText.getText().toString().trim();
        String confirmPassword = editableConfirmPasswordText.getText().toString().trim();

        // Validate inputs
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!oldPassword.equals(storedPassword)) {
            Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!meetsPwdStrength(newPassword)) {
            Toast.makeText(this, "Password strength not met", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send request to update the password
        updatePassword(newPassword);
    }

    /**
     * Validates if the provided password meets the required strength criteria:
     * - At least 8 characters
     * - Contains at least one letter, one digit, and one special character.
     *
     * @param pwd The password to validate.
     * @return true if the password meets the strength criteria, otherwise false.
     */
    private static boolean meetsPwdStrength(String pwd) {
        if (pwd.length() >= 8) {
            Pattern letter  = Pattern.compile("[a-zA-Z]"); // Valid letter regex
            Pattern digit   = Pattern.compile("[0-9]");    // Valid digit regex
            Pattern special = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]"); // Valid special char regex

            // Match conditions
            Matcher hasLetter      = letter.matcher(pwd);
            Matcher hasDigit       = digit.matcher(pwd);
            Matcher hasSpecialChar = special.matcher(pwd);

            // Return true only if all conditions are met
            return hasDigit.find() && hasSpecialChar.find() && hasLetter.find();
        } else {
            return false;
        }
    }

    /**
     * Sends a request to the server to update the user's password.
     *
     * @param newPassword The new password to set.
     */
    private void updatePassword(String newPassword) {
        String url = URLManager.USERS_URL + "/" + username;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("password", newPassword);  // Set the new password
        } catch (JSONException e) {
            Log.e("PasswordUpdate", "JSON Error", e);
            return;
        }

        StringRequest putRequest = new StringRequest(
                Request.Method.PUT,
                url,
                response -> {
                    Toast.makeText(ChangePasswordActivity.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Log.e("PasswordUpdateError", "Update failed", error);
                    Toast.makeText(ChangePasswordActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public byte[] getBody() {
                return jsonBody.toString().getBytes();  // Set request body
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";  // Set content type
            }
        };

        requestQueue.add(putRequest);  // Add request to the queue
    }
}
