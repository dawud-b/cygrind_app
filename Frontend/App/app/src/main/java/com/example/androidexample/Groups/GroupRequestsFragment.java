package com.example.androidexample.Groups;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.androidexample.R;
import com.example.androidexample.URLManager;
import com.example.androidexample.User;
import com.example.androidexample.VolleySingleton;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment that displays a list of group join requests. It allows the group leader to accept or decline requests.
 * The fragment interacts with the `GroupPageActivity` to fetch and manage join requests for a workout group.
 */
public class GroupRequestsFragment extends Fragment {

    private RecyclerView recyclerView;  // RecyclerView to display the list of join requests
    private GroupRequestAdapter adapter;  // Adapter to bind the data to the RecyclerView
    private List<JoinRequest> requestList;  // List of join requests to be displayed
    private View rootView;  // The root view of the fragment
    private GroupPageActivity activity;  // Reference to the root activity

    /**
     * Default constructor for the fragment.
     */
    public GroupRequestsFragment() {
        // Required empty public constructor
    }

    /**
     * Called when the fragment is created and its layout is to be inflated.
     *
     * @param inflater The LayoutInflater object used to inflate the view
     * @param container The parent ViewGroup that the fragment's UI will be attached to
     * @param savedInstanceState A bundle containing the saved state of the fragment (if any)
     * @return The inflated view for the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        rootView = inflater.inflate(R.layout.fragment_requests, container, false);

        // Initialize the list of join requests
        requestList = new ArrayList<>();

        // Initialize RecyclerView and set its LayoutManager
        recyclerView = rootView.findViewById(R.id.recyclerViewRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get reference to the parent activity (GroupPageActivity)
        activity = (GroupPageActivity) getActivity();
        if (activity == null) {
            Log.e("GroupRequestsFragment", "activity is null");
        }

        // Ensure user is the leader of the group before fetching requests
        if (activity.inGroup && activity.isLeader) {
            fetchRequests();
        } else {
            Toast.makeText(activity, "Must be leader to view requests.", Toast.LENGTH_SHORT).show();
        }

        // Set the adapter for the RecyclerView with an implementation of OnRequestClickListener
        adapter = new GroupRequestAdapter(requestList, new GroupRequestAdapter.OnRequestClickListener() {
            @Override
            public void onDeclineClick(JoinRequest request) {
                declineRequest(request);
            }

            @Override
            public void onAcceptClick(JoinRequest request) {
                acceptRequest(request);
            }
        });
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    /**
     * Declines a join request by sending a DELETE request to the server.
     *
     * @param request The join request to be declined
     */
    private void declineRequest(JoinRequest request) {
        SharedPreferences prefs = activity.getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");
        String url = URLManager.declineGroupJoinRequestsURL(activity.groupId, request.getId(), username);

        // Create a DELETE request to decline the join request
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Update the request's status to REJECTED and notify the adapter
                        request.setStatus(JoinRequest.RequestStatus.REJECTED);
                        adapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle any error responses
                        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            Toast.makeText(activity, "Group not found", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Add the request to the Volley request queue
        VolleySingleton.getInstance(activity).addToRequestQueue(stringRequest);
    }

    /**
     * Accepts a join request by sending a POST request to the server.
     *
     * @param request The join request to be accepted
     */
    private void acceptRequest(JoinRequest request) {
        SharedPreferences prefs = activity.getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");
        String url = URLManager.acceptGroupJoinRequestsURL(activity.groupId, request.getId(), username);

        // Create a POST request to accept the join request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Update the request's status to ACCEPTED and notify the adapter
                        request.setStatus(JoinRequest.RequestStatus.ACCEPTED);
                        adapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle any error responses
                        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            Toast.makeText(activity, "Group not found", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Add the request to the Volley request queue
        VolleySingleton.getInstance(activity).addToRequestQueue(stringRequest);
    }

    /**
     * Fetches the list of join requests for the group from the server.
     * This method is only called if the current user is the group leader.
     */
    private void fetchRequests() {
        SharedPreferences prefs = activity.getSharedPreferences("MyPrefs", 0);
        String url = URLManager.getGroupJoinRequestsURL(activity.groupId, prefs.getString("username", ""));

        // Create a GET request to fetch the join requests
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null, // No body for GET request
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Process the list of join requests from the response
                            for (int i = 0; i < response.length(); i++) {
                                JoinRequest joinRequest = new JoinRequest(response.getJSONObject(i));
                                requestList.add(joinRequest);
                            }

                            // Notify the adapter that the data has changed
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error responses
                        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            Toast.makeText(activity, "Group not found", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Add the request to the Volley request queue
        VolleySingleton.getInstance(activity).addToRequestQueue(request);
    }
}
