package parser;

import org.example.model.LogEntry;
import org.example.model.LogType;
import org.example.parser.AbstractLogParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AbstractLogParserTest {

    static class TestLogParser extends AbstractLogParser {
        @Override
        protected LogEntry parseLine(String line) {

            if (line.equals("BAD")) {
                throw new IllegalArgumentException("Cannot parse line");
            }
            return new LogEntry(
                    Instant.parse("2024-01-01T00:00:00Z"),
                    LogType.INFO,
                    line,
                    "test-parser"
            );
        }
    }

    @TempDir
    Path tempDir;

    @Test
    void parseFile_ShouldReturnParsedEntriesAndSkipInvalidLines() throws IOException {
        Path file = tempDir.resolve("log.txt");
        List<String> lines = List.of(
                "first line",
                "BAD",
                "second line"
        );
        Files.write(file, lines);

        AbstractLogParser parser = new TestLogParser();

        List<LogEntry> result = parser.parseFile(file);

        assertEquals(2, result.size());

        assertEquals("first line", result.get(0).message());
        assertEquals("second line", result.get(1).message());
        assertEquals(LogType.INFO, result.get(0).type());
        assertEquals("test-parser", result.get(0).source());
    }

    @Test
    void parseFile_EmptyFile_ShouldReturnEmptyList() throws IOException {
        Path file = tempDir.resolve("empty.txt");
        Files.write(file, List.of());  // празен файл

        AbstractLogParser parser = new TestLogParser();

        List<LogEntry> result = parser.parseFile(file);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void parseFile_NonExistingFile_ShouldThrowIOException() {
        Path nonExisting = tempDir.resolve("no-such-file.log");
        AbstractLogParser parser = new TestLogParser();

        assertThrows(IOException.class, () -> parser.parseFile(nonExisting));
    }
}

