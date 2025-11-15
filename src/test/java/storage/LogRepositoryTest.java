package storage;

import org.example.model.LogEntry;
import org.example.model.LogType;
import org.example.storage.LogRepository;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogRepositoryTest {

    private LogRepository repo;
    private Path tempDir;

    @BeforeEach
    void resetSingleton() throws Exception {
        Field f = LogRepository.class.getDeclaredField("INSTANCE");
        f.setAccessible(true);
        f.set(null, null);
    }

    @BeforeEach
    void setup() throws Exception {
        // наша temp директория, JUnit НЕ я трие автоматично
        tempDir = Files.createTempDirectory("log-repo-test-");
        repo = LogRepository.getInstance(tempDir);
    }

    @AfterEach
    void cleanup() {
        repo.shutdown(); // не чакаме, но директорията си остава, така че няма NoSuchFileException
    }

    private LogEntry entry(LogType type, String msg) {
        return new LogEntry(
                Instant.parse("2024-01-01T10:00:00Z"),
                type,
                msg,
                "test"
        );
    }

    @Test
    void append_ShouldAddEntryToMemory() {
        LogEntry e = entry(LogType.INFO, "Hello");

        repo.append(e);

        List<LogEntry> all = repo.all();
        assertEquals(1, all.size());
        assertEquals(e, all.get(0));
    }

    @Test
    void byType_ShouldReturnOnlyMatchingEntries() {
        LogEntry e1 = entry(LogType.INFO, "A");
        LogEntry e2 = entry(LogType.ERROR, "B");
        LogEntry e3 = entry(LogType.INFO, "C");

        repo.append(e1);
        repo.append(e2);
        repo.append(e3);

        List<LogEntry> infos = repo.byType(LogType.INFO);

        assertEquals(2, infos.size());
        assertEquals("A", infos.get(0).message());
        assertEquals("C", infos.get(1).message());
    }

    @Test
    void append_ShouldWriteToCorrectFile() throws Exception {
        LogEntry e = entry(LogType.WARNING, "Disk almost full");
        repo.append(e);

        // writer-ът е асинхронен → малко изчакване
        Thread.sleep(200);

        Path file = tempDir.resolve("warning.log");
        assertTrue(Files.exists(file));

        String content = Files.readString(file);

        assertTrue(content.contains("2024-01-01T10:00:00Z"));
        assertTrue(content.contains("[WARNING]"));
        assertTrue(content.contains("(test) Disk almost full"));
    }
}


