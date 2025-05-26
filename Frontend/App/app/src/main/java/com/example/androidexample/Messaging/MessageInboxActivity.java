package com.example.androidexample.Messaging;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.androidexample.HomeActivity;
import com.example.androidexample.R;
import com.example.androidexample.URLManager;
import com.example.androidexample.VolleySingleton;
import com.example.androidexample.WebSocketService;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
/**
 * The MessageInboxActivity class represents an activity that displays a list of chat sessions (message threads) in a
 * messaging application. It allows users to view their message threads, select a thread to enter a chat, and navigate
 * back to the home screen. This activity communicates with the backend to fetch the list of chat sessions associated
 * with the user and presents them in a RecyclerView.
 * <p>
 * It provides two main functionalities:
 * 1. Viewing the list of message threads (chat sessions).
 * 2. Navigating to the specific chat activity when a message thread is selected.
 */
public class MessageInboxActivity extends AppCompatActivity {

    private RecyclerView recyclerView;  // RecyclerView to display the list of chat sessions
    private ChatSessionAdapter chatSessionAdapter;  // Adapter for the RecyclerView
    private List<ChatSession> chatSessionList;  // List of chat sessions to be displayed
    private Button backBtn;  // Button to navigate back to the home screen
    private Button newBtn;  // Button to start a new chat (currently not used)

    /**
     * Called when the activity is created. This method initializes the UI elements, sets up the RecyclerView,
     * fetches the chat sessions, and handles user interactions such as selecting a chat session or navigating back.
     *
     * @param savedInstanceState The saved instance state for the activity, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_threads);

        recyclerView = findViewById(R.id.recyclerViewThreads);  // Find the RecyclerView
        backBtn = findViewById(R.id.buttonBack);  // Find the back button

        recyclerView.setLayoutManager(new LinearLayoutManager(this));  // Set the layout manager for the RecyclerView

        // Initialize the list of chat sessions
        chatSessionList = new ArrayList<>();
        getChatSessions();  // Fetch the chat sessions from the backend

        // Set the adapter to the RecyclerView
        chatSessionAdapter = new ChatSessionAdapter(chatSessionList, new ChatSessionAdapter.OnMessageThreadClickListener() {
            /**
             * Called when a message thread is clicked. It starts a new activity to display the chat for the selected thread.
             *
             * @param thread The chat session (message thread) that was clicked.
             */
            @Override
            public void onMessageThreadClick(ChatSession thread) {
                SharedPreferences prefs = getSharedPreferences("MyPrefs", 0);
                String username = prefs.getString("username", "");
                String serverUrl = URLManager.chatURL(thread.getId(), username);  // Generate the server URL for the chat session
                Log.d("MessageInboxActivity", "URL: " + serverUrl);

                // Intent to navigate to the ChatActivity
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("session_id", thread.getId());  // Pass the chat session ID
                intent.putExtra("group_name", thread.getGroupName());  // Pass the group name
                intent.putExtra("server_url", serverUrl);  // Pass the server URL
                startActivity(intent);  // Start the chat activity
            }
        });

        recyclerView.setAdapter(chatSessionAdapter);  // Set the adapter to the RecyclerView

        // Set up the back button to navigate back to the home screen
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MessageInboxActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Fetches the list of chat sessions from the backend using an HTTP request. The response is parsed and added to
     * the chatSessionList, and the adapter is notified to update the RecyclerView.
     */
    private void getChatSessions() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");
        String url = URLManager.getUserChatSessionsURL(username);  // Get the URL to fetch chat sessions for the user

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Handle the JSON array response
                        // Log the response for debugging purposes
                        System.out.println("Response: " + response.toString());
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                // Parse each chat session from the response and add to the list
                                chatSessionList.add(new ChatSession((JSONObject) response.get(i), username));
                            } catch (JSONException e) {
                                Log.e("MessageInboxActivity JSONException", "Error parsing ChatSessions JSONArray");
                            }
                        }
                        Log.d("MessageInboxActivity", "MessageThreadList size: " + chatSessionList.size());
                        chatSessionAdapter.notifyDataSetChanged();  // Notify the adapter that data has changed
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                        System.out.println("Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add the request to the Volley request queue for execution
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);
    }
}
