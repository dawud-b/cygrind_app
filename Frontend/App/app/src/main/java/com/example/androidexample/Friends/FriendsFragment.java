package com.example.androidexample.Friends;

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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.androidexample.Messaging.ChatActivity;
import com.example.androidexample.R;
import com.example.androidexample.URLManager;
import com.example.androidexample.User;
import com.example.androidexample.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Fragment that displays the user's list of friends.
 * <p>
 * Supports refreshing the list via {@link SwipeRefreshLayout} and allows users
 * to message or remove friends through buttons provided in each item of the {@link RecyclerView}.
 */
public class FriendsFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private FriendsAdapter adapter;
    private List<User> friendList;

    /**
     * Required empty public constructor.
     */
    public FriendsFragment() {}

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The root view for the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        friendList = new ArrayList<>();

        swipeRefreshLayout = rootView.findViewById(R.id.friend_list_layout);
        recyclerView = rootView.findViewById(R.id.recyclerViewFriends);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the adapter and set it to the RecyclerView
        adapter = new FriendsAdapter(getContext(), friendList, new FriendsAdapter.OnFriendActionListener() {
            @Override
            public void onMessageClicked(User friend) {
                goToChatActivity(friend);
            }

            @Override
            public void onRemoveFriendClicked(User friend) {
                removeFriend(friend);
            }
        });
        recyclerView.setAdapter(adapter);

        fetchFriends(); // Load friend list initially

        // Refresh the friend list on swipe down
        swipeRefreshLayout.setOnRefreshListener(() -> fetchFriends());

        return rootView;
    }

    /**
     * Launches the {@link ChatActivity} by creating a chat session
     * with the selected friend via a POST request.
     *
     * @param friend The selected friend to start a chat with.
     */
    private void goToChatActivity(User friend) {
        String url = URLManager.postChatSessionURL();
        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");
        Long id = prefs.getLong("id", -1);

        JSONArray usersArray = new JSONArray();
        try {
            JSONObject currentUser = new JSONObject();
            currentUser.put("id", id);
            usersArray.put(currentUser);

            JSONObject userObject = new JSONObject();
            userObject.put("id", friend.getId());
            usersArray.put(userObject);
        } catch (JSONException e) {
            Log.e("FriendsFragment JSONException", "Error building UserList for ChatSession POST request");
        }

        final String requestBody = usersArray.toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (!response.equals("One or more users are null!")) {
                        String serverUrl = URLManager.chatURL(response, username);
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("session_id", response);
                        intent.putExtra("server_url", serverUrl);
                        intent.putExtra("group_name", friend.getUsername());
                        startActivity(intent);
                    }
                },
                error -> Log.e("Volley Error", "Error: " + error.getMessage())) {
            @Override
            public byte[] getBody() {
                return requestBody.getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        VolleySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    /**
     * Fetches the list of friends from the backend and updates the RecyclerView.
     */
    private void fetchFriends() {
        SharedPreferences prefs = getContext().getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");
        String url = URLManager.getFriendsURL(username);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    friendList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            friendList.add(new User(response.getJSONObject(i)));
                        } catch (JSONException e) {
                            Log.e("FriendList Parse Error", e.toString());
                        }
                    }
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                },
                error -> {
                    Log.e("Volley Error", error.toString());
                    Toast.makeText(getContext(), "Connection Error", Toast.LENGTH_LONG).show();
                });

        VolleySingleton.getInstance(getContext()).addToRequestQueue(request);
    }

    /**
     * Sends a DELETE request to the backend to remove the selected friend from the user's list.
     *
     * @param friend The friend to be removed.
     */
    public void removeFriend(User friend) {
        SharedPreferences prefs = getContext().getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");
        String url = URLManager.removeFriendURL(username, friend.getUsername());

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                    friendList.remove(friend);
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    Log.e("removeFriend", "Error: " + error.getMessage());
                    Toast.makeText(getContext(), "Connection Error", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public byte[] getBody() {
                return null;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        VolleySingleton.getInstance(getContext()).addToRequestQueue(request);
    }
}
