package com.example.androidexample.Messaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidexample.R;
import com.example.androidexample.Redemption.RedeemItem;
import com.example.androidexample.URLManager;
import com.example.androidexample.User;
import com.example.androidexample.VolleySingleton;
import com.example.androidexample.WebSocketService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatFragment extends Fragment {
    private String sessionId;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private Button sendBtn;
    private EditText msgEntry;
    private String currentUser;
    private TextView dot1, dot2, dot3;
    private LinearLayout typingIndicatorContainer;
    private Animation typingDotAnimation;

    // used for controlling animation
    private boolean isTyping = false;
    private boolean isFirstCycle = true;
    private TextView typingTextView;
    private Set<Reaction> unlockedReactions;

    // required constructor
    public ChatFragment() {};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        View view = inflater.inflate(R.layout.fragment_chatbox, container, false);

        // store current username
        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", 0);
        currentUser = prefs.getString("username", "");

        // initialize messageList
        messageList = new ArrayList<>();

        // set typing indicator fields
        dot1 = view.findViewById(R.id.dot1);
        dot2 = view.findViewById(R.id.dot2);
        dot3 = view.findViewById(R.id.dot3);
        typingIndicatorContainer = view.findViewById(R.id.typing_indicator);
        typingDotAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.typing_dot_animation);
        typingTextView = view.findViewById(R.id.typing_name);

        // initalize views
        recyclerView = view.findViewById(R.id.recyclerViewMessage);
        sendBtn = view.findViewById(R.id.buttonSend);
        msgEntry = view.findViewById(R.id.messageEditText);

        unlockedReactions = new HashSet<>();
        fetchUnlockedReactions();

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        Bundle extras = getActivity().getIntent().getExtras();
        if( extras != null ) { // when coming from MessageInbox
            sessionId = extras.getString("session_id");
            String url = extras.getString("server_url");

            Intent serviceIntent = new Intent(getActivity(), WebSocketService.class);
            serviceIntent.setAction("CONNECT");
            serviceIntent.putExtra("key", sessionId);
            serviceIntent.putExtra("url", url);
            getActivity().startService(serviceIntent);
        } else if( getArguments() != null ) { // when used as fragment in groups
            sessionId = getArguments().getString("session_id");
            String url = getArguments().getString("server_url");

            Intent serviceIntent = new Intent(getActivity(), WebSocketService.class);
            serviceIntent.setAction("CONNECT");
            serviceIntent.putExtra("key", sessionId);
            serviceIntent.putExtra("url", url);
            getActivity().startService(serviceIntent);
        }

        // used to control animation
        // Handler to delay hiding the typing indicator
        Handler typingHandler = new Handler();
        Runnable hideTypingRunnable = new Runnable() {
            @Override
            public void run() {
                // send [User stopped typing] when done typing
                Intent intent = new Intent("SendWebSocketMessage");
                intent.putExtra("key", sessionId);
                intent.putExtra("message", "[User stopped typing]");
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                Log.d("Chatbox Activity", "Broadcast sent for message: " + "[User is typing]");
                isTyping = false;
            }
        };

        msgEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // send [User is typing] to socket when not actively typing
                if( !isTyping ) {
                    Intent intent = new Intent("SendWebSocketMessage");
                    intent.putExtra("key", sessionId);
                    intent.putExtra("message", "[User is typing]");
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                    Log.d("Chatbox Activity", "Broadcast sent for message: " + "[User is typing]");
                    isTyping = true;
                }

                // Remove any existing callbacks to hide the typing indicator (this resets the 5-second timer)
                typingHandler.removeCallbacks(hideTypingRunnable);

                // Re-schedule the Runnable to hide the indicator after 5 seconds of inactivity
                typingHandler.postDelayed(hideTypingRunnable, 5000); // 5000ms = 5 seconds
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = msgEntry.getText().toString().trim();
                // broadcast this message to the WebSocketService
                // tag it with the key - to specify which WebSocketClient (connection) to send
                // in this case: "chat1"
                if (message.isEmpty()) {
                    Log.w("Chatbox Activity", "sendBtn clicked but message was empty.");
                    return;
                }
                Intent intent = new Intent("SendWebSocketMessage");
                intent.putExtra("key", sessionId);
                intent.putExtra("message", msgEntry.getText().toString());
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                Log.d("Chatbox Activity", "Broadcast sent for message: " + message);
                msgEntry.setText("");
            }
        });

        return view;
    }

    private void setAdapter() {
        // Set the adapter
        messageAdapter = new MessageAdapter(messageList, new MessageAdapter.OnReactionClickListener() {
            @Override
            public void onReactionClicked(Long messageId, String reactionType) {
                // send reaction message over websocket
                Intent intent = new Intent("SendWebSocketMessage");
                intent.putExtra("key", sessionId);
                intent.putExtra("message", "[Reaction]:" + messageId + ":" + reactionType);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            }
        }, unlockedReactions);
        recyclerView.setAdapter(messageAdapter);
    }

    // Function to start the animation on the dots
    private void startTypingIndicator() {
        typingIndicatorContainer.setVisibility(View.VISIBLE);

        // Set up animation for dot 1
        dot1.setVisibility(View.VISIBLE);
        startDotAnimation(dot1, 0); // Start immediately

        // Set up animation for dot 2 (slightly delayed)
        dot2.setVisibility(View.VISIBLE);
        startDotAnimation(dot2, isFirstCycle ? 200 : 0); // Delay of 200ms

        // Set up animation for dot 3 (delayed more)
        dot3.setVisibility(View.VISIBLE);
        startDotAnimation(dot3, isFirstCycle ? 400 : 0); // Delay of 400ms

        if(isFirstCycle) {
            isFirstCycle = false;
        }
    }

    // Function to start scaling animation for a dot with a delay
    private void startDotAnimation(final TextView dot, long delay) {
        // Scale animation (expand from the center)
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0, 1,  // fromX, toX (scale from 0 to 1)
                0, 1,  // fromY, toY (scale from 0 to 1)
                Animation.RELATIVE_TO_SELF, 0.5f,  // pivotX
                Animation.RELATIVE_TO_SELF, 0.5f); // pivotY
        scaleAnimation.setDuration(400); // Animation duration for each dot
        scaleAnimation.setStartOffset(delay); // Set delay for cascade effect
        scaleAnimation.setRepeatMode(Animation.REVERSE); // Repeat the animation in reverse (expand and shrink)
        scaleAnimation.setRepeatCount(Animation.INFINITE); // Repeat indefinitely

        // Alpha animation (fade in)
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1); // Fade from invisible to visible
        alphaAnimation.setDuration(400);
        alphaAnimation.setStartOffset(delay);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        scaleAnimation.setRepeatCount(Animation.INFINITE);

        // Combine the animations
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        dot.startAnimation(animationSet);
    }


    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // verify key
            String key = intent.getStringExtra("key");
            if (sessionId.equals(key)){
                String messageStr = intent.getStringExtra("message");
                Log.d("message received", "message: " + messageStr);

                // parse [Server] messages - connection and typing indicator
                if( messageStr.contains("[Server]")) {
                    if (messageStr.contains("is typing")) {
                        String[] split = messageStr.split(" ");
                        String typing_username = split[1];
                        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", 0);
                        String current_username = prefs.getString("username", "");
                        if (!typing_username.equals(current_username)) {
                            startTypingIndicator();
                            isFirstCycle = true;
                            typingTextView.setText(typing_username); // display username
                        }
                    } else if (messageStr.contains("stopped typing")) {
                        typingIndicatorContainer.setVisibility(View.INVISIBLE);
                    }
                }
                else if( messageStr.contains("[Reaction]")) {
                    Log.d("reaction received", "reaction: " + messageStr);
                    String[] split = messageStr.split(":");
                    Long messageId = (long) Integer.parseInt(split[1]);
                    int reaction = Integer.parseInt(split[2]);
                    for(Message msg : messageList) {
                        if( messageId == msg.getId() ) {
                            msg.addReaction(new Reaction(null, reaction));
                            break;
                        }
                    }
                    messageAdapter.notifyDataSetChanged();
                }
                // regular message
                else {
                    // parse message string into messsage object
                    Message messageObj = parseMessage(messageStr, currentUser);

                    // eliminates bug where messages are displayed several times
                    for (Message msg : messageList) {
                        if (msg.getId() == messageObj.getId()) {
                            return;
                        }
                    }

                    Log.d("message received", "message object: " + messageObj.toString());

                    getActivity().runOnUiThread(() -> {
                        messageList.add(messageObj);
                        messageAdapter.notifyItemInserted(messageAdapter.getItemCount());
                        int lastPosition = messageAdapter.getItemCount() - 1;
                        recyclerView.smoothScrollToPosition(lastPosition);
                    });
                }
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(messageReceiver,
                new IntentFilter("WebSocketMessageReceived"));
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(messageReceiver);
    }

    private Message parseMessage(String jsonString, String username) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            // Parsing the user object
            JSONObject userJson = jsonObject.getJSONObject("user");
            User user = new User(userJson);

            // Parsing the message object
            Message message = new Message();
            message.setId(jsonObject.getLong("id"));
            message.setUser(user);
            message.setContent(jsonObject.getString("content"));
            message.setSent(new Date(jsonObject.getLong("sent")));

            JSONArray reactions = jsonObject.getJSONArray("reactions");
            message.setReactions(reactions);

            if( message.getSender().getUsername().equals(username)) {
                message.isCurrentUser = true;
            } else {
                message.isCurrentUser = false;
            }

            return message;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void fetchUnlockedReactions() {
        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", 0);
        String username = prefs.getString("username", "");

        String url = URLManager.getUserUnlockedReactionsURL(username); // e.g., /points/redeem/reactions/{username}
        Log.d("ChatActivity", "Fetching unlocked reactions from: " + url);

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
                                String name = obj.getString("name");
                                int emoji = obj.getInt("emoji");
                                // Optional: you can grab "purchaseDate" too
                                Reaction reaction = new Reaction(emoji);
                                reaction.setPremium(Boolean.TRUE);
                                unlockedReactions.add(reaction); // Cost not needed here
                            }
                            setAdapter();
                            Log.d("unlockedReactions length", "" + unlockedReactions.size());
                            // You can now show this in a reaction picker, dialog, or emoji bar
                            //showUnlockedReactions(unlockedList);

                        } else {
                            Toast.makeText(getActivity(), response.getString("message"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Failed to parse unlocked reactions", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getActivity(), "Error fetching reactions", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(getActivity()).addToRequestQueue(jsonRequest);
    }
}
