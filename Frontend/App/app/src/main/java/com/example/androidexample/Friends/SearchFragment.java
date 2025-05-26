package com.example.androidexample.Friends;

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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment for searching users and sending friend requests.
 * Displays a list of users excluding the current user and allows searching through them.
 */
public class SearchFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView; // RecyclerView for displaying search results
    private SearchAdapter adapter;     // Adapter for user search results
    private List<User> userList;       // List of users excluding the current user
    private User currentUser;          // The logged-in user
    private EditText searchView;       // EditText for inputting search queries

    /**
     * Required public empty constructor.
     */
    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Inflates the layout and sets up views, adapters, listeners.
     *
     * @param inflater           LayoutInflater to inflate the view
     * @param container          Parent view group
     * @param savedInstanceState Previous instance state if exists
     * @return View root view of the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        userList = new ArrayList<>();

        swipeRefreshLayout = rootView.findViewById(R.id.friend_search_layout);
        recyclerView = rootView.findViewById(R.id.recyclerViewSearchResults);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SearchAdapter(userList, new SearchAdapter.OnFriendRequestClickListener() {
            @Override
            public void onFriendRequestClick(User user) {
                sendFriendRequest(user);
            }
        });
        recyclerView.setAdapter(adapter);

        searchView = rootView.findViewById(R.id.searchBar);
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing after text changes
            }
        });

        swipeRefreshLayout.setOnRefreshListener(this::fetchUsers);

        fetchUsers(); // Initial user fetch

        return rootView;
    }

    /**
     * Fetches the list of users from the server and updates the adapter.
     * Filters out the current logged-in user.
     */
    private void fetchUsers() {
        String url = URLManager.USERS_URL;
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    userList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            User user = new User(response.getJSONObject(i));
                            SharedPreferences prefs = getContext().getSharedPreferences("MyPrefs", 0);
                            if (!user.getUsername().equals(prefs.getString("username", ""))) {
                                userList.add(user);
                            } else {
                                currentUser = user;
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("User List Parse Error", e.toString());
                    }

                    adapter.notifyDataSetChanged();
                    adapter.filterList(""); // Reset the filter
                    swipeRefreshLayout.setRefreshing(false);
                },
                error -> {
                    Log.e("Volley Error", error.toString());
                    Toast.makeText(getContext(), "Connection Error", Toast.LENGTH_LONG).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                return new HashMap<>();
            }

            @Override
            protected Map<String, String> getParams() {
                return new HashMap<>();
            }
        };

        VolleySingleton.getInstance(getContext()).addToRequestQueue(request);
    }

    /**
     * Sends a friend request to the selected user.
     *
     * @param user The user to send a friend request to.
     */
    private void sendFriendRequest(User user) {
        Calendar calendar = Calendar.getInstance();
        long timestamp = calendar.getTimeInMillis();

        JSONObject requestJson = new JSONObject();
        JSONObject senderJson = new JSONObject();
        JSONObject receiverJson = new JSONObject();

        try {
            senderJson.put("id", currentUser.getId());
            receiverJson.put("id", user.getId());

            requestJson.put("sender", senderJson);
            requestJson.put("receiver", receiverJson);
            requestJson.put("date", timestamp);
        } catch (JSONException e) {
            Log.e("JSONException", "Error creating request JSON object");
        }

        String url = URLManager.FRIEND_REQUEST_URL;

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show(),
                error -> {
                    Log.e("FriendRequest", "Error: " + error.getMessage());
                    Toast.makeText(getContext(), "Connection Error", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public byte[] getBody() {
                return requestJson.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        VolleySingleton.getInstance(getContext()).addToRequestQueue(request);
    }
}
