package com.example.androidexample.Groups;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.androidexample.HomeActivity;
import com.example.androidexample.Messaging.ChatActivity;
import com.example.androidexample.R;
import com.example.androidexample.URLManager;
import com.example.androidexample.User;
import com.example.androidexample.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsible for displaying and managing group members in the group page.
 * It allows group members to be removed, messages to be sent, and for users to leave the group.
 */
public class GroupMembersFragment extends Fragment {

    private RecyclerView recyclerView;   // RecyclerView to display group members
    private GroupMemberAdapter adapter;  // Adapter for RecyclerView
    private List<User> userList;         // List of group members
    private View rootView;               // Root view of the fragment
    private GroupPageActivity activity;  // Reference to the parent activity
    private Button leaveBtn;             // Button to leave the group

    /**
     * Default constructor for the fragment.
     */
    public GroupMembersFragment() {
        // Required empty public constructor
    }

    /**
     * Inflates the layout for this fragment, initializes the RecyclerView, and sets up necessary listeners.
     *
     * @param inflater The LayoutInflater object to inflate the views.
     * @param container The container in which the fragment's UI should be placed.
     * @param savedInstanceState If the fragment is being re-created from a previous state.
     * @return The root view of the fragment's layout.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        rootView = inflater.inflate(R.layout.fragment_members, container, false);
        if( rootView == null ) {
            System.out.println("Rootview is null");
        }

        activity = (GroupPageActivity)getActivity();

        // Initialize userList
        userList = new ArrayList<>();

        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.group_members_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        leaveBtn = rootView.findViewById(R.id.group_leave_button);
        if( leaveBtn == null ) {
            System.out.println("leave button is null");
        } else {
            leaveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    leaveGroup();
                }
            });
        }

        if( activity == null ) {
            Log.e("GroupMemberFragment", "activity is null");
        }

        if( activity.inGroup ) {
            Log.d("GroupMemberFragment", "groupId: " + activity.groupId);
            fetchMembers();
        } else {
            Toast.makeText(activity, "User not in group.", Toast.LENGTH_SHORT).show();
        }

        // Set the adapter
        adapter = new GroupMemberAdapter(userList, activity.isLeader, new GroupMemberAdapter.onRemoveClickListener() {
            @Override
            public void onRemoveClick(User user) {
                removeMember(user);
            }

            @Override
            public void onMessageClick(User user) {
                goToChatActivity(user);
            }
        });
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    /**
     * Starts the ChatActivity to initiate a chat session with the selected user.
     *
     * @param friend The user to chat with.
     */
    private void goToChatActivity(User friend) {
        String url = URLManager.postChatSessionURL();

        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");
        Long id = prefs.getLong("id", -1);

        // Prepare the JSON body with user data
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

        // Create the request body
        final String requestBody = usersArray.toString();

        // Create a new StringRequest
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("Response: " + response);

                        // Enter ChatBoxActivity if a session id is given
                        if( !response.equals("One or more users are null!")) {
                            String serverUrl = URLManager.chatURL(response, username);
                            Log.d("MessageInboxActivity", "URL:  " + serverUrl);

                            // Go to chat activity
                            Intent intent = new Intent(getActivity(), ChatActivity.class);
                            intent.putExtra("session_id", response);
                            intent.putExtra("server_url", serverUrl);
                            intent.putExtra("group_name", friend.getUsername());
                            startActivity(intent);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response (e.g., network issue)
                        System.out.println("Error: " + error.getMessage());
                    }
                }) {
            @Override
            public byte[] getBody() throws com.android.volley.AuthFailureError {
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
     * Allows the user to leave the group.
     * Sends a request to the server to leave the group and redirects the user to the home screen.
     */
    private void leaveGroup() {
        SharedPreferences prefs = activity.getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");
        String url = URLManager.leaveGroupURL(activity.groupId, username);

        // Create a StringRequest for the POST request
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the response from the server
                        Log.d("VolleyResponse", "Response: " + response);
                        Toast.makeText(activity, "Group left.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(activity, HomeActivity.class);
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                        Log.e("VolleyError", "Error: " + error.getMessage());
                        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            Toast.makeText(activity, "Group not found", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Add the request to the RequestQueue
        VolleySingleton.getInstance(activity).addToRequestQueue(stringRequest);
    }

    /**
     * Fetches the list of members in the group from the backend.
     */
    private void fetchMembers() {
        String url;
        if( activity != null ) {
            url = URLManager.getGroupMembersURL(activity.groupId);
        } else {
            Log.e("GroupMemberFragment", "activity is null, can't access groupId");
            return;
        }

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null, // No body for GET request
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Handle the response from the backend
                        try {
                            // Process the list of members here
                            for (int i = 0; i < response.length(); i++) {
                                User user = new User(response.getJSONObject(i));
                                userList.add(user);
                            }

                            // Update adapter
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            Toast.makeText(activity, "Group not found", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
        VolleySingleton.getInstance(activity).addToRequestQueue(jsonArrayRequest);
    }

    /**
     * Removes a member from the group.
     *
     * @param user The user to be removed from the group.
     */
    private void removeMember(User user) {
        SharedPreferences prefs = activity.getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");

        if(username.equals(user.getUsername())) {
            Toast.makeText(activity, "Use the leave button.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = URLManager.kickGroupMemberURL(activity.groupId, user.getUsername(), username);

        // Create a StringRequest for the POST request
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the response from the server
                        Log.d("VolleyResponse", "Response: " + response);
                        userList.remove(user);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(activity, "Member removed.", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                        Log.e("VolleyError", "Error: " + error.getMessage());
                        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            Toast.makeText(activity, "Group not found", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Add the request to the RequestQueue
        VolleySingleton.getInstance(activity).addToRequestQueue(stringRequest);
    }
}
