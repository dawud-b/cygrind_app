package com.example.androidexample.Messaging;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.BroadcastReceiver;
import android.content.Context;

import com.example.androidexample.R;
import com.example.androidexample.User;
import com.example.androidexample.WebSocketService;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The ChatActivity class represents the user interface for a chat group.
 * It displays the group title and allows users to interact with the chat.
 * It includes a back button to return to the previous activity and dynamically loads the ChatFragment.
 */
public class ChatActivity extends AppCompatActivity {

    private TextView groupTitle;  // TextView to display the group title
    private ImageButton backBtn;  // ImageButton to navigate back to the previous activity

    /**
     * Called when the activity is created. This method sets up the layout,
     * retrieves the group name from the intent's extras, and dynamically adds the ChatFragment.
     * It also sets up the back button to finish the activity when clicked.
     *
     * @param savedInstanceState If the activity is being re-initialized, this Bundle contains
     *                           the previously saved state. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbox);

        // Initialize views
        backBtn = findViewById(R.id.buttonBack);
        groupTitle = findViewById(R.id.groupNameTitle);

        // Retrieve group name from intent extras and set it to the TextView
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            groupTitle.setText(extras.getString("group_name"));
        }

        // Add the ChatFragment dynamically if this is the first time the activity is created
        if (savedInstanceState == null) {
            ChatFragment chatFragment = new ChatFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.chatFragmentContainer, chatFragment)
                    .commit();
        }

        // Set up the back button click listener to finish the activity (return to previous screen)
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();  // Closes the current activity and returns to the previous one
            }
        });
    }
}
