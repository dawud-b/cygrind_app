package com.example.androidexample.Trainers;

import android.content.Intent;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.androidexample.R;
import com.example.androidexample.URLManager;
import com.example.androidexample.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * A Fragment that displays a list of trainers followed by the current user.
 * The fragment fetches the list of followed trainers from a remote server
 * and displays them in a `RecyclerView`. It supports refreshing the list
 * with a swipe gesture using `SwipeRefreshLayout`.
 * <p>
 * This fragment also provides functionality for interacting with trainers,
 * including viewing a trainer's profile and unfollowing a trainer.
 */
public class FollowedTrainersFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;    // SwipeRefreshLayout for pull-to-refresh
    private RecyclerView recyclerView;                // RecyclerView to display the list of followed trainers
    private FollowedTrainersAdapter adapter;          // Adapter to bind data to the RecyclerView
    private List<Trainer> trainerList;                // List of trainers followed by the user

    private static final String TAG = "FollowedTrainersFrag"; // Tag for logging

    /**
     * Required empty public constructor.
     */
    public FollowedTrainersFragment() {
        // Required empty public constructor
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * Initializes the RecyclerView, SwipeRefreshLayout, and adapter.
     * Also sets up the listener for swipe-to-refresh functionality.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_followed_trainers, container, false);

        trainerList = new ArrayList<>();

        swipeRefreshLayout = rootView.findViewById(R.id.friend_search_layout);

        recyclerView = rootView.findViewById(R.id.recyclerViewFollowedTrainers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the adapter and set it to the RecyclerView
        adapter = new FollowedTrainersAdapter(getContext(), trainerList, new FollowedTrainersAdapter.OnTrainerActionListener() {
            @Override
            public void onViewProfileClicked(Trainer trainer) {
                viewTrainerProfile(trainer);
            }

            @Override
            public void onUnfollowClicked(Trainer trainer) {
                unfollowTrainer(trainer);
            }
        });
        recyclerView.setAdapter(adapter);

        fetchFollowedTrainers();

        // Set up SwipeRefreshLayout listener for refreshing the list
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchFollowedTrainers();
            }
        });

        return rootView;
    }

    /**
     * Fetches the list of followed trainers for the current user from the server.
     * Makes a GET request to the server and updates the adapter with the fetched data.
     * Also handles swipe-to-refresh state.
     */
    private void fetchFollowedTrainers() {
        SharedPreferences prefs = getContext().getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");
        if(username.isEmpty()) {
            Log.e(TAG, "username not set correctly");
            return;
        }
        String url = URLManager.getBaseUrl() + "/users/" + username + "/followed-trainers";

        JsonArrayRequest request = new JsonArrayRequest(
                com.android.volley.Request.Method.GET,
                url,
                null, // Pass null as the request body since it's a GET request
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, "Response: " + response.toString());

                        // Clear current list and populate from response
                        trainerList.clear();
                        for(int i = 0; i < response.length(); i++) {
                            try {
                                trainerList.add(new Trainer(response.getJSONObject(i)));
                            } catch (JSONException e) {
                                Log.e(TAG, "Failed parsing trainer JSON: " + e.toString());
                            }
                        }

                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley Error: " + error.toString());
                        Toast.makeText(getContext(), "Connection Error", Toast.LENGTH_LONG).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // Add any required headers here
                return headers;
            }
        };

        // Add the request to the queue
        VolleySingleton.getInstance(getContext()).addToRequestQueue(request);
    }

    /**
     * Removes a trainer from the followed list and notifies the user of the action.
     * Sends a DELETE request to the server to unfollow the trainer.
     *
     * @param trainer The trainer to unfollow.
     */
    private void unfollowTrainer(Trainer trainer) {
        SharedPreferences prefs = getContext().getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");

        String url = URLManager.getBaseUrl() + "/trainers/" + trainer.getId() + "/followers/" + username;

        StringRequest request = new StringRequest(com.android.volley.Request.Method.DELETE, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                        trainerList.remove(trainer);
                        adapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error unfollowing trainer: " + error.getMessage());
                        Toast.makeText(getContext(), "Connection Error", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the Volley request queue
        VolleySingleton.getInstance(getContext()).addToRequestQueue(request);
    }

    /**
     * Opens a new activity to view the profile of the specified trainer.
     *
     * @param trainer The trainer whose profile is to be viewed.
     */
    private void viewTrainerProfile(Trainer trainer) {
        // Intent to view trainer profile
        Intent intent = new Intent(getActivity(), TrainerProfileActivity.class);
        intent.putExtra("TRAINER_ID", trainer.getId());
        startActivity(intent);
    }
}
