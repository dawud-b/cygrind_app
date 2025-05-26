package com.example.androidexample.Redemption;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidexample.HomeActivity;
import com.example.androidexample.Messaging.Reaction;
import com.example.androidexample.R;
import com.example.androidexample.URLManager;
import com.example.androidexample.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RedeemActivity extends AppCompatActivity {
    private TextView pointsTextView;
    private int userPoints = 100; // You can pull this from a database later
    private ItemAdapter adapter;
    private List<RedeemItem> itemList; // list for items
    private Set<String> unlockedReactions; // contains unlocked reaction ids
    private RecyclerView recyclerView;// view for items
    private ImageButton backBtn;
    private static final String TAG = "RedeemActivity"; // Class-level tag for logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem);

        backBtn = findViewById(R.id.redeem_backButton);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RedeemActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        pointsTextView = findViewById(R.id.pointsTextView);

        itemList = new ArrayList<>();
        getBalance();

        // sets up premium item display
        loadReactionsFromBackend();
        fetchUnlockedReactions();
    }
    private void tryInitRecyclerView() {
        if (itemList != null && unlockedReactions != null) {
            setupRecyclerView();
        }
    }


    private void updatePointsDisplay() {
        pointsTextView.setText("Points: " + userPoints);
    }

    private void onRedeemClicked(RedeemItem item) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");

        String url = URLManager.getPurchaseReactionURL(username, item.getReactionId());

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            userPoints = response.getInt("remainingPoints");
                            updatePointsDisplay();
                            unlockedReactions.add("" + item.getReactionId());
                            adapter.setUnlockedReactions(unlockedReactions); // refresh unlocked reactions
                            Toast.makeText(this, "Redeemed: " + item.getDisplayName(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Redemption failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void getBalance() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");

        String url = URLManager.getPointBalanceURL(username);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            int points = response.getInt("totalPoints");
                            userPoints = points;
                            updatePointsDisplay();
                        } else {
                            Toast.makeText(this, "Error: " + response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "JSON Parsing Error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Volley Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonRequest);
    }

    private void loadReactionsFromBackend() {
        String url = URLManager.getPremiumReactionsURL();
        Log.d(TAG, "Fetching premium reactions from URL: " + url);

        JsonArrayRequest jsonRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    itemList = new ArrayList<>();
                    Log.d(TAG, "Received response with " + response.length() + " items");

                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);

                            long reactionId = obj.getLong("id");
                            String name = obj.getString("name");
                            String emoji = obj.getString("emoji");
                            int cost = obj.getInt("pointCost");

                            Log.d(TAG, "Parsed reaction: ID=" + reactionId + ", Name=" + name + ", Emoji=" + emoji + ", Cost=" + cost);

                            itemList.add(new RedeemItem(reactionId, name, emoji, cost));
                        }

                        tryInitRecyclerView();
                        Log.d(TAG, "RecyclerView setup complete with " + itemList.size() + " items");

                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(this, "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error: " + error.toString());
                    error.printStackTrace();
                    Toast.makeText(this, "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(jsonRequest);
    }
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.redemptionView);
        adapter = new ItemAdapter(itemList, unlockedReactions, this::onRedeemClicked);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }
    private void fetchUnlockedReactions() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");

        String url = URLManager.getUserUnlockedReactionsURL(username); // e.g., /points/redeem/reactions/{username}
        Log.d("RedemptionActivity", "Fetching unlocked reactions from: " + url);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray unlocked = response.getJSONArray("unlockedReactions");
                            Log.d("unlockedEmojis", response.toString());
                            //Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_SHORT).show();

                            unlockedReactions = new HashSet<>();
                            for (int i = 0; i < unlocked.length(); i++) {
                                JSONObject obj = unlocked.getJSONObject(i);

                                long id = obj.getLong("id");
                                unlockedReactions.add("" + id); // Cost not needed here
                            }
                            Log.d("unlockedReactions length", "" + unlockedReactions.size());
                            tryInitRecyclerView();
                            // You can now show this in a reaction picker, dialog, or emoji bar
                            //showUnlockedReactions(unlockedList);

                        } else {
                            Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to parse unlocked reactions", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error fetching reactions", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(jsonRequest);
    }
}
