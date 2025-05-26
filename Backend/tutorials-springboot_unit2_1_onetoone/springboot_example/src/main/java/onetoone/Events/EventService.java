package onetoone.Events;

import jakarta.transaction.Transactional;
import onetoone.users.User;
import onetoone.users.UserRepository;
import onetoone.users.WeightClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final Logger logger = LoggerFactory.getLogger(EventService.class);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    // Create a new event (admin only)
    public Event createEvent(String title, String description, String exerciseType,
                             String adminUsername, LocalDateTime startDate, LocalDateTime endDate) {
        User admin = userRepository.findByUsername(adminUsername);

        if (admin == null || !"Admin".equals(admin.getUserRole())) {
            return null; // Only admins can create events
        }

        Event event = new Event(title, description, exerciseType, admin, startDate, endDate);
        return eventRepository.save(event);
    }

    // Update an event (admin only)
    public Event updateEvent(long eventId, String title, String description,
                             String exerciseType, LocalDateTime startDate,
                             LocalDateTime endDate, String adminUsername) {
        User admin = userRepository.findByUsername(adminUsername);
        Event event = eventRepository.findById(eventId);

        if (admin == null || event == null || !"Admin".equals(admin.getUserRole())) {
            return null;
        }

        if (title != null) event.setTitle(title);
        if (description != null) event.setDescription(description);
        if (exerciseType != null) event.setExerciseType(exerciseType);
        if (startDate != null) event.setStartDate(startDate);
        if (endDate != null) event.setEndDate(endDate);

        return eventRepository.save(event);
    }

    public Event toggleEventStatus(long eventId, boolean active, String adminUsername) {
        User admin = userRepository.findByUsername(adminUsername);
        Event event = eventRepository.findById(eventId);

        if (admin == null || event == null || !"Admin".equals(admin.getUserRole())) {
            return null;
        }

        // Print current status
        System.out.println("Event " + eventId + " status before change: " + event.isActive());

        // Set the new status
        event.setActive(active);

        // Print the updated status
        System.out.println("Event " + eventId + " status after change: " + event.isActive());

        // Save and return
        Event savedEvent = eventRepository.save(event);

        // Verify saved status
        System.out.println("Event " + eventId + " status after save: " +
                (savedEvent != null ? savedEvent.isActive() : "null"));

        return savedEvent;
    }

    // Delete an event (admin only)
    public boolean deleteEvent(long eventId, String adminUsername) {
        User admin = userRepository.findByUsername(adminUsername);
        Event event = eventRepository.findById(eventId);

        if (admin == null || event == null || !"Admin".equals(admin.getUserRole())) {
            return false;
        }

        eventRepository.delete(event);
        return true;
    }

    // Submit a score for an event
    @Transactional
    public boolean submitScore(long eventId, String username, int score) {
        try {
            // Log the attempt
            logger.info("Attempting to submit score: eventId={}, username={}, score={}",
                    eventId, username, score);

            User user = userRepository.findByUsername(username);
            if (user == null) {
                logger.error("User not found: {}", username);
                return false;
            }
            logger.info("User found: id={}", user.getId());

            Event event = eventRepository.findById(eventId);
            if (event == null) {
                logger.error("Event not found: {}", eventId);
                return false;
            }
            logger.info("Event found: title={}, active={}", event.getTitle(), event.isActive());

            if (!event.isActive()) {
                logger.error("Event is not active: {}", eventId);
                return false;
            }

            // Submit score
            event.updateParticipantScore(user, score);

            // Save changes
            logger.info("Saving event with updated score");
            Event savedEvent = eventRepository.save(event);

            logger.info("Score submitted successfully");
            return savedEvent != null;
        } catch (Exception e) {
            logger.error("Error submitting score: {}", e.getMessage(), e);
            return false;
        }
    }

    // Get leaderboard for an event
    public Map<String, Map<String, Object>> getLeaderboard(long eventId) {
        Event event = eventRepository.findById(eventId);
        if (event == null) {
            return new HashMap<>();
        }

        Map<String, Map<String, Object>> formattedLeaderboard = new HashMap<>();

        // Get leaderboard by weight class
        Map<WeightClass, Map<User, Integer>> leaderboard = event.getLeaderboardByWeightClass();

        // Format leaderboard for frontend
        for (Map.Entry<WeightClass, Map<User, Integer>> entry : leaderboard.entrySet()) {
            WeightClass weightClass = entry.getKey();
            Map<User, Integer> classLeaderboard = entry.getValue();

            // Sort users by score (descending)
            List<Map.Entry<User, Integer>> sortedEntries = classLeaderboard.entrySet().stream()
                    .sorted(Map.Entry.<User, Integer>comparingByValue().reversed())
                    .collect(Collectors.toList());

            // Format entries for this weight class
            Map<String, Object> classData = new HashMap<>();
            classData.put("weightClassName", weightClass.getName());
            classData.put("weightClassRange", weightClass.toString());

            // Format participant data
            List<Map<String, Object>> participants = sortedEntries.stream()
                    .map(userEntry -> {
                        User user = userEntry.getKey();
                        Integer userScore = userEntry.getValue();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("username", user.getUsername());
                        userData.put("firstName", user.getFirstName());
                        userData.put("lastName", user.getLastName());
                        userData.put("weight", user.getWeight());
                        userData.put("score", userScore);

                        return userData;
                    })
                    .collect(Collectors.toList());

            classData.put("participants", participants);
            formattedLeaderboard.put(weightClass.getName(), classData);
        }

        return formattedLeaderboard;
    }

    // Get all active events
    public List<Event> getActiveEvents() {
        return eventRepository.findByActive(true);
    }

    // Get event by ID
    public Event getEventById(long eventId) {
        return eventRepository.findById(eventId);
    }

    public EventRepository getEventRepository() {
        return eventRepository;
    }
}