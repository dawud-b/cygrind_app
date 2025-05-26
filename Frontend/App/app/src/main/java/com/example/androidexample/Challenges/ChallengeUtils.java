package com.example.androidexample.Challenges;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for date formatting in the Challenges feature
 */
public class ChallengeUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    /**
     * Format a date object to a readable string
     * @param date Date to format
     * @return Formatted date string
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "Not completed";
        }
        return DATE_FORMAT.format(date);
    }

    /**
     * Format a progress percentage
     * @param progress Progress as a decimal (0.0 to 1.0)
     * @return Formatted progress percentage string
     */
    public static String formatProgress(double progress) {
        return String.format(Locale.US, "%.0f%%", progress * 100);
    }
}