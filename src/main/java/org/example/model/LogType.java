package org.example.model;

public enum LogType {
    INFO, WARNING, ERROR, DEBUG;


    public static LogType fromTag(String tag) {
        return switch (tag.toUpperCase()) {
            case "WARN", "WARNING" -> WARNING;
            case "ERR", "ERROR" -> ERROR;
            case "DBG", "DEBUG" -> DEBUG;
            default -> INFO;
        };
    }
}