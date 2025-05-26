package com.example.androidexample;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Singleton WebSocketManager instance used for managing WebSocket connections
 * in the Android application, with typing indicator support.
 */
public class WebSocketManager {

    private static WebSocketManager instance;
    private MyWebSocketClient webSocketClient;
    private WebSocketListener webSocketListener;
    private String username;
    private Timer typingTimer;

    // Typing indicator delay in milliseconds (3 seconds)
    private static final long TYPING_TIMEOUT = 3000;
    private boolean isTyping = false;

    private WebSocketManager() {}

    /**
     * Retrieves a synchronized instance of the WebSocketManager.
     */
    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    public void setWebSocketListener(WebSocketListener listener) {
        this.webSocketListener = listener;
    }

    public void removeWebSocketListener() {
        this.webSocketListener = null;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void connectWebSocket(String serverUrl) {
        try {
            URI serverUri = URI.create(serverUrl);
            webSocketClient = new MyWebSocketClient(serverUri);
            webSocketClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a message through the WebSocket.
     *
     * @param message The message to be sent.
     */
    public void sendMessage(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            try {
                // Create a message JSON object
                JSONObject jsonMessage = new JSONObject();
                jsonMessage.put("type", "message");
                jsonMessage.put("sender", username);
                jsonMessage.put("content", message);

                // Send message
                webSocketClient.send(jsonMessage.toString());

                // Reset typing indicator when a message is sent
                sendTypingStatus(false);
                isTyping = false;
            } catch (Exception e) {
                Log.d("ExceptionSendMessage:", e.getMessage());
            }
        }
    }

    /**
     * Sends typing status when user is typing in the input field.
     * Implements debounce to prevent spamming the server.
     */
    public void userIsTyping() {
        if (!isTyping) {
            isTyping = true;
            sendTypingStatus(true);
        }

        // Cancel any existing timer
        if (typingTimer != null) {
            typingTimer.cancel();
            typingTimer = null;
        }

        // Start new timer for typing timeout
        typingTimer = new Timer();
        typingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                isTyping = false;
                sendTypingStatus(false);
            }
        }, TYPING_TIMEOUT);
    }

    /**
     * Sends the typing status to the WebSocket server.
     *
     * @param isTyping true if user is typing, false if stopped typing
     */
    private void sendTypingStatus(boolean isTyping) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            try {
                JSONObject typingStatus = new JSONObject();
                typingStatus.put("type", "typing");
                typingStatus.put("sender", username);
                typingStatus.put("isTyping", isTyping);

                webSocketClient.send(typingStatus.toString());
            } catch (JSONException e) {
                Log.e("WebSocketManager", "Error creating typing status: " + e.getMessage());
            }
        }
    }

    public void disconnectWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }

        if (typingTimer != null) {
            typingTimer.cancel();
            typingTimer = null;
        }
    }

    private class MyWebSocketClient extends WebSocketClient {

        private MyWebSocketClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.d("WebSocket", "Connected");
            if (webSocketListener != null) {
                webSocketListener.onWebSocketOpen(handshakedata);
            }
        }

        @Override
        public void onMessage(String message) {
            Log.d("WebSocket", "Received message: " + message);

            try {
                JSONObject jsonMessage = new JSONObject(message);

                // Check if it's a typing indicator message
                if (jsonMessage.has("type") && "typing".equals(jsonMessage.getString("type"))) {
                    String sender = jsonMessage.getString("sender");
                    boolean isTyping = jsonMessage.getBoolean("isTyping");

                    // Only process typing indicators from other users
                    if (!sender.equals(username) && webSocketListener != null) {
                        webSocketListener.onTypingStatusUpdate(sender, isTyping);
                    }
                } else {
                    // Regular message
                    if (webSocketListener != null) {
                        webSocketListener.onWebSocketMessage(message);
                    }
                }
            } catch (JSONException e) {
                // If it's not valid JSON, treat as regular message
                if (webSocketListener != null) {
                    webSocketListener.onWebSocketMessage(message);
                }
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.d("WebSocket", "Closed");
            if (webSocketListener != null) {
                webSocketListener.onWebSocketClose(code, reason, remote);
            }
        }

        @Override
        public void onError(Exception ex) {
            Log.d("WebSocket", "Error");
            if (webSocketListener != null) {
                webSocketListener.onWebSocketError(ex);
            }
        }
    }
}