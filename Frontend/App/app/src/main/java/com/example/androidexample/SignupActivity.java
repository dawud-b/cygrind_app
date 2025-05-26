package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The SignupActivity class handles the user registration process for both regular users and trainers.
 * It validates user input, shows appropriate error messages, and sends requests to the server to create
 * a new user or trainer account.
 */
public class SignupActivity extends AppCompatActivity {

    private EditText usernameEditText;  // define username edittext variable
    private EditText emailEditText;  // define username edittext variable
    private EditText passwordEditText;  // define password edittext variable
    private EditText confirmEditText;   // define confirm edittext variable
    private Button signupButton;        // define signup button variable
    private Button backButton;          // button for returning to login
    private TextView entryErrorText;   // text entry error messages
    private CheckBox trainerCheckBox;  // checkbox for trainer signup
    private LinearLayout trainerInfoLayout; // layout for trainer-specific fields
    private EditText specializationEditText; // trainer specialization field
    private EditText bioEditText; // trainer bio field
    private static String URL = "http://7052c66e-8818-4f49-ace9-3f1e9f0c35c2.mock.pstmn.io/users";


    /**
     * Called when the activity is created. Initializes UI elements, adds listeners, and handles logic for the signup process.
     * @param savedInstanceState The saved instance state from a previous activity, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize UI elements
        usernameEditText = findViewById(R.id.signup_username_edt);
        emailEditText = findViewById(R.id.signup_email_edt);
        passwordEditText = findViewById(R.id.signup_password_edt);
        confirmEditText = findViewById(R.id.signup_confirm_edt);
        signupButton = findViewById(R.id.signup_signup_btn);
        backButton = findViewById(R.id.signup_back_btn);
        entryErrorText = findViewById(R.id.signup_entry_message);
        trainerCheckBox = findViewById(R.id.signup_trainer_checkbox);
        trainerInfoLayout = findViewById(R.id.trainer_info_layout);
        specializationEditText = findViewById(R.id.signup_specialization_edt);
        bioEditText = findViewById(R.id.signup_bio_edt);

        // Set error message visibility to invisible initially
        entryErrorText.setVisibility(View.INVISIBLE);

        // Initially hide trainer info fields
        trainerInfoLayout.setVisibility(View.GONE);

        // Add text change listeners for validation
        passwordEditText.addTextChangedListener(getTextWatcher());
        confirmEditText.addTextChangedListener(getTextWatcher());
        emailEditText.addTextChangedListener(getTextWatcher());
        usernameEditText.addTextChangedListener(getTextWatcher());

        // Set up the checkbox listener to show/hide trainer info fields
        trainerCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            trainerInfoLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Back button listener to navigate to LoginActivity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Signup button listener to initiate signup process
        signupButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String email = emailEditText.getText().toString();
            String confirm = confirmEditText.getText().toString();
            boolean isTrainerSignup = trainerCheckBox.isChecked();

            // Validate the form data before sending to the server
            if (password.equals(confirm)
                    && meetsPwdStrength(password)
                    && isValidEmail(email)
                    && isValidUsername(username)) {

                if (isTrainerSignup) {
                    // Create trainer account if the user is a trainer
                    String specialization = specializationEditText.getText().toString();
                    String bio = bioEditText.getText().toString();

                    // Validate trainer fields before sending to server
                    if (specialization.isEmpty() || bio.isEmpty()) {
                        Toast.makeText(SignupActivity.this,
                                "Please fill in specialization and bio for trainer signup",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    createTrainer(username, email, password, specialization, bio);
                } else {
                    // Create regular user account
                    createUser(username, email, password);
                }
            } else {
                Toast.makeText(SignupActivity.this, "Invalid Entries", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Validates that the provided username is between 4 and 12 characters long, starts with a letter,
     * and contains only alphanumeric characters or underscores.
     * @param user The username to validate.
     * @return True if the username is valid, false otherwise.
     */
    private boolean isValidUsername(String user) {
        if( user.length() >= 4 && user.length() <= 12 ) {
            return user.matches("^[A-Za-z][A-Za-z0-9_]*$");
        } else {
            return false;
        }
    }

    /**
     * Validates that the provided email matches a valid email format.
     * @param email The email to validate.
     * @return True if the email is valid, false otherwise.
     */
    private boolean isValidEmail(String email) {
        Pattern regex = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

        return regex.matcher(email).find();
    }


    /**
     * Validates that the provided password meets the following requirements:
     * 1. At least 8 characters long.
     * 2. Contains at least one letter.
     * 3. Contains at least one digit.
     * 4. Contains at least one special character.
     * @param pwd The password to validate.
     * @return True if the password meets the requirements, false otherwise.
     */
    private static boolean meetsPwdStrength(String pwd) {
        if( pwd.length() >= 8 ) {
            Pattern letter  = Pattern.compile("[a-zA-Z]"); // valid letter regex
            Pattern digit   = Pattern.compile("[0-9]");    // valid digit regex
            Pattern special = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]"); // valid special char regex

            // set match conditions
            Matcher hasLetter      = letter.matcher(pwd);
            Matcher hasDigit       = digit.matcher(pwd);
            Matcher hasSpecialChar = special.matcher(pwd);

            // return true only if all condition are met
            if( hasDigit.find() && hasSpecialChar.find() && hasLetter.find()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    /**
     * Returns a TextWatcher object used to validate fields as the user types.
     * It checks the validity of the email, username, password strength, and password match.
     * @return A TextWatcher to be attached to the input fields.
     */
    private TextWatcher getTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // string used to build error message
                String msg = "";

                // make error text invisible
                entryErrorText.setVisibility(View.INVISIBLE);

                if ( !emailEditText.getText().toString().equals("")) {
                    if( !isValidEmail(emailEditText.getText().toString())) {
                        msg += "Email is invalid.\n";
                        entryErrorText.setVisibility(View.VISIBLE);
                    }
                }

                if ( !usernameEditText.getText().toString().equals("")) {
                    if( !isValidUsername(usernameEditText.getText().toString())) {
                        msg += "Username is invalid.\n";
                        entryErrorText.setVisibility(View.VISIBLE);
                    }
                }

                // password strength checked only when password entry is not empty
                if ( !passwordEditText.getText().toString().equals("") ) {
                    if( !meetsPwdStrength(passwordEditText.getText().toString())) {
                        msg += "Password Strength not met.\n";
                        entryErrorText.setVisibility(View.VISIBLE);
                    }
                }

                // if both password entries are filled, check that they match
                if (( !confirmEditText.getText().toString().equals("")) ||
                        ( !passwordEditText.getText().toString().equals(""))) {

                    if( !confirmEditText.getText().toString().equals(passwordEditText.getText().toString())) {
                        msg += "Password entries do not match.";
                        entryErrorText.setVisibility(View.VISIBLE);
                    }
                }

                entryErrorText.setText(msg);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    /**
     * Sends a POST request to the server to create a new user account.
     * @param username The username for the new user.
     * @param email The email for the new user.
     * @param password The password for the new user.
     */
    private void createUser(String username, String email, String password) {
        // POST endpoint
        String url = "http://coms-3090-035.class.las.iastate.edu:8080/users";

        // Creates user JSON object
        JSONObject userJson = new JSONObject();
        try {
            userJson.put("username", username);
            userJson.put("email", email);
            userJson.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // create JSON object POST request and adds to Volley request queue
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // backend response when user is already created
                        Toast.makeText(SignupActivity.this, response, Toast.LENGTH_SHORT).show();
                        if (!response.contains("Failed")) {
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            intent.putExtra("username", username);
                            intent.putExtra("password", password);
                            startActivity(intent);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("CreateUser", "Error: " + error.getMessage());
                        Toast.makeText(SignupActivity.this, "Request failed.", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public byte[] getBody() {
                // convert JSON user to string
                return userJson.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                // Set the content type as application/json
                return "application/json";
            }
        };

        // Add the request to the request queue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }


    /**
     * Sends a POST request to the server to create a new trainer account.
     * @param username The username for the new trainer.
     * @param email The email for the new trainer.
     * @param password The password for the new trainer.
     * @param specialization The specialization of the trainer.
     * @param bio The bio of the trainer.
     */
    private void createTrainer(String username, String email, String password,
                               String specialization, String bio) {
        // POST endpoint for trainer registration
        String url = "http://coms-3090-035.class.las.iastate.edu:8080/register/trainer";

        // Creates trainer registration JSON object
        JSONObject trainerJson = new JSONObject();
        try {
            // User fields
            trainerJson.put("username", username);
            trainerJson.put("email", email);
            trainerJson.put("password", password);

            // We don't have firstName and lastName in the current form,
            // but they're required by the API, so add empty or default values
            trainerJson.put("firstName", "");
            trainerJson.put("lastName", "");
            trainerJson.put("telephone", "");

            // Trainer fields
            trainerJson.put("specialization", specialization);
            trainerJson.put("bio", bio);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create JSON object POST request and add to Volley request queue
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(SignupActivity.this, "Trainer account created!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                        intent.putExtra("username", username);
                        intent.putExtra("password", password);
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("CreateTrainer", "Error: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"));
                        Toast.makeText(SignupActivity.this, "Failed to create trainer account.", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public byte[] getBody() {
                // Convert JSON trainer to string
                return trainerJson.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                // Set the content type as application/json
                return "application/json";
            }
        };

        // Add the request to the request queue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
}