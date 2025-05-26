package onetoone.Events;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import onetoone.users.User;
import onetoone.users.UserRepository;
import onetoone.users.WeightClass;

@ServerEndpoint("/events/{username}")
@Component
public class EventsServer {

    private static Map<Session, String> sessionUsernameMap = new Hashtable<>();
    private static Map<String, Session> usernameSessionMap = new Hashtable<>();
    private static Map<String, Boolean> isAdminMap = new Hashtable<>();

    private static EventService eventService;
    private static UserRepository userRepository;

    private final Logger logger = LoggerFactory.getLogger(EventsServer.class);

    @Autowired
    public void setEventService(EventService service) {
        eventService = service;
    }

    @Autowired
    public void setUserRepository(UserRepository repository) {
        userRepository = repository;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        try {
            if (username == null || username.trim().isEmpty()) {
                sendTextSafely(session, "Error: Username cannot be empty");
                closeSafely(session);
                return;
            }

            User user = userRepository.findByUsername(username);
            if (user == null) {
                sendTextSafely(session, "Error: Invalid username");
                closeSafely(session);
                return;
            }

            sessionUsernameMap.put(session, username);
            usernameSessionMap.put(username, session);
            isAdminMap.put(username, "Admin".equals(user.getUserRole()));

            sendMessageToUser(username, "Welcome to the Events system, " + username);
            sendMessageToUser(username, "Type /help to see available commands");
            sendActiveEvents(username);
        } catch (Exception e) {
            logger.error("Error during connection: " + e.getMessage());
            sendTextSafely(session, "Error occurred during connection. Please try again.");
            closeSafely(session);
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        try {
            String username = sessionUsernameMap.get(session);
            if (username == null) {
                return;
            }

            if (message == null || message.trim().isEmpty()) {
                sendMessageToUser(username, "Please enter a command. Type /help for available commands.");
                return;
            }

            message = message.trim();

            if (message.equals("/help")) {
                handleHelpCommand(username);
                return;
            }

            if (message.equals("/events")) {
                sendActiveEvents(username);
                return;
            }

            if (message.startsWith("/create ")) {
                handleCreateEventCommand(username, message.substring(8));
                return;
            }

            if (message.startsWith("/delete ")) {
                handleDeleteEventCommand(username, message.substring(8));
                return;
            }

            if (message.startsWith("/activate ")) {
                handleActivateEventCommand(username, message.substring(10), true);
                return;
            }

            if (message.startsWith("/deactivate ")) {
                handleActivateEventCommand(username, message.substring(12), false);
                return;
            }

            if (message.startsWith("/leaderboard ")) {
                handleLeaderboardCommand(username, message.substring(12));
                return;
            }

            if (message.startsWith("/score ")) {
                handleSubmitScoreCommand(username, message.substring(7));
                return;
            }

            sendMessageToUser(username, "Unknown command '" + message + "'. Type /help for available commands.");
        } catch (Exception e) {
            logger.error("Error processing message: " + e.getMessage());
            try {
                String username = sessionUsernameMap.get(session);
                if (username != null) {
                    sendMessageToUser(username, "An error occurred processing your command. Please try again.");
                }
            } catch (Exception ex) {
                logger.error("Error recovery failed: " + ex.getMessage());
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        String username = sessionUsernameMap.get(session);
        sessionUsernameMap.remove(session);
        if (username != null) {
            usernameSessionMap.remove(username);
            isAdminMap.remove(username);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        String username = sessionUsernameMap.get(session);
        logger.error("WebSocket error: " + throwable.getMessage());

        if (username != null && session.isOpen()) {
            try {
                sendMessageToUser(username, "An error occurred with your connection. You may need to reconnect.");
            } catch (Exception e) {
                logger.error("Failed to notify user of error");
            }
        }
    }

    private void handleHelpCommand(String username) {
        try {
            StringBuilder helpMsg = new StringBuilder("Available Commands:\n");
            helpMsg.append("- /events - List all active events\n");
            helpMsg.append("- /leaderboard [eventId] - View leaderboard for an event\n");
            helpMsg.append("- /score [eventId] [score] - Submit your score for an event\n");

            if (isAdminMap.getOrDefault(username, false)) {
                helpMsg.append("\nAdmin Commands:\n");
                helpMsg.append("- /create [title]|[description]|[exerciseType] - Create a new event\n");
                helpMsg.append("- /delete [eventId] - Delete an event\n");
                helpMsg.append("- /activate [eventId] - Activate an event\n");
                helpMsg.append("- /deactivate [eventId] - Deactivate an event\n");
            }

            sendMessageToUser(username, helpMsg.toString());
        } catch (Exception e) {
            logger.error("Error displaying help: " + e.getMessage());
            try {
                sendMessageToUser(username, "Error displaying help. Please try again.");
            } catch (IOException ex) {
                logger.error("Failed to send error message");
            }
        }
    }

    private void sendActiveEvents(String username) {
        try {
            if (eventService == null) {
                sendMessageToUser(username, "Error: Service unavailable");
                return;
            }

            StringBuilder eventsMsg = new StringBuilder("Active Events:\n");
            List<Event> events = eventService.getActiveEvents();

            if (events == null || events.isEmpty()) {
                eventsMsg.append("No active events found.");
                sendMessageToUser(username, eventsMsg.toString());
                return;
            }

            for (Event event : events) {
                if (event != null) {
                    eventsMsg.append("- ID: ").append(event.getId())
                            .append(" | ").append(event.getTitle() != null ? event.getTitle() : "Untitled")
                            .append(" | ").append(event.getExerciseType() != null ? event.getExerciseType() : "No type")
                            .append("\n");
                }
            }

            sendMessageToUser(username, eventsMsg.toString());
        } catch (Exception e) {
            logger.error("Error retrieving events: " + e.getMessage());
            try {
                sendMessageToUser(username, "Error retrieving active events. Please try again.");
            } catch (IOException ex) {
                logger.error("Failed to send error message");
            }
        }
    }

    private void handleCreateEventCommand(String username, String eventData) {
        try {
            if (!isAdminMap.getOrDefault(username, false)) {
                sendMessageToUser(username, "Error: You do not have permission to create events.");
                return;
            }

            if (eventData == null || eventData.trim().isEmpty()) {
                sendMessageToUser(username, "Error: Missing event data. Use: /create title|description|exerciseType");
                return;
            }

            String[] parts = eventData.split("\\|");
            if (parts.length != 3) {
                sendMessageToUser(username, "Error: Invalid format. Use: /create title|description|exerciseType");
                return;
            }

            String title = parts[0].trim();
            String description = parts[1].trim();
            String exerciseType = parts[2].trim();

            if (title.isEmpty() || exerciseType.isEmpty()) {
                sendMessageToUser(username, "Error: Title and exercise type cannot be empty.");
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endDate = now.plusDays(7);

            Event event = eventService.createEvent(title, description, exerciseType, username, now, endDate);

            if (event != null) {
                broadcast("New event created: " + event.getTitle() + " (ID: " + event.getId() + ")");
                sendMessageToUser(username, "Event created successfully with ID: " + event.getId());
            } else {
                sendMessageToUser(username, "Error: Failed to create event.");
            }
        } catch (Exception e) {
            logger.error("Error creating event: " + e.getMessage());
            try {
                sendMessageToUser(username, "Error creating event. Please try again.");
            } catch (IOException ex) {
                logger.error("Failed to send error message");
            }
        }
    }

    private void handleDeleteEventCommand(String username, String eventIdStr) {
        try {
            if (!isAdminMap.getOrDefault(username, false)) {
                sendMessageToUser(username, "Error: You do not have permission to delete events.");
                return;
            }

            if (eventIdStr == null || eventIdStr.trim().isEmpty()) {
                sendMessageToUser(username, "Error: Missing event ID. Use: /delete eventId");
                return;
            }

            try {
                long eventId = Long.parseLong(eventIdStr.trim());
                Event event = eventService.getEventById(eventId);

                if (event == null) {
                    sendMessageToUser(username, "Error: Event not found with ID: " + eventId);
                    return;
                }

                String eventTitle = event.getTitle();
                boolean success = eventService.deleteEvent(eventId, username);

                if (success) {
                    broadcast("Event deleted: " + eventTitle + " (ID: " + eventId + ")");
                    sendMessageToUser(username, "Event deleted successfully: " + eventId);
                } else {
                    sendMessageToUser(username, "Error: Failed to delete event.");
                }

            } catch (NumberFormatException e) {
                sendMessageToUser(username, "Error: Invalid event ID format. Please use a number.");
            }
        } catch (Exception e) {
            logger.error("Error deleting event: " + e.getMessage());
            try {
                sendMessageToUser(username, "Error deleting event. Please try again.");
            } catch (IOException ex) {
                logger.error("Failed to send error message");
            }
        }
    }

    private void handleActivateEventCommand(String username, String eventIdStr, boolean activate) {
        try {
            if (!isAdminMap.getOrDefault(username, false)) {
                sendMessageToUser(username, "Error: You do not have permission to modify events.");
                return;
            }

            if (eventIdStr == null || eventIdStr.trim().isEmpty()) {
                String cmd = activate ? "/activate" : "/deactivate";
                sendMessageToUser(username, "Error: Missing event ID. Use: " + cmd + " eventId");
                return;
            }

            try {
                long eventId = Long.parseLong(eventIdStr.trim());
                Event event = eventService.toggleEventStatus(eventId, activate, username);

                if (event != null) {
                    broadcast("Event " + (activate ? "activated" : "deactivated") + ": "
                            + event.getTitle() + " (ID: " + eventId + ")");
                    sendMessageToUser(username, "Event " + (activate ? "activated" : "deactivated")
                            + " successfully: " + eventId);
                } else {
                    sendMessageToUser(username, "Error: Failed to update event status.");
                }
            } catch (NumberFormatException e) {
                sendMessageToUser(username, "Error: Invalid event ID format. Please use a number.");
            }
        } catch (Exception e) {
            logger.error("Error changing event status: " + e.getMessage());
            try {
                sendMessageToUser(username, "Error changing event status. Please try again.");
            } catch (IOException ex) {
                logger.error("Failed to send error message");
            }
        }
    }

    private void handleLeaderboardCommand(String username, String eventIdStr) {
        try {
            if (eventIdStr == null || eventIdStr.trim().isEmpty()) {
                sendMessageToUser(username, "Error: Missing event ID. Use: /leaderboard eventId");
                return;
            }

            long eventId;
            try {
                eventId = Long.parseLong(eventIdStr.trim());
            } catch (NumberFormatException e) {
                sendMessageToUser(username, "Error: Event ID must be a number. Example: /leaderboard 1");
                return;
            }

            Event event = eventService.getEventById(eventId);
            if (event == null) {
                sendMessageToUser(username, "Error: Event not found with ID: " + eventId);
                return;
            }

            Map<String, Map<String, Object>> leaderboard = eventService.getLeaderboard(eventId);
            StringBuilder leaderboardMsg = new StringBuilder("Leaderboard for " + event.getTitle() + ":\n");

            if (leaderboard == null || leaderboard.isEmpty()) {
                leaderboardMsg.append("No participants yet.");
                sendMessageToUser(username, leaderboardMsg.toString());
                return;
            }

            for (Map.Entry<String, Map<String, Object>> entry : leaderboard.entrySet()) {
                String weightClassName = entry.getKey();
                Map<String, Object> weightClassData = entry.getValue();

                if (weightClassName == null || weightClassData == null) {
                    continue;
                }

                leaderboardMsg.append("\n\n").append(weightClassName).append(" Weight Class:\n");

                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> participants =
                        (java.util.List<Map<String, Object>>) weightClassData.get("participants");

                if (participants == null || participants.isEmpty()) {
                    leaderboardMsg.append("  No participants in this weight class.\n");
                    continue;
                }

                for (int i = 0; i < participants.size(); i++) {
                    Map<String, Object> participant = participants.get(i);
                    if (participant == null) {
                        continue;
                    }

                    leaderboardMsg.append("  ").append(i+1).append(". ");

                    Object firstName = participant.get("firstName");
                    Object lastName = participant.get("lastName");
                    Object participantUsername = participant.get("username");
                    Object score = participant.get("score");

                    if (firstName != null) {
                        leaderboardMsg.append(firstName).append(" ");
                    }

                    if (lastName != null) {
                        leaderboardMsg.append(lastName).append(" ");
                    }

                    leaderboardMsg.append("(");
                    if (participantUsername != null) {
                        leaderboardMsg.append(participantUsername);
                    } else {
                        leaderboardMsg.append("unknown");
                    }
                    leaderboardMsg.append(") - ");

                    if (score != null) {
                        leaderboardMsg.append(score).append(" pts");
                    } else {
                        leaderboardMsg.append("0 pts");
                    }

                    leaderboardMsg.append("\n");
                }
            }

            sendMessageToUser(username, leaderboardMsg.toString());
        } catch (Exception e) {
            logger.error("Error retrieving leaderboard: " + e.getMessage());
            try {
                sendMessageToUser(username, "Error retrieving leaderboard. Please try again.");
            } catch (IOException ex) {
                logger.error("Failed to send error message");
            }
        }
    }

    private void handleSubmitScoreCommand(String username, String data) {
        try {
            String[] parts = data.trim().split("\\s+");
            if (parts.length != 2) {
                sendMessageToUser(username, "Error: Invalid format. Use: /score eventId score");
                return;
            }

            long eventId;
            int score;

            try {
                eventId = Long.parseLong(parts[0].trim());
            } catch (NumberFormatException e) {
                sendMessageToUser(username, "Error: Event ID must be a number. Example: /score 1 200");
                return;
            }

            try {
                score = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                sendMessageToUser(username, "Error: Score must be a number. Example: /score 1 200");
                return;
            }

            Event event = eventService.getEventById(eventId);
            if (event == null) {
                sendMessageToUser(username, "Error: Event not found with ID: " + eventId);
                return;
            }

            if (!event.isActive()) {
                sendMessageToUser(username, "Error: This event is not active. Scores cannot be submitted.");
                return;
            }

            boolean success = eventService.submitScore(eventId, username, score);

            if (success) {
                User user = userRepository.findByUsername(username);
                WeightClass weightClass = WeightClass.getWeightClassForWeight(user.getWeight());

                sendMessageToUser(username, "Score submitted successfully!");

                broadcast(username + " submitted a score of " + score + " for event '" +
                        event.getTitle() + "' in the " + weightClass.getName() + " weight class!");

                broadcastLeaderboard(eventId);
            } else {
                sendMessageToUser(username, "Error: Failed to submit score.");
            }
        } catch (Exception e) {
            logger.error("Error submitting score: " + e.getMessage());
            try {
                sendMessageToUser(username, "Error submitting score. Please try again.");
            } catch (IOException ex) {
                logger.error("Failed to send error message");
            }
        }
    }

    private void sendTextSafely(Session session, String message) {
        if (session == null || !session.isOpen()) {
            return;
        }

        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            logger.error("Failed to send message: " + e.getMessage());
        }
    }

    private void closeSafely(Session session) {
        if (session == null) {
            return;
        }

        try {
            session.close();
        } catch (IOException e) {
            logger.error("Failed to close session: " + e.getMessage());
        }
    }

    private void sendMessageToUser(String username, String message) throws IOException {
        if (username == null || message == null) {
            return;
        }

        String timestamp = DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());
        String formattedMessage = "[" + timestamp + "] " + message;

        Session session = usernameSessionMap.get(username);
        if (session != null && session.isOpen()) {
            session.getBasicRemote().sendText(formattedMessage);
        }
    }

    private void broadcast(String message) {
        if (message == null) {
            return;
        }

        String timestamp = DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());
        String formattedMessage = "[" + timestamp + "] " + message;

        Map<Session, String> sessionsCopy = new HashMap<>(sessionUsernameMap);

        sessionsCopy.forEach((session, username) -> {
            try {
                if (session != null && session.isOpen()) {
                    session.getBasicRemote().sendText(formattedMessage);
                }
            } catch (IOException e) {
                logger.error("Broadcast error for user " + username);
            }
        });
    }

    private void broadcastLeaderboard(long eventId) {
        try {
            Event event = eventService.getEventById(eventId);
            if (event == null) return;

            Map<String, Map<String, Object>> leaderboard = eventService.getLeaderboard(eventId);
            if (leaderboard == null || leaderboard.isEmpty()) return;

            StringBuilder leaderboardMsg = new StringBuilder("Updated Leaderboard for " + event.getTitle() + ":\n");
            boolean hasEntries = false;

            for (Map.Entry<String, Map<String, Object>> entry : leaderboard.entrySet()) {
                String weightClassName = entry.getKey();
                if (weightClassName == null) continue;

                Map<String, Object> weightClassData = entry.getValue();
                if (weightClassData == null) continue;

                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> participants =
                        (java.util.List<Map<String, Object>>) weightClassData.get("participants");

                if (participants == null || participants.isEmpty()) {
                    continue;
                }

                leaderboardMsg.append("\r\n").append(weightClassName).append(" Top 3:\n");
                hasEntries = true;

                int count = 0;
                for (Map<String, Object> participant : participants) {
                    if (participant == null) continue;

                    if (count >= 3) break;  // Only show top 3

                    Object username = participant.get("username");
                    Object score = participant.get("score");

                    if (username != null && score != null) {
                        leaderboardMsg.append("  ").append(count + 1).append(". ")
                                .append(username).append(" - ")
                                .append(score).append(" pts\n");
                        count++;
                    }
                }
            }

            if (hasEntries) {
                broadcast(leaderboardMsg.toString());
            }
        } catch (Exception e) {
            logger.error("Error broadcasting leaderboard: " + e.getMessage());
        }
    }
}