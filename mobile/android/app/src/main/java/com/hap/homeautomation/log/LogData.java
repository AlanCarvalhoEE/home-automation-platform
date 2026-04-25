package com.hap.homeautomation.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Class responsible for storing log data
public class LogData {

    public long id;
    public long timestamp;
    public String type;
    public String message;

    public LogData() {}

    public LogData(long id, long timestamp, String type, String message) {

        this.id = id;
        this.timestamp = timestamp;
        this.type = type;
        this.message = message;
    }

    // Method to get the log ID
    public long getId() { return id; }

    // Method to get the log timestamp
    public long getTimestamp() { return timestamp; }

    // Method to get the log type
    public String getType() { return type; }

    // Method to get the log message
    public String getMessage() { return message; }

    // Method to get the time of the day
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}

