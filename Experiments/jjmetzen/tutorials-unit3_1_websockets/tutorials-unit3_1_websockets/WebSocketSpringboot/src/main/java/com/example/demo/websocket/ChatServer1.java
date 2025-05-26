package com.example.demo.websocket;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.ArrayList;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Represents a WebSocket chat server for handling real-time communication
 * between users. Each user connects to the server using their unique
 * username.
 *
 * This class is annotated with Spring's `@ServerEndpoint` and `@Component`
 * annotations, making it a WebSocket endpoint that can handle WebSocket
 * connections at the "/chat/{username}" endpoint.
 *
 * Example URL: ws://localhost:8080/chat/username
 *
 * The server provides functionality for broadcasting messages to all connected
 * users and sending messages to specific users.
 */
@ServerEndpoint("/chat/1/{username}")
@Component
public class ChatServer1 {

    // Store all socket session and their corresponding username
    // Two maps for the ease of retrieval by key
    private static Map<Session, String> sessionUsernameMap = new Hashtable<>();
    private static Map<String, Session> usernameSessionMap = new Hashtable<>();

    // FEATURE 1: User Status Tracking
    private static Map<String, String> userStatusMap = new Hashtable<>();

    // FEATURE 2: User Colors for Chat Messages
    private static Map<String, String> userColorMap = new Hashtable<>();
    private static final String[] COLORS = {"#FF5733", "#33FF57", "#3357FF", "#FF33A8", "#33FFF5"};

    // server side logger
    private final Logger logger = LoggerFactory.getLogger(ChatServer1.class);

    // FEATURE 3: Emoji Replacement
    private String replaceEmojis(String message) {
        message = message.replace(":smile:", "😊");
        message = message.replace(":laugh:", "😄");
        message = message.replace(":sad:", "😢");
        message = message.replace(":heart:", "❤️");
        message = message.replace(":thumbsup:", "👍");
        return message;
    }

    /**
     * This method is called when a new WebSocket connection is established.
     *
     * @param session  represents the WebSocket session for the connected user.
     * @param username username specified in path parameter.
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) throws IOException {

        // server side log
        logger.info("[onOpen] " + username);

        // Handle the case of a duplicate username
        if (usernameSessionMap.containsKey(username)) {
            session.getBasicRemote().sendText("Username already exists");
            session.close();
        } else {
            // map current session with username
            sessionUsernameMap.put(session, username);

            // map current username with session
            usernameSessionMap.put(username, session);

            // FEATURE 1: Set default status
            userStatusMap.put(username, "online");

            // FEATURE 2: Assign a random color to the user
            int colorIndex = usernameSessionMap.size() % COLORS.length;
            userColorMap.put(username, COLORS[colorIndex]);

            // send to the user joining in
            sendMessageToPArticularUser(username, "Welcome to the chat server, " + username);

            // Send help message
            sendMessageToPArticularUser(username, "Type /help to see available commands");

            // send to everyone in the chat
            broadcast("User: " + username + " has Joined the Chat");
        }
    }

    /**
     * Handles incoming WebSocket messages from a client.
     *
     * @param session The WebSocket session representing the client's connection.
     * @param message The message received from the client.
     */
    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        // get the username by session
        String username = sessionUsernameMap.get(session);

        // server side log
        logger.info("[onMessage] " + username + ": " + message);

        // FEATURE 3: Help command
        if (message.equals("/help")) {
            StringBuilder helpMsg = new StringBuilder("Available Commands:\n");
            helpMsg.append("- /status [status] - Set your status (online, away, busy, etc.)\n");
            helpMsg.append("- /color - View your assigned chat color\n");
            helpMsg.append("- /users - List all online users and their statuses\n");
            helpMsg.append("- /history - View chat history\n");
            helpMsg.append("- @username [message] - Send a private message\n");
            helpMsg.append("Emoji shortcuts: :smile: :laugh: :sad: :heart: :thumbsup:");
            sendMessageToPArticularUser(username, helpMsg.toString());
            return;
        }

        // FEATURE 1: Status command
        if (message.startsWith("/status")) {
            if (message.length() > 8) {
                String newStatus = message.substring(8).trim();
                userStatusMap.put(username, newStatus);
                broadcast("System: " + username + " is now " + newStatus);
            } else {
                sendMessageToPArticularUser(username, "Your current status is: " + userStatusMap.get(username));
            }
            return;
        }

        // FEATURE 2: Color command
        if (message.equals("/color")) {
            String userColor = userColorMap.get(username);
            sendMessageToPArticularUser(username, "Your chat color is: " + userColor);
            return;
        }

