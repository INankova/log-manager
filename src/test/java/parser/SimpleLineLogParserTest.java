package parser;

import org.example.model.LogEntry;
import org.example.model.LogType;
import org.example.parser.SimpleLineLogParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimpleLineLogParserTest {

    private final SimpleLineLogParser parser = new SimpleLineLogParser();

    @Test
    void parseLine_ShouldParseValidLine() {
        String line = "2024-01-01T10:00:00Z [ERROR] (system) Something broke";

        LogEntry entry = parser.parseLine(line);

        assertNotNull(entry);
        assertEquals(Instant.parse("2024-01-01T10:00:00Z"), entry.timestamp());
        assertEquals(LogType.ERROR, entry.type());
        assertEquals("system", entry.source());
        assertEquals("Something broke", entry.message());
    }

    @Test
    void parseLine_ShouldReturnNull_WhenFormatDoesNotMatch() {
        assertNull(parser.parseLine("This is not a log line"));
        assertNull(parser.parseLine("2024-01-01 [INFO] missing parts"));
        assertNull(parser.parseLine("2024-01-01T10:00:00Z (source) no type"));
    }

    @Test
    void parseFile_ShouldParseOnlyValidLines(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("logs.txt");

        List<String> lines = List.of(
                "2024-05-05T12:00:00Z [INFO] (app) Started ok",
                "bad line here", // трябва да се пропусне
                "2024-05-05T13:00:00Z [WARN] (core) Something odd"
        );

        Files.write(file, lines);

        List<LogEntry> entries = parser.parseFile(file);

        assertEquals(2, entries.size());

        assertEquals("Started ok", entries.get(0).message());
        assertEquals(LogType.INFO, entries.get(0).type());

        assertEquals("Something odd", entries.get(1).message());
        assertEquals(LogType.WARNING, entries.get(1).type());
    }
}
