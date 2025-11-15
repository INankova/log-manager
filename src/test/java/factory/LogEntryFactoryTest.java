package factory;

import org.example.factory.LogEntryFactory;
import org.example.model.LogEntry;
import org.example.model.LogType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class LogEntryFactoryTest {

    @Test
    void testCreateLogEntry() {
        LogEntryFactory factory = new LogEntryFactory();

        Instant before = Instant.now();
        LogEntry entry = factory.create(LogType.INFO, "Test message", "UnitTest");
        Instant after = Instant.now();

        assertNotNull(entry);
        assertEquals(LogType.INFO, entry.type());
        assertEquals("Test message", entry.message());
        assertEquals("UnitTest", entry.source());
        assertTrue(!entry.timestamp().isBefore(before) &&
                        !entry.timestamp().isAfter(after),
                "Timestamp should be generated between before and after instants");
    }
}

