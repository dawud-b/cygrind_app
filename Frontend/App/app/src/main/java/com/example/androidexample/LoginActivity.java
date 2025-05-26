package com.example.androidexample;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.security.NetworkSecurityPolicy;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * The LoginActivity handles the user authentication process by allowing users to input their
 * credentials and either login or navigate to the signup screen.
 * It uses Volley to send network requests and handles login validation and success.
 */
public class LoginActivity extends AppCompatActivity {

    // UI elements
    private EditText usernameEditText;  // Username input field
    private EditText passwordEditText;  // Password input field
    private Button loginButton;         // Login button
    private Button signupButton;        // Sign up button

    /**
     * Initializes the LoginActivity UI and sets up click listeners for the login and signup buttons.
     * Also initializes user input fields with data passed from previous activities if available.
     *
     * @param savedInstanceState the saved instance state of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        usernameEditText = findViewById(R.id.login_username_edt);
        passwordEditText = findViewById(R.id.login_password_edt);
        loginButton = findViewById(R.id.login_login_btn);
        signupButton = findViewById(R.id.login_signup_btn);

        // Extract data passed into this activity (if any)
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            usernameEditText.setText(extras.getString("username"));
            passwordEditText.setText(extras.getString("password"));
        }

        // Login button click listener
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                if( username.equals("test") ) {
                    // Test login for demo purposes
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    // Send login credentials for validation
                    String password = passwordEditText.getText().toString();
                    sendPutRequest(username, password);
                }
            }
        });

        // Signup button click listener
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to signup screen
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Sends a PUT request with the provided username and password to the backend server to authenticate the user.
     * Upon a successful response, navigates to the HomeActivity.
     *
     * @param username the username entered by the user
     * @param password the password entered by the user
     */
    private void sendPutRequest(final String username, final String password) {
        // Backend URL for login PUT request
        String url = URLManager.getLoginPutReqURL(username);

        // Create PUT request with username and password
        StringRequest request = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("LoginResponse", response);
                        if( response.equals("success") ) {
                            // On successful login, navigate to HomeActivity
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
                        }
                        // Show response in toast
                        Toast.makeText(LoginActivity.this, "Response: " + response, Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("LoginResponseError", "Error: " + error.getMessage());
                        if (error.networkResponse != null) {
                            Log.e("Status", "Status Code: " + error.networkResponse.statusCode);
                            Log.e("Body", "Response Body: " + new String(error.networkResponse.data));
                        }
                        // Show error in toast
                        Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public byte[] getBody() {
                // Convert password string to byte array
                return password.getBytes();
            }

            @Override
            public String getBodyContentType() {
                // Set content type as plain text
                return "text/plain";
            }
        };

        // Add request to Volley request queue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    /**
     * Sends a POST request to validate the login credentials using a mock server.
     * On successful login, navigates to the HomeActivity, else displays a toast with an error message.
     *
     * @param username the username entered by the user
     * @param password the password entered by the user
     */
    private void sendLoginRequest(String username, String password) {
        // Mock POST endpoint for login
        String url = "https://7052c66e-8818-4f49-ace9-3f1e9f0c35c2.mock.pstmn.io/login";

        // Create a JSON object with login credentials
        JSONObject loginJson = new JSONObject();
        try {
            loginJson.put("username", username);
            loginJson.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create a JSON object POST request and add it to the Volley queue
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, loginJson,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String message = response.getString("message");
                            if( message.equals("Login Successful")) {
                                // On successful login, navigate to HomeActivity
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                intent.putExtra("username", username);
                                startActivity(intent);
                            } else {
                                // Show error message if login fails
                                Toast.makeText(LoginActivity.this, "Username or password incorrect", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("CreateUser", "Error: " + error.getMessage());
                        // Show a failure message on error
                        Toast.makeText(LoginActivity.this, "Request failed.", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add request to Volley request queue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
}