package com.example.androidexample.Friends;

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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.androidexample.R;
import com.example.androidexample.URLManager;
import com.example.androidexample.VolleySingleton;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link Fragment} subclass that displays and manages incoming friend requests.
 * <p>
 * Uses a {@link RecyclerView} to display requests with options to accept or decline.
 * Integrates with a backend using Volley for network communication.
 */
public class RequestsFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private List<FriendRequest> friendRequestList = new ArrayList<>();

    /**
     * Required empty public constructor.
     */
    public RequestsFragment() {}

    /**
     * Inflates the fragment layout and sets up RecyclerView and network operations.
     *
     * @param inflater           LayoutInflater to inflate views.
     * @param container          ViewGroup container of the fragment.
     * @param savedInstanceState Previously saved instance state.
     * @return The inflated root view of the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fetchRequests();
        View rootView = inflater.inflate(R.layout.fragment_requests, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerViewRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefreshLayout = rootView.findViewById(R.id.friend_requests_layout);

        // Initialize adapter with action listener for requests
        adapter = new RequestAdapter(getContext(), friendRequestList, new RequestAdapter.OnRequestActionListener() {
            @Override
            public void onAcceptClick(FriendRequest friendRequest) {
                acceptRequest(friendRequest);
            }

            @Override
            public void onDeclineClick(FriendRequest friendRequest) {
                declineRequest(friendRequest);
            }
        });
        recyclerView.setAdapter(adapter);

        // Refresh the list when the user swipes down
        swipeRefreshLayout.setOnRefreshListener(() -> fetchRequests());

        return rootView;
    }

    /**
     * Sends a POST request to accept a friend request.
     *
     * @param r The {@link FriendRequest} object representing the friend request.
     */
    private void acceptRequest(FriendRequest r) {
        Long requestId = r.getId();
        String url = URLManager.getFriendAccept(requestId);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                    friendRequestList.remove(r);
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    Log.e("acceptRequest", "Error: " + error.getMessage());
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

    /**
     * Sends a DELETE request to decline a friend request.
     *
     * @param r The {@link FriendRequest} object representing the friend request.
     */
    private void declineRequest(FriendRequest r) {
        Long requestId = r.getId();
        String url = URLManager.getFriendAccept(requestId);

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                    friendRequestList.remove(r);
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    Log.e("declineRequest", "Error: " + error.getMessage());
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

    /**
     * Fetches the list of incoming friend requests from the backend.
     */
    private void fetchRequests() {
        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");

        if (username.equals("")) {
            Log.e("SharedPrefs Error", "SharedPrefs username not set");
            return;
        }

        String url = URLManager.getReceivedRequestsURL(username);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("Volley Response", response.toString());
                    friendRequestList.clear();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            friendRequestList.add(new FriendRequest(response.getJSONObject(i)));
                        } catch (JSONException e) {
                            Log.e("Request Parse Error", e.toString());
                        }
                    }

                    adapter.notifyDataSetChanged();
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
}
