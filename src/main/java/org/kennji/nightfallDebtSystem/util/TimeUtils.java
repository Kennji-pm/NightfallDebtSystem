package org.kennji.nightfallDebtSystem.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for time formatting and manipulation
 */
public class TimeUtils {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
    private static final SimpleDateFormat HMS_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private static final SimpleDateFormat DUE_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    
    /**
     * Format timestamp to Vietnamese date time format
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted date string
     */
    public static String formatDateTime(long timestamp) {
        return DATE_FORMAT.format(new Date(timestamp));
    }
    
    /**
     * Format timestamp to just time (HMS) format
     * @param timestamp Unix timestamp in milliseconds  
     * @return Formatted time string
     */
    public static String formatTime(long timestamp) {
        return HMS_FORMAT.format(new Date(timestamp));
    }
    
    /**
     * Format timestamp to due date format (no time)
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted date string
     */
    public static String formatDueDate(long timestamp) {
        return DUE_DATE_FORMAT.format(new Date(timestamp));
    }
    
    /**
     * Get remaining time from now to due date in readable format
     * @param dueDateTimestamp Unix timestamp of due date
     * @return Remaining time string
     */
    public static String getRemainingTime(long dueDateTimestamp) {
        long now = System.currentTimeMillis();
        long remaining = dueDateTimestamp - now;
        
        if (remaining <= 0) {
            return "Đã quá hạn";
        }
        
        long days = remaining / (24 * 60 * 60 * 1000);
        long hours = (remaining % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        long minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000);
        
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" ngày ");
        }
        if (hours > 0) {
            sb.append(hours).append(" giờ ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(" phút");
        }
        
        if (sb.length() == 0) {
            return "Ít hơn 1 phút";
        }
        
        return sb.toString().trim();
    }
    
    /**
     * Check if a debt is overdue
     * @param dueDateTimestamp Unix timestamp of due date
     * @return true if overdue, false otherwise
     */
    public static boolean isOverdue(long dueDateTimestamp) {
        return System.currentTimeMillis() > dueDateTimestamp;
    }
}
