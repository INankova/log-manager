package org.example.model;

import java.time.Instant;
import java.util.Objects;

public record LogEntry(Instant timestamp, LogType type, String message, String source) {
    public LogEntry(Instant timestamp, LogType type, String message, String source) {
        this.timestamp = Objects.requireNonNull(timestamp);
        this.type = Objects.requireNonNull(type);
        this.message = Objects.requireNonNullElse(message, "");
        this.source = Objects.requireNonNullElse(source, "app");
    }


    @Override
    public String toString() {
        return timestamp + " [" + type + "] (" + source + ") " + message;
    }
}