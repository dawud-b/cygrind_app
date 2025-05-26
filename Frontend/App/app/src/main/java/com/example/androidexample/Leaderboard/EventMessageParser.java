package com.example.androidexample.Leaderboard;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * A utility class responsible for parsing messages related to events, including timestamps,
 * event listings, and leaderboard data.
 *
 * This class includes methods to extract relevant data from event-related messages
 * and organize it into meaningful objects such as EventSummary and LeaderboardEntry.
 */
public class EventMessageParser {
    private static final String TAG = "EventMessageParser";  // Tag for logging

    /**
     * Parses the timestamp from a given message.
     *
     * @param message The message containing a timestamp in the format [HH:mm:ss].
     * @return The parsed timestamp (HH:mm:ss) if found, otherwise returns null.
     */
    public static String parseTimestamp(String message) {
        // Looking for timestamp format like [HH:mm:ss]
        Pattern pattern = Pattern.compile("\\[(\\d{2}:\\d{2}:\\d{2})\\]");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Parses an event listing message and extracts event summaries.
     *
     * @param message The message containing event listing data.
     * @return A list of EventSummary objects representing the active events.
     */
    public static List<EventSummary> parseEventListing(String message) {
        List<EventSummary> events = new ArrayList<>();

        // Format: "Active Events:\n- ID: 1 | Event Title | Exercise Type\n- ID: 2..."

        if (!message.contains("Active Events:")) {
            return events;
        }

        String[] lines = message.split("\n");
        for (String line : lines) {
            if (line.startsWith("- ID:")) {
                try {
                    // Extract event ID
                    Pattern idPattern = Pattern.compile("ID:\\s*(\\d+)");
                    Matcher idMatcher = idPattern.matcher(line);
                    if (!idMatcher.find()) continue;
                    long id = Long.parseLong(idMatcher.group(1));

                    // Extract title
                    Pattern titlePattern = Pattern.compile("\\|\\s*([^|]+)\\s*\\|");
                    Matcher titleMatcher = titlePattern.matcher(line);
                    if (!titleMatcher.find()) continue;
                    String title = titleMatcher.group(1).trim();

                    // Extract exercise type
                    Pattern typePattern = Pattern.compile("\\|\\s*([^|]+)$");
                    Matcher typeMatcher = typePattern.matcher(line);
                    String exerciseType = "";
                    if (typeMatcher.find()) {
                        exerciseType = typeMatcher.group(1).trim();
                    }

                    events.add(new EventSummary(id, title, exerciseType));
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing event line: " + line, e);
                }
            }
        }

        return events;
    }

    /**
     * Parses a leaderboard message and organizes it by weight class.
     *
     * @param message The message containing leaderboard data.
     * @return A map where the keys are weight class names and the values are lists of LeaderboardEntry objects.
     */
    public static Map<String, List<LeaderboardEntry>> parseLeaderboard(String message) {
        Map<String, List<LeaderboardEntry>> leaderboardByWeightClass = new HashMap<>();

        // Check if this is a leaderboard message
        if (!message.contains("Leaderboard for")) {
            return leaderboardByWeightClass;
        }

        String currentWeightClass = null;
        List<LeaderboardEntry> currentEntries = null;

        String[] lines = message.split("\n");
        for (String line : lines) {
            line = line.trim();

            // Check for weight class header
            if (line.endsWith("Weight Class:")) {
                currentWeightClass = line.replace("Weight Class:", "").trim();
                currentEntries = new ArrayList<>();
                leaderboardByWeightClass.put(currentWeightClass, currentEntries);
                continue;
            }

            // Parse participant entry (format: "  1. John Doe (username) - 250 pts")
            if (line.matches("\\s*\\d+\\..*") && currentEntries != null) {
                try {
                    // Extract rank (not used in our model but useful for debugging)
                    Pattern rankPattern = Pattern.compile("(\\d+)\\.");
                    Matcher rankMatcher = rankPattern.matcher(line);
                    if (!rankMatcher.find()) continue;

                    // Extract name and username
                    Pattern namePattern = Pattern.compile("\\d+\\.\\s*([^(]+)\\(([^)]+)\\)\\s*-\\s*(\\d+)\\s*pts");
                    Matcher nameMatcher = namePattern.matcher(line);
                    if (!nameMatcher.find()) continue;

                    String fullName = nameMatcher.group(1).trim();
                    String username = nameMatcher.group(2).trim();
                    int score = Integer.parseInt(nameMatcher.group(3).trim());

                    // Split full name into first and last name (if possible)
                    String firstName = fullName;
                    String lastName = "";
                    String[] nameParts = fullName.split("\\s+", 2);
                    if (nameParts.length == 2) {
                        firstName = nameParts[0];
                        lastName = nameParts[1];
                    }

                    currentEntries.add(new LeaderboardEntry(username, firstName, lastName, currentWeightClass, score));
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing leaderboard entry: " + line, e);
                }
            }
        }

        return leaderboardByWeightClass;
    }

    /**
     * A helper class to represent an event summary, containing details such as event ID,
     * title, and exercise type.
     */
    public static class EventSummary {
        private long id;
        private String title;
        private String exerciseType;

        /**
         * Constructs an EventSummary object with the provided details.
         *
         * @param id The event ID.
         * @param title The event title.
         * @param exerciseType The type of exercise for the event.
         */
        public EventSummary(long id, String title, String exerciseType) {
            this.id = id;
            this.title = title;
            this.exerciseType = exerciseType;
        }

        /**
         * Returns the event ID.
         *
         * @return The event ID.
         */
        public long getId() {
            return id;
        }

        /**
         * Returns the event title.
         *
         * @return The event title.
         */
        public String getTitle() {
            return title;
        }

        /**
         * Returns the exercise type for the event.
         *
         * @return The exercise type.
         */
        public String getExerciseType() {
            return exerciseType;
        }
    }
}
