package com.branegy.util;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class DateTimeUtils {
    
    private DateTimeUtils() {
    }

    public static String dateShort(Date date) {
        return DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).format(date);
    }
    
    public static String dateMediumt(Date date) {
        return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US).format(date);
    }
    
    public static String dateLong(Date date) {
        return DateFormat.getDateInstance(DateFormat.LONG, Locale.US).format(date);
    }
    
    public static String dateTimeShort(Date date) {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US).format(date);
    }
    
    public static String dateTimeMediumt(Date date) {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US).format(date);
    }
    
    public static String dateTimeLong(Date date) {
        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.US).format(date);
    }
    
    public static String timeShort(Date date) {
        return DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date);
    }
    
    public static String timeMediumt(Date date) {
        return DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.US).format(date);
    }
    
    public static String timeLong(Date date) {
        return DateFormat.getTimeInstance(DateFormat.LONG, Locale.US).format(date);
    }
}
