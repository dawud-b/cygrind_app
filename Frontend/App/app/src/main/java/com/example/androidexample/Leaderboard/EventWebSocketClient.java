package com.example.androidexample.Leaderboard;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
/**
 * A WebSocket client for handling communication with an event server.
 * This client establishes a connection to a WebSocket server and allows for receiving and sending messages
 * related to events. It uses a listener interface to notify about connection status, messages, and errors.
 */
public class EventWebSocketClient {

    private static final String TAG = "EventWebSocketClient";
    private WebSocketClient webSocketClient;  // WebSocket client instance
    private EventWebSocketListener listener;  // Listener for WebSocket events
    private String serverUrl;  // Server URL for WebSocket connection
    private String username;  // Username for the WebSocket connection
    private boolean isConnected = false;  // Connection status flag
    private Handler mainHandler;  // Handler to manage UI thread interaction

    /**
     * Constructor to initialize the WebSocket client with the server URL, username, and listener.
     *
     * @param serverUrl The URL of the WebSocket server.
     * @param username The username to be used for the WebSocket connection.
     * @param listener The listener to handle WebSocket events (connection, message, error).
     */
    public EventWebSocketClient(String serverUrl, String username, EventWebSocketListener listener) {
        this.serverUrl = serverUrl;
        this.username = username;
        this.listener = listener;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Interface for handling WebSocket events like connection, disconnection, message reception, and errors.
     */
    public interface EventWebSocketListener {
        /**
         * Called when the WebSocket connection is established successfully.
         */
        void onConnected();

        /**
         * Called when the WebSocket connection is closed.
         *
         * @param code The closure code.
         * @param reason The reason for the closure.
         */
        void onDisconnected(int code, String reason);

        /**
         * Called when an error occurs with the WebSocket connection.
         *
         * @param ex The exception that occurred.
         */
        void onError(Exception ex);

        /**
         * Called when a message is received from the WebSocket server.
         *
         * @param message The received message.
         */
        void onMessageReceived(String message);
    }

    /**
     * Connects to the WebSocket server and establishes communication.
     * It uses the server URL and username for the connection and initializes the WebSocket client.
     * If the connection is successful, it triggers the `onConnected()` callback.
     *
     * @throws URISyntaxException If the server URL is not valid.
     */
    public void connect() {
        try {
            URI uri = new URI(serverUrl + "/events/" + username);  // Construct the WebSocket URI
            webSocketClient = new WebSocketClient(uri) {

                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d(TAG, "WebSocket connection opened");
                    isConnected = true;
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onConnected();
                        }
                    });
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Received message: " + message);
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onMessageReceived(message);
                        }
                    });
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "WebSocket connection closed: " + reason);
                    isConnected = false;
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onDisconnected(code, reason);
                        }
                    });
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket error", ex);
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onError(ex);
                        }
                    });
                }
            };

            webSocketClient.connect();  // Connect to the WebSocket server
        } catch (URISyntaxException e) {
            Log.e(TAG, "URI syntax error", e);
            if (listener != null) {
                listener.onError(e);
            }
        }
    }

    /**
     * Disconnects from the WebSocket server.
     * If the client is currently connected, it will close the connection.
     */
    public void disconnect() {
        if (webSocketClient != null && isConnected) {
            webSocketClient.close();
        }
    }

    /**
     * Sends a message to the WebSocket server.
     * If the WebSocket client is not connected, an error is logged.
     *
     * @param message The message to send to the server.
     */
    public void sendMessage(String message) {
        if (webSocketClient != null && isConnected) {
            webSocketClient.send(message);
        } else {
            Log.e(TAG, "Cannot send message - WebSocket is not connected");
        }
    }

    /**
     * Returns whether the WebSocket client is currently connected to the server.
     *
     * @return True if connected, false otherwise.
     */
    public boolean isConnected() {
        return isConnected;
    }
}
