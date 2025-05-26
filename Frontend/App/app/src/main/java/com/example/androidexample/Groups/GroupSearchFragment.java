package com.example.androidexample.Groups;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.androidexample.R;
import com.example.androidexample.URLManager;
import com.example.androidexample.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment that allows users to search for and join workout groups, or create new ones.
 * This fragment displays a list of available workout groups and provides functionalities
 * to send join requests or create a new group.
 */
public class GroupSearchFragment extends Fragment {

    private RecyclerView recyclerView;  // RecyclerView to display the list of workout groups
    private WorkoutGroupAdapter adapter;  // Adapter to bind data to the RecyclerView
    private List<WorkoutGroup> groupList;  // List of available workout groups
    private Button createBtn;  // Button to initiate the group creation process
    private View rootView;  // The root view of the fragment
    private String username;

    /**
     * Default constructor for the fragment.
     */
    public GroupSearchFragment() {}

    /**
     * Called when the fragment's view is created. This method initializes the view components,
     * sets up the RecyclerView, and handles user interactions such as creating a new group or
     * sending a join request.
     *
     * @param inflater The LayoutInflater object used to inflate the view
     * @param container The parent ViewGroup that the fragment's UI will be attached to
     * @param savedInstanceState A bundle containing any saved instance state of the fragment
     * @return The inflated view for the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_group_search, container, false);

        SharedPreferences prefs = rootView.getContext().getSharedPreferences("MyPrefs", 0);
        username = prefs.getString("username", "");

        // Initialize group list
        groupList = new ArrayList<>();

        // Initialize UI components
        createBtn = rootView.findViewById(R.id.group_search_create_button);
        recyclerView = rootView.findViewById(R.id.group_search_view);

        // Set the layout manager for the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));

        // Initialize the adapter for RecyclerView
        adapter = new WorkoutGroupAdapter(groupList, new WorkoutGroupAdapter.OnJoinRequestClickListener() {
            @Override
            public void onJoinRequestClick(WorkoutGroup group) {
                sendJoinRequest(group);
            }
        });

        // Set the OnClickListener for the "Create Group" button
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGroup(rootView.getContext());
            }
        });

        // Set the adapter for the RecyclerView
        recyclerView.setAdapter(adapter);

        checkSubscriptionStatus();

        // Fetch workout groups from the server
        fetchWorkoutGroups();

        return rootView;
    }

    /**
     * Sends a join request for a selected workout group by sending a POST request to the server.
     *
     * @param group The workout group to join
     */
    private void sendJoinRequest(WorkoutGroup group) {

        String url = URLManager.getGroupJoinURL(group.getId(), username);

        // Create a POST request to send the join request
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the response from the server
                        if (response.equals("User is already in a group")) {
                            Toast.makeText(rootView.getContext(), "You are already a member of a group.", Toast.LENGTH_SHORT).show();
                        } else if (response.equals("Join request submitted")) {
                            Toast.makeText(rootView.getContext(), "Join request successfully submitted.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error responses such as network issues
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                String errorMessage = new String(error.networkResponse.data);
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                // Add the necessary parameters to the POST request
                Map<String, String> params = new HashMap<>();
                params.put("username", username);  // Add username to the request parameters
                return params;
            }
        };

        // Add the request to the Volley request queue
        VolleySingleton.getInstance(rootView.getContext()).addToRequestQueue(request);
    }

    /**
     * Fetches the list of workout groups available for the user to join.
     * This method sends a GET request to the server and updates the RecyclerView with the
     * retrieved data.
     */
    private void fetchWorkoutGroups() {
        String url = URLManager.WORKOUT_GROUP_URL;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        groupList.clear();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject groupJson = response.getJSONObject(i);
                                WorkoutGroup group = new WorkoutGroup(groupJson);
                                groupList.add(group);
                            }
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("JSONException", e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error responses
                Log.e("VolleyError", error.toString());
            }
        });

        // Add the request to the Volley request queue
        VolleySingleton.getInstance(rootView.getContext()).addToRequestQueue(request);
    }

    /**
     * Displays a dialog that allows the user to create a new workout group. The dialog includes
     * input fields for group name, description, and type.
     *
     * @param context The context in which the dialog will be shown
     */
    private void createGroup(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Create New Group");

        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_create_group, null);
        builder.setView(dialogView);

        // Find input fields in the dialog layout
        final EditText groupNameInput = dialogView.findViewById(R.id.groupNameInput);
        final EditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);
        final EditText groupTypeInput = dialogView.findViewById(R.id.groupTypeInput);

        // Set up the positive button to handle group creation
        builder.setPositiveButton("Create", (dialog, which) -> {
            String groupName = groupNameInput.getText().toString();
            String description = descriptionInput.getText().toString();
            String groupType = groupTypeInput.getText().toString();

            // Validate the input fields
            if (groupName.isEmpty() || description.isEmpty() || groupType.isEmpty()) {
                Toast.makeText(context, "All fields are required!", Toast.LENGTH_SHORT).show();
            } else {
                // If valid, call createGroup method to create the group
                createGroup(groupName, description, groupType);
            }
        });

        // Set up the negative button (Cancel)
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Show the dialog
        builder.create().show();
    }

    private void checkSubscriptionStatus() {
        String url = "http://coms-3090-035.class.las.iastate.edu:8080/users/" + username;
        Log.d("GroupSearch", "Request URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d("PaymentActivity", "SubStatusResponse:" + response);
                        boolean isActive = response.getBoolean("subscribed");

                        if (!isActive) {
                            createBtn.setEnabled(false); // Disable the button
                            createBtn.setAlpha(0.5f);    // Make the button look disabled
                            Toast.makeText(getActivity(), "Subscription required to create a group.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("PaymentActivity", "SubStatusResponse:" + response);
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        String body = new String(error.networkResponse.data);
                        Log.e("SubscriptionCheck", "Raw response: " + body);
                    }
                    Log.e("SubscriptionCheck", "Volley error: " + error.toString());
                }
        );

        VolleySingleton.getInstance(getActivity()).addToRequestQueue(request);
    }

    /**
     * Creates a new workout group by sending the group details to the server via a POST request.
     *
     * @param groupName The name of the new group
     * @param description A brief description of the group
     * @param groupType The type/category of the group
     */
    private void createGroup(String groupName, String description, String groupType) {
        // Create a JSON object with the group details
        JSONObject groupDetails = new JSONObject();
        try {
            groupDetails.put("groupName", groupName);
            groupDetails.put("description", description);
            groupDetails.put("groupType", groupType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SharedPreferences prefs = rootView.getContext().getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");

        String url = URLManager.WORKOUT_GROUP_URL + "/create" + "?username=" + username;

        // Create a JsonObjectRequest to send the group creation details
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, groupDetails,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful group creation
                        WorkoutGroup group = new WorkoutGroup(response);
                        groupList.add(group);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(), "Group Created", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error responses
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                String errorMessage = new String(error.networkResponse.data);
                                Log.e("Volley Error", "Error message: " + errorMessage);
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }) {

            @Override
            public Map<String, String> getParams() {
                // Add parameters to the request
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("groupDetails", groupDetails.toString());
                return params;
            }
        };

        // Add the request to the Volley request queue
        VolleySingleton.getInstance(getActivity()).addToRequestQueue(request);
    }
}
