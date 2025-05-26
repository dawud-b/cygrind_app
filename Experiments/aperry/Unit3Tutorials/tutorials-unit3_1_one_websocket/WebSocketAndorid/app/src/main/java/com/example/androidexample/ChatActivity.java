package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * ChatActivity handles the chat interface where users can send and receive messages
 * using a WebSocket connection, with typing indicator support.
 */
public class ChatActivity extends AppCompatActivity implements WebSocketListener {

    private Button sendBtn;
    private EditText msgEtx;
    private TextView msgTv;
    private TextView typingIndicatorTv;

    // Track users who are currently typing
    private Map<String, Long> typingUsers = new HashMap<>();
    // Handler for periodic cleanup of typing users
    private android.os.Handler typingCleanupHandler = new android.os.Handler();
    private static final long TYPING_EXPIRY = 5000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        /* initialize UI elements */
        sendBtn = findViewById(R.id.sendBtn);
        msgEtx = findViewById(R.id.msgEdt);
        msgTv = findViewById(R.id.tx1);

        // Initialize typing indicator TextView
        typingIndicatorTv = findViewById(R.id.typingIndicator);
        typingIndicatorTv.setVisibility(View.GONE);

        /* connect this activity to the websocket instance */
        WebSocketManager.getInstance().setWebSocketListener(ChatActivity.this);

        /* send button listener */
        sendBtn.setOnClickListener(v -> {
            try {
                String message = msgEtx.getText().toString();
                if (!message.trim().isEmpty()) {
                    // send message
                    WebSocketManager.getInstance().sendMessage(message);
                    // clear text box once the msg is sent
                    msgEtx.setText("");
                }
            } catch (Exception e) {
                Log.d("ExceptionSendMessage:", e.getMessage());
            }
        });

        /* Add text watcher to detect typing */
        msgEtx.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Notify typing status when user types
                WebSocketManager.getInstance().userIsTyping();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        // Start periodic cleanup of typing users
        startTypingCleanup();
    }


    /**
     * Periodic cleanup of typing users who haven't refreshed their status
     */
    private void startTypingCleanup() {
        typingCleanupHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                boolean updated = false;

                // Use iterator to safely remove while iterating
                Iterator<Map.Entry<String, Long>> iterator = typingUsers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Long> entry = iterator.next();
                    if (currentTime - entry.getValue() > TYPING_EXPIRY) {
                        iterator.remove();
                        updated = true;
                    }
                }

                if (updated) {
                    updateTypingIndicator();
                }

                // Schedule next cleanup
                typingCleanupHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    @Override
    public void onWebSocketMessage(String message) {
        try {
            JSONObject jsonMessage = new JSONObject(message);

            // Ignore typing messages (handled separately)
            if (jsonMessage.has("type") && "typing".equals(jsonMessage.getString("type"))) {
                return;
            }

            // Process normal chat messages
            final String messageContent;
            if (jsonMessage.has("content")) {
                // Structured message
                String sender = jsonMessage.getString("sender");
                String content = jsonMessage.getString("content");
                messageContent = sender + ": " + content;
            } else {
                // Legacy plain text message
                messageContent = message;
            }

            runOnUiThread(() -> {
                String s = msgTv.getText().toString();
                msgTv.setText(s + "\n" + messageContent);
            });
        } catch (JSONException e) {
            // Plain text message
            runOnUiThread(() -> {
                String s = msgTv.getText().toString();
                msgTv.setText(s + "\n" + message);
            });
        }
    }



    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        String closedBy = remote ? "server" : "local";
        runOnUiThread(() -> {
            String s = msgTv.getText().toString();
            msgTv.setText(s + "\n---\nconnection closed by " + closedBy + "\nreason: " + reason);
        });
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        // Connection successful
    }

    @Override
    public void onWebSocketError(Exception ex) {
        runOnUiThread(() -> {
            String s = msgTv.getText().toString();
            msgTv.setText(s + "\n---\nError: " + ex.getMessage());
        });
    }

    @Override
    public void onTypingStatusUpdate(String username, boolean isTyping) {
        if (isTyping) {
            typingUsers.put(username, System.currentTimeMillis());
        } else {
            typingUsers.remove(username);
        }
        updateTypingIndicator();
    }

    private void updateTypingIndicator() {
        runOnUiThread(() -> {
            if (typingUsers.isEmpty()) {
                typingIndicatorTv.setVisibility(View.GONE);
            } else {
                typingIndicatorTv.setText("typing...");
                typingIndicatorTv.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        typingCleanupHandler.removeCallbacksAndMessages(null);
    }
}