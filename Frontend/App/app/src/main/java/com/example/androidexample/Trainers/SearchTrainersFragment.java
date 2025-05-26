package com.example.androidexample.Trainers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import java.util.List;
/**
 * A Fragment that displays a list of trainers that can be searched and filtered.
 * It includes a search bar for filtering trainers by name or specialization,
 * a SwipeRefreshLayout for refreshing the list, and a RecyclerView to display the results.
 * <p>
 * Users can interact with the trainers by following them or viewing their profiles.
 */
public class SearchTrainersFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;  // Layout for pull-to-refresh functionality
    private RecyclerView recyclerView;              // RecyclerView to display the list of trainers
    private SearchTrainersAdapter adapter;          // Adapter to bind trainers to the RecyclerView
    private List<Trainer> trainerList;              // List to store the trainer objects
    private EditText searchBar;                     // EditText for filtering the trainers based on user input

    private static final String TAG = "SearchTrainersFrag"; // Tag for logging

    /**
     * Default constructor for the fragment. It is required for fragments in Android.
     */
    public SearchTrainersFragment() {
        // Required empty public constructor
    }

    /**
     * Called when the fragment's view is created. Initializes the views, sets up the adapter,
     * and configures listeners for the search bar and swipe-to-refresh layout.
     *
     * @param inflater The LayoutInflater object used to inflate the fragment's layout.
     * @param container The parent container that holds the fragment.
     * @param savedInstanceState Any saved state for restoring the fragment.
     * @return The root view for the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_trainers, container, false);

        // Initialize views
        trainerList = new ArrayList<>();
        swipeRefreshLayout = rootView.findViewById(R.id.friend_search_layout);
        searchBar = rootView.findViewById(R.id.searchBar);
        recyclerView = rootView.findViewById(R.id.recyclerViewTrainerResults);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the adapter with the list of trainers and set it to the RecyclerView
        adapter = new SearchTrainersAdapter(getContext(), trainerList, new SearchTrainersAdapter.OnTrainerActionListener() {
            @Override
            public void onFollowClick(Trainer trainer) {
                followTrainer(trainer);
            }

            @Override
            public void onViewProfileClick(Trainer trainer) {
                viewTrainerProfile(trainer);
            }
        });
        recyclerView.setAdapter(adapter);

        // Add a text change listener to the search bar for filtering the trainer list
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter the list based on the search query entered by the user
                adapter.filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });

        // Fetch the list of all trainers from the server
        fetchAllTrainers();

        // Set up SwipeRefreshLayout listener for refreshing the list
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchAllTrainers();
            }
        });

        return rootView;
    }

    /**
     * Fetches all trainers from the server and updates the trainer list.
     * It is called initially and also on refresh.
     */
    private void fetchAllTrainers() {
        String url = URLManager.getBaseUrl() + "/trainers";

        // Create a GET request to fetch the list of trainers
        JsonArrayRequest request = new JsonArrayRequest(
                com.android.volley.Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, "Response: " + response.toString());

                        // Clear the existing list and add trainers from the response
                        trainerList.clear();
                        for(int i = 0; i < response.length(); i++) {
                            try {
                                trainerList.add(new Trainer(response.getJSONObject(i)));
                            } catch (JSONException e) {
                                Log.e(TAG, "Failed parsing trainer JSON: " + e.toString());
                            }
                        }

                        // Notify the adapter of the data change
                        adapter.notifyDataSetChanged();
                        // Filter the list based on the current search query
                        adapter.filterList(searchBar.getText().toString());
                        // Hide the refresh indicator
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley Error: " + error.toString());
                        // Show an error message if the request fails
                        Toast.makeText(getContext(), "Connection Error", Toast.LENGTH_LONG).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

        // Add the request to the Volley request queue
        VolleySingleton.getInstance(getContext()).addToRequestQueue(request);
    }

    /**
     * Sends a request to follow a trainer.
     *
     * @param trainer The trainer to be followed.
     */
    private void followTrainer(Trainer trainer) {
        SharedPreferences prefs = getContext().getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");

        String url = URLManager.getBaseUrl() + "/trainers/" + trainer.getId() + "/followers/" + username;

        // Create a POST request to follow the trainer
        StringRequest request = new StringRequest(com.android.volley.Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error following trainer: " + error.getMessage());
                        // Show an error message if the request fails
                        Toast.makeText(getContext(), "Connection Error", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the Volley request queue
        VolleySingleton.getInstance(getContext()).addToRequestQueue(request);
    }

    /**
     * Starts an activity to view a trainer's profile.
     *
     * @param trainer The trainer whose profile is to be viewed.
     */
    private void viewTrainerProfile(Trainer trainer) {
        // Create an intent to view the trainer's profile
        Intent intent = new Intent(getActivity(), TrainerProfileActivity.class);
        intent.putExtra("TRAINER_ID", trainer.getId());
        startActivity(intent);
    }
}
