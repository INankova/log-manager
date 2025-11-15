package service;

import org.example.model.LogEntry;
import org.example.model.LogType;
import org.example.parser.AbstractLogParser;
import org.example.service.LogService;
import org.example.storage.LogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogServiceTest {

    @Test
    void log_ShouldCreateEntryAndAppendToRepository() {
        LogRepository repo = mock(LogRepository.class);
        AbstractLogParser parser = mock(AbstractLogParser.class);

        LogService service = new LogService(repo, parser);

        service.log(LogType.ERROR, "Something failed", "unit-test");

        ArgumentCaptor<LogEntry> captor = ArgumentCaptor.forClass(LogEntry.class);
        verify(repo).append(captor.capture());

        LogEntry logged = captor.getValue();
        assertNotNull(logged);
        assertEquals(LogType.ERROR, logged.type());
        assertEquals("Something failed", logged.message());
        assertEquals("unit-test", logged.source());
        assertNotNull(logged.timestamp());
    }

    @Test
    void getByType_ShouldReturnRepositoryResult() {
        LogRepository repo = mock(LogRepository.class);
        AbstractLogParser parser = mock(AbstractLogParser.class);
        LogService service = new LogService(repo, parser);

        List<LogEntry> expected = List.of(
                new LogEntry(Instant.parse("2024-01-01T00:00:00Z"), LogType.INFO, "msg1", "src1"),
                new LogEntry(Instant.parse("2024-01-01T01:00:00Z"), LogType.INFO, "msg2", "src2")
        );

        when(repo.byType(LogType.INFO)).thenReturn(expected);

        List<LogEntry> result = service.getByType(LogType.INFO);

        assertSame(expected, result);
        verify(repo).byType(LogType.INFO);
        verifyNoMoreInteractions(repo);
    }

    @Test
    void ingestFile_ShouldParseFileAndAppendAllEntries() throws IOException {
        LogRepository repo = mock(LogRepository.class);
        AbstractLogParser parser = mock(AbstractLogParser.class);
        LogService service = new LogService(repo, parser);

        Path path = Path.of("dummy.log");

        LogEntry e1 = new LogEntry(Instant.parse("2024-01-01T00:00:00Z"), LogType.INFO, "first", "src");
        LogEntry e2 = new LogEntry(Instant.parse("2024-01-01T01:00:00Z"), LogType.ERROR, "second", "src");

        when(parser.parseFile(path)).thenReturn(List.of(e1, e2));

        service.ingestFile(path);

        verify(parser).parseFile(path);
        verify(repo).append(e1);
        verify(repo).append(e2);
        verifyNoMoreInteractions(repo, parser);
    }
}