        // FEATURE 4: Users command
        if (message.equals("/users")) {
            StringBuilder userList = new StringBuilder("Online Users:\n");
            for (String user : usernameSessionMap.keySet()) {
                String status = userStatusMap.get(user);
                userList.append("- ").append(user).append(" [").append(status).append("]\n");
            }
            sendMessageToPArticularUser(username, userList.toString());
            return;
        }

        // FEATURE 5: History command
        if (message.equals("/history")) {
            sendMessageToPArticularUser(username, "--- Chat History ---");
            for (String historyMessage : messageHistory) {
                sendMessageToPArticularUser(username, historyMessage);
            }
            sendMessageToPArticularUser(username, "--- End of History ---");
            return;
        }

        // Direct message to a user using the format "@username <message>"
        if (message.startsWith("@")) {
            // split by space
            String[] split_msg = message.split("\\s+");

            // Combine the rest of message
            StringBuilder actualMessageBuilder = new StringBuilder();
            for (int i = 1; i < split_msg.length; i++) {
                actualMessageBuilder.append(split_msg[i]).append(" ");
            }
            String destUserName = split_msg[0].substring(1);    //@username and get rid of @
            String actualMessage = actualMessageBuilder.toString();

            // FEATURE 3: Process emojis
            actualMessage = replaceEmojis(actualMessage);

            // Check if user exists
            if (usernameSessionMap.containsKey(destUserName)) {
                sendMessageToPArticularUser(destUserName, "[DM from " + username + "]: " + actualMessage);
                sendMessageToPArticularUser(username, "[DM from " + username + "]: " + actualMessage);
            } else {
                sendMessageToPArticularUser(username, "Error: User '" + destUserName + "' does not exist or is offline.");
            }
        } else { // Message to whole chat
            // FEATURE 3: Process emojis
            message = replaceEmojis(message);

            // Add status and color to broadcast message
            String userStatus = userStatusMap.get(username);
            String colorInfo = "[Color:" + userColorMap.get(username) + "]";
            broadcast(username + " [" + userStatus + "] " + colorInfo + ": " + message);
        }
    }

    /**
     * Handles the closure of a WebSocket connection.
     *
     * @param session The WebSocket session that is being closed.
     */
    @OnClose
    public void onClose(Session session) throws IOException {
        // get the username from session-username mapping
        String username = sessionUsernameMap.get(session);

        // server side log
        logger.info("[onClose] " + username);

        // Remove from status and color maps
        userStatusMap.remove(username);
        userColorMap.remove(username);

        // remove user from memory mappings
        sessionUsernameMap.remove(session);
        usernameSessionMap.remove(username);

        // send the message to chat
        broadcast(username + " disconnected");
    }

    /**
     * Handles WebSocket errors that occur during the connection.
     *
     * @param session   The WebSocket session where the error occurred.
     * @param throwable The Throwable representing the error condition.
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        // get the username from session-username mapping
        String username = sessionUsernameMap.get(session);

        // do error handling here
        logger.info("[onError]" + username + ": " + throwable.getMessage());
    }

    /**
     * Sends a message to a specific user in the chat (DM).
     * ADDED A TIMESTAMP FOR EACH INDIVIDUAL MESSAGE.
     * @param username The username of the recipient.
     * @param message  The message to be sent.
     */
    private void sendMessageToPArticularUser(String username, String message) {
        try {
            String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            String formattedMessage = "[" + timestamp + "] " + message;
            usernameSessionMap.get(username).getBasicRemote().sendText(formattedMessage);
        } catch (IOException e) {
            logger.info("[DM Exception] " + e.getMessage());
        }
    }

    /**
     * Broadcasts a message to all users in the chat.
     *
     * @param message The message to be broadcasted to all users.
     */

    /**
     * ADDED TIMESTAMP FOR STARTING A CHAT and HISTORY
     * @param message
     */
    private static final int MAX_HISTORY = 10;
    private static java.util.List<String> messageHistory = new java.util.ArrayList<>();

    private void broadcast(String message) {
        // Added message to history
        if (messageHistory.size() >= MAX_HISTORY) {
            messageHistory.remove(0);
        }
        messageHistory.add(message);

        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        String formattedMessage = "[" + timestamp + "] " + message;

        sessionUsernameMap.forEach((session, username) -> {
            try {
                session.getBasicRemote().sendText(formattedMessage);
            } catch (IOException e) {
                logger.info("[Broadcast Exception] " + e.getMessage());
            }
        });
    }
}