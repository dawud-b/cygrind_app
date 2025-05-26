package com.example.androidexample;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class WebSocketService extends Service {

    // key to WebSocketClient obj mapping - for multiple WebSocket connections
    private final Map<String, WebSocketClient> webSockets = new HashMap<>();

    private static final String TAG = "WebSocketService"; // Tag for logging

    public WebSocketService() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand Called");
        if (intent != null) {
            String action = intent.getAction();
            if ("CONNECT".equals(action)) {
                String url = intent.getStringExtra("url");      // eg, "ws://localhost:8080/chat/1/uname"
                String key = intent.getStringExtra("key");      // eg, "chat1" - refer to MainActivity where this Intent was called
                Log.d(TAG, "onStartCommand: Action CONNECT received. Connecting to " + url + " with key " + key);
                connectWebSocket(key, url);                       // Initialize WebSocket connection
            } else if ("DISCONNECT".equals(action)) {
                String key = intent.getStringExtra("key");
                Log.d(TAG, "onStartCommand: Action DISCONNECT received for key " + key);
                disconnectWebSocket(key);
            }
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {    // Register BroadcastReceiver to listen for messages from Activities
        super.onCreate();
        Log.d(TAG, "onCreate: WebSocketService created.");
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(messageReceiver, new IntentFilter("SendWebSocketMessage"));
    }

    @Override
    public void onDestroy() {   // Close WebSocket connection to prevent memory leaks
        super.onDestroy();
        Log.d(TAG, "onDestroy: WebSocketService destroyed. Closing WebSocket connections.");
        for (WebSocketClient client : webSockets.values()) {
            client.close();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Initialize WebSocket client and define callback for message reception
    private void connectWebSocket(String key, String url) {
        Log.d(TAG, "connectWebSocket: Attempting to connect to URL: " + url);

        try {
            URI serverUri = URI.create(url);
            WebSocketClient webSocketClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d(TAG, "onOpen: WebSocket connection opened for key: " + key);
                    Log.d(TAG, "onOpen: Handshake data: " + handshakedata);
                }

                @Override
                public void onMessage(String message) {
                    // Whenever a message is received for this WebSocketClient obj
                    // broadcast the message internally (within the app) with its corresponding key
                    // only the Activities who care about this message will act accordingly
                    Log.d(TAG, "onMessage: Message received for key " + key + ": " + message);
                    Intent intent = new Intent("WebSocketMessageReceived");
                    intent.putExtra("key", key);
                    intent.putExtra("message", message);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "onClose: WebSocket connection closed for key: " + key + " with code: " + code + " and reason: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "onError: Error occurred for key " + key, ex);
                }
            };

            webSocketClient.connect();              // connect to the websocket
            webSockets.put(key, webSocketClient);   // add this instance to the mapping
            Log.d(TAG, "connectWebSocket: WebSocket client added for key: " + key);

        } catch (Exception e) {
            Log.e(TAG, "connectWebSocket: Failed to connect to WebSocket", e);
        }
    }

    // Listen to the messages from Activities
    // Send the message to its designated Websocket connection
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra("key");
            String message = intent.getStringExtra("message");

            WebSocketClient webSocket = webSockets.get(key);
            if (webSocket != null) {
                Log.d(TAG, "onReceive: Sending message to WebSocket with key " + key + ": " + message);
                webSocket.send(message);
            } else {
                Log.w(TAG, "onReceive: No WebSocket connection found for key " + key);
            }
        }
    };

    private void disconnectWebSocket(String key) {
        Log.d(TAG, "disconnectWebSocket: Disconnecting WebSocket for key: " + key);
        if (webSockets.containsKey(key)) {
            webSockets.get(key).close();
            Log.d(TAG, "disconnectWebSocket: WebSocket for key " + key + " closed.");
        } else {
            Log.w(TAG, "disconnectWebSocket: No WebSocket found for key " + key);
        }
    }
}
