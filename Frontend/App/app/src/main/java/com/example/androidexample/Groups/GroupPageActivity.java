package com.example.androidexample.Groups;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidexample.R;
import com.example.androidexample.URLManager;
import com.example.androidexample.VolleySingleton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONObject;

/**
 * Activity that represents the group page where users can view and interact with group-related features.
 * This activity manages the group details, tab navigation, and view pager for different tabs such as
 * "Search", "Members", "Chat", and "Requests". It also handles checking the user's group membership
 * and whether the user is the group leader.
 */
public class GroupPageActivity extends AppCompatActivity {

    public boolean inGroup = false;   // The current user's group status (whether they are in a group)
    public Long groupId;              // The ID of the current user's group
    public boolean isLeader = false;  // Indicates if the current user is the leader of the group
    private TabLayout tabLayout;      // TabLayout to manage the tab navigation
    private ViewPager2 viewPager;     // ViewPager2 to switch between different sections of the group page
    private GroupViewPagerAdapter adapter;  // Adapter for managing the views in the ViewPager2

    /**
     * Called when the activity is created. Initializes the UI components, sets up the ViewPager2
     * with its adapter, and configures the TabLayout for navigation between tabs.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_page);

        // Fetch the details of the group the user is part of
        getGroupDetails();

        // Initialize the TabLayout and ViewPager2
        tabLayout = findViewById(R.id.group_page_tabLayout);
        viewPager = findViewById(R.id.group_page_viewPager);

        // Set up the ViewPager2 with the GroupViewPagerAdapter
        adapter = new GroupViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Connect the TabLayout with the ViewPager2 to manage tab navigation
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Search");
                            break;
                        case 1:
                            tab.setText("Members");
                            break;
                        case 2:
                            tab.setText("Chat");
                            break;
                        case 3:
                            tab.setText("Requests");
                            break;
                    }
                }).attach();
    }

    /**
     * Fetches the group details for the current user from the backend. This includes checking if
     * the user is part of a group, storing the group ID, and checking if the user is the leader of the group.
     * Also prepares data for the WebSocket connection in the chat.
     */
    private void getGroupDetails() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");
        String url = URLManager.getUserGroupURL(username);

        // Create a GET request to fetch group details
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle the JSON response
                        Log.d("VolleyResponse", "current group: " + response.toString());

                        // Parse the response into a WorkoutGroup object
                        WorkoutGroup currentGroup = new WorkoutGroup(response);
                        inGroup = true;              // The user is part of a group
                        groupId = currentGroup.getId();  // Store the group ID

                        // Determine if the current user is the leader of the group
                        if( username.equals(currentGroup.getLeader().getUsername())) {
                            isLeader = true;
                        } else {
                            isLeader = false;
                        }

                        // Prepare data for WebSocket connection and pass it to the adapter
                        Bundle bundle = new Bundle();
                        String session_id = "" + currentGroup.getId() + ":workoutGroup";
                        bundle.putString("session_id", session_id);  // Pass the session ID
                        String url = URLManager.chatURL(session_id, username);
                        bundle.putString("server_url", url);  // Pass the server URL

                        // Set the bundle to the adapter for managing WebSocket
                        adapter.setBundle(bundle);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        inGroup = false;
                        isLeader = false;
                        // Handle errors (e.g., user not in a group, network issues)
                        Log.e("VolleyError", error.toString());
                        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            // Handle specific error case (e.g., user not found or no group)
                        } else {
                            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Add the request to the RequestQueue for processing
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }
}
