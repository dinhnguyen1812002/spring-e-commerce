package com.app.e_commerce.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class TimeAgoUtil {
    
    public static String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime zdtNow = now.atZone(ZoneId.systemDefault());
        ZonedDateTime zdtDateTime = dateTime.atZone(ZoneId.systemDefault());
        
        long seconds = ChronoUnit.SECONDS.between(zdtDateTime, zdtNow);
        long minutes = ChronoUnit.MINUTES.between(zdtDateTime, zdtNow);
        long hours = ChronoUnit.HOURS.between(zdtDateTime, zdtNow);
        long days = ChronoUnit.DAYS.between(zdtDateTime, zdtNow);
        long months = ChronoUnit.MONTHS.between(zdtDateTime, zdtNow);
        long years = ChronoUnit.YEARS.between(zdtDateTime, zdtNow);
        
        if (years > 0) {
            return years + (years == 1 ? " year ago" : " years ago");
        } else if (months > 0) {
            return months + (months == 1 ? " month ago" : " months ago");
        } else if (days > 0) {
            return days + (days == 1 ? " day ago" : " days ago");
        } else if (hours > 0) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (minutes > 0) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (seconds > 10) {
            return seconds + (seconds == 1 ? " second ago" : " seconds ago");
        } else {
            return "just now";
        }
    }

}
