package onetoone.Events;

import onetoone.users.User;
import onetoone.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Event Controller", description = "REST APIs related to Event Entity")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    // REST endpoints only

    @Operation(summary = "Get all active events", description = "Retrieve a list of all currently active events.")
    @GetMapping("/api/events")
    public List<Event> getAllActiveEvents() {
        return eventService.getActiveEvents();
    }

    @Operation(summary = "Get event by ID", description = "Retrieve a specific event using its unique identifier.")
    @GetMapping("/api/events/{eventId}")
    public ResponseEntity<Event> getEventById(@PathVariable long eventId) {
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(event);
    }

    @Operation(summary = "Get event leaderboard", description = "Retrieve the current leaderboard for a specific event.")
    @GetMapping("/api/events/{eventId}/leaderboard")
    public ResponseEntity<Map<String, Map<String, Object>>> getLeaderboard(@PathVariable long eventId) {
        Map<String, Map<String, Object>> leaderboard = eventService.getLeaderboard(eventId);
        return ResponseEntity.ok(leaderboard);
    }

    @Operation(summary = "Create new event", description = "Create a new event in the system (admin access only).")
    @PostMapping("/api/events")
    public ResponseEntity<Event> createEvent(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String exerciseType,
            @RequestParam String adminUsername,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        User admin = userRepository.findByUsername(adminUsername);
        if (admin == null || !"Admin".equals(admin.getUserRole())) {
            return ResponseEntity.badRequest().build();
        }

        Event event = eventService.createEvent(title, description, exerciseType,
                adminUsername, startDate, endDate);
        if (event == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(event);
    }

    @Operation(summary = "Update event", description = "Update an existing event's details (admin access only).")
    @PutMapping("/api/events/{eventId}")
    public ResponseEntity<Event> updateEvent(
            @PathVariable long eventId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String exerciseType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam String adminUsername) {

        Event event = eventService.updateEvent(eventId, title, description, exerciseType,
                startDate, endDate, adminUsername);

        if (event == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(event);
    }

    @Operation(summary = "Toggle event status", description = "Activate or deactivate an event (admin access only).")
    @PutMapping("/api/events/{eventId}/status")
    public ResponseEntity<Event> toggleEventStatus(
            @PathVariable long eventId,
            @RequestParam boolean active,
            @RequestParam String adminUsername) {

        Event event = eventService.toggleEventStatus(eventId, active, adminUsername);

        if (event == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(event);
    }

    @Operation(summary = "Delete event", description = "Remove an event from the system (admin access only).")
    @DeleteMapping("/api/events/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable long eventId,
            @RequestParam String adminUsername) {

        boolean success = eventService.deleteEvent(eventId, adminUsername);

        if (!success) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Submit event score", description = "Submit a user's score for a specific event.")
    @PostMapping("/api/events/{eventId}/scores")
    public ResponseEntity<Void> submitScore(
            @PathVariable long eventId,
            @RequestParam String username,
            @RequestParam int score) {

        boolean success = eventService.submitScore(eventId, username, score);

        if (!success) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }
}