package org.example.factory;

import org.example.model.LogEntry;
import org.example.model.LogType;

import java.time.Instant;

public class LogEntryFactory {

    public LogEntry create(LogType type, String message, String source) {
        return new LogEntry(Instant.now(), type, message, source);
    }
}
