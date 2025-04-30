package com.havenwatch.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date and time operations
 */
public class DateTimeUtils {
    // Date and time formatters for consistent display
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");

    private static final DateTimeFormatter SHORT_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("MM/dd/yy HH:mm");

    /**
     * Format a date for display
     * @param date The date to format
     * @return Formatted date string
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * Format a date-time for display
     * @param dateTime The date-time to format
     * @return Formatted date-time string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Format a date-time with a short format
     * @param dateTime The date-time to format
     * @return Formatted short date-time string
     */
    public static String formatShortDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(SHORT_DATE_TIME_FORMATTER);
    }

    /**
     * Format just the time portion of a date-time
     * @param dateTime The date-time to format
     * @return Formatted time string
     */
    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(TIME_FORMATTER);
    }

    /**
     * Calculate age from birth date
     * @param birthDate The birth date
     * @return Age in years
     */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    }

    /**
     * Format a relative time (e.g., "5 minutes ago")
     * @param dateTime The date-time to format
     * @return Relative time string
     */
    public static String formatRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        long seconds = ChronoUnit.SECONDS.between(dateTime, now);

        if (seconds < 60) {
            return "just now";
        }

        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        if (minutes < 60) {
            return minutes + " minute" + (minutes != 1 ? "s" : "") + " ago";
        }

        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours < 24) {
            return hours + " hour" + (hours != 1 ? "s" : "") + " ago";
        }

        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (days < 7) {
            return days + " day" + (days != 1 ? "s" : "") + " ago";
        }

        // If more than a week, just show the regular date/time
        return formatDateTime(dateTime);
    }

    /**
     * Parse a date string in the standard format
     * @param dateStr The date string to parse
     * @return LocalDate object
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            System.err.println("Error parsing date: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parse a date-time string in the standard format
     * @param dateTimeStr The date-time string to parse
     * @return LocalDateTime object
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            System.err.println("Error parsing date-time: " + e.getMessage());
            return null;
        }
    }
}