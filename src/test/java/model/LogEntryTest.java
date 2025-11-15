package model;

import org.example.model.LogEntry;
import org.example.model.LogType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class LogEntryTest {

    @Test
    void constructor_ShouldAssignFieldsCorrectly() {
        Instant now = Instant.now();
        LogEntry entry = new LogEntry(now, LogType.INFO, "Test message", "tester");

        assertEquals(now, entry.timestamp());
        assertEquals(LogType.INFO, entry.type());
        assertEquals("Test message", entry.message());
        assertEquals("tester", entry.source());
    }

    @Test
    void constructor_ShouldReplaceNullMessageWithEmptyString() {
        Instant now = Instant.now();
        LogEntry entry = new LogEntry(now, LogType.WARNING, null, "src");

        assertEquals("", entry.message());
    }

    @Test
    void constructor_ShouldReplaceNullSourceWithDefaultApp() {
        Instant now = Instant.now();
        LogEntry entry = new LogEntry(now, LogType.ERROR, "msg", null);

        assertEquals("app", entry.source());
    }

    @Test
    void constructor_ShouldThrowWhenTimestampIsNull() {
        assertThrows(NullPointerException.class,
                () -> new LogEntry(null, LogType.INFO, "msg", "src"));
    }

    @Test
    void constructor_ShouldThrowWhenTypeIsNull() {
        assertThrows(NullPointerException.class,
                () -> new LogEntry(Instant.now(), null, "msg", "src"));
    }

    @Test
    void toString_ShouldContainCorrectFormat() {
        Instant now = Instant.parse("2024-01-01T10:00:00Z");
        LogEntry entry = new LogEntry(now, LogType.DEBUG, "hello", "system");

        String expected = "2024-01-01T10:00:00Z [DEBUG] (system) hello";

        assertEquals(expected, entry.toString());
    }
}
