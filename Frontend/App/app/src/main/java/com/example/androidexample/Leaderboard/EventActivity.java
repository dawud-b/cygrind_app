package com.example.androidexample.Leaderboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidexample.R;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
/**
 * Activity that handles the event page where users can view event details, submit scores, and view the leaderboard.
 * This activity connects to a WebSocket server to handle real-time communication for event updates.
 */
public class EventActivity extends AppCompatActivity {

    private static final String TAG = "EventActivity";  // Tag for logging
    private static final String WEBSOCKET_URL = "ws://coms-3090-035.class.las.iastate.edu:8080";  // WebSocket server URL

    // UI components
    private Button backButton;  // Button to navigate back to the previous screen
    private Button submitScoreButton;  // Button to submit the user's score
    private EditText scoreInput;  // EditText for entering the score
    private TextView eventTitle;  // TextView to display the event title
    private TextView eventDetails;  // TextView to display the event details
    private TextView timeRemaining;  // TextView to display the remaining time
    private TextView currentWeightClass;  // TextView to display the current weight class
    private RecyclerView leaderboardRecyclerView;  // RecyclerView to display the leaderboard

    // Data
    private long eventId;  // ID of the event
    private String username;  // Username of the current user
    private WebSocketClient webSocketClient;  // WebSocket client for real-time communication
    private List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();  // List of leaderboard entries
    private LeaderboardAdapter leaderboardAdapter;  // Adapter for the leaderboard RecyclerView

    /**
     * Called when the activity is first created.
     * Initializes UI components, retrieves data from the intent, sets up WebSocket connection,
     * and sets click listeners for the buttons.
     *
     * @param savedInstanceState A bundle containing the activity's previously saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        // Initialize UI components
        backButton = findViewById(R.id.back_button);
        submitScoreButton = findViewById(R.id.submit_score_button);
        scoreInput = findViewById(R.id.score_input);
        eventTitle = findViewById(R.id.event_title);
        eventDetails = findViewById(R.id.event_details);
        timeRemaining = findViewById(R.id.time_remaining);
        currentWeightClass = findViewById(R.id.current_weight_class);
        leaderboardRecyclerView = findViewById(R.id.leaderboard_recycler_view);

        // Set up RecyclerView for leaderboard
        leaderboardAdapter = new LeaderboardAdapter(this, leaderboardEntries);
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        leaderboardRecyclerView.setAdapter(leaderboardAdapter);

        // Retrieve event data from the intent
        eventId = getIntent().getLongExtra("EVENT_ID", 1L);
        String title = getIntent().getStringExtra("EVENT_TITLE");
        username = getIntent().getStringExtra("USERNAME");

        // Set event title
        if (title != null && !title.isEmpty()) {
            eventTitle.setText(title);
        } else {
            eventTitle.setText("Event #" + eventId);
        }

        // Example event details
        eventDetails.setText("Exercise Type: Bench Press");

        // Set back button click listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();  // Close the current activity and return to the previous one
            }
        });

        // Set submit score button click listener
        submitScoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitScore();  // Submit the score when the button is clicked
            }
        });

        // Connect to WebSocket server
        connectToWebSocket();
    }

    /**
     * Establishes a WebSocket connection to the event server.
     * This method creates a WebSocket client, handles connection events, and listens for messages.
     */
    private void connectToWebSocket() {
        try {
            URI serverUri = new URI(WEBSOCKET_URL + "/events/" + username);
            webSocketClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d(TAG, "WebSocket connection opened");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EventActivity.this, "Connected to event server", Toast.LENGTH_SHORT).show();
                            // Request leaderboard data
                            requestLeaderboard();
                        }
                    });
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "WebSocket message received: " + message);

                    // Handle different types of messages
                    if (message.contains("Leaderboard for")) {
                        handleLeaderboardData(message);  // Parse and update leaderboard data
                    } else if (message.contains("Score submitted successfully")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(EventActivity.this, "Score submitted successfully!", Toast.LENGTH_SHORT).show();
                                // Refresh leaderboard
                                requestLeaderboard();
                            }
                        });
                    } else if (message.contains("Error:")) {
                        final String errorMsg = message;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(EventActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "WebSocket connection closed: " + reason);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EventActivity.this, "Disconnected from event server", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket error", ex);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EventActivity.this, "Error connecting to server: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            };

            webSocketClient.connect();
        } catch (URISyntaxException e) {
            Log.e(TAG, "Invalid WebSocket URI", e);
            Toast.makeText(this, "Error connecting to server", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Requests the leaderboard data from the server via WebSocket.
     * This sends a message to the server to fetch the leaderboard for the current event.
     */
    private void requestLeaderboard() {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            String command = "/leaderboard " + eventId;
            webSocketClient.send(command);
            Log.d(TAG, "Requested leaderboard data: " + command);
        }
    }

    /**
     * Handles the leaderboard data received from the server.
     * Parses the leaderboard message and updates the UI with the leaderboard entries.
     *
     * @param message The message containing the leaderboard data.
     */
    private void handleLeaderboardData(String message) {
        // Parse leaderboard data from message
        final List<LeaderboardEntry> entries = new ArrayList<>();
        String currentWeightClassName = "Default";

        // Simple parsing logic - extract entries from the message
        String[] lines = message.split("\n");
        for (String line : lines) {
            if (line.contains("Weight Class:")) {
                currentWeightClassName = line.replace("Weight Class:", "").trim();
                continue;
            }

            if (line.matches("\\s*\\d+\\..*")) {
                try {
                    // Extract and parse leaderboard entry data
                    String[] parts = line.trim().split("-");
                    if (parts.length >= 2) {
                        String nameWithRank = parts[0].trim();
                        String scoreText = parts[1].trim().replace("pts", "").trim();

                        // Extract rank, name, username, and score
                        int dotIndex = nameWithRank.indexOf(".");
                        String name = nameWithRank.substring(dotIndex + 1).trim();

                        String username = name;
                        if (name.contains("(") && name.contains(")")) {
                            username = name.substring(name.indexOf("(") + 1, name.indexOf(")")).trim();
                            name = name.substring(0, name.indexOf("(")).trim();
                        }

                        int score = Integer.parseInt(scoreText);
                        String[] nameParts = name.split(" ", 2);
                        String firstName = nameParts[0];
                        String lastName = nameParts.length > 1 ? nameParts[1] : "";

                        entries.add(new LeaderboardEntry(username, firstName, lastName, currentWeightClassName, score));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing leaderboard entry: " + line, e);
                }
            }
        }

        // Update UI on main thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!entries.isEmpty() && entries.get(0).getWeightClass() != null) {
                    currentWeightClass.setText(entries.get(0).getWeightClass() + " Weight Class");
                }
                leaderboardEntries.clear();
                leaderboardEntries.addAll(entries);
                leaderboardAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Submits the score entered by the user to the server.
     * This method sends the score via WebSocket to be processed by the server.
     */
    private void submitScore() {
        String scoreText = scoreInput.getText().toString().trim();

        if (scoreText.isEmpty()) {
            Toast.makeText(this, "Please enter a score", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int score = Integer.parseInt(scoreText);

            if (score <= 0) {
                Toast.makeText(this, "Score must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            if (webSocketClient != null && webSocketClient.isOpen()) {
                String command = "/score " + eventId + " " + score;
                webSocketClient.send(command);
                Log.d(TAG, "Score submission command: " + command);

                scoreInput.setText("");
                Toast.makeText(this, "Submitting score...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Not connected to server", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when the activity is destroyed.
     * Closes the WebSocket connection when the activity is no longer needed.
     */
    @Override
    protected void onDestroy() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
        super.onDestroy();
    }
}
