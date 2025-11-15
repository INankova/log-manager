package org.example.service;

import org.example.factory.LogEntryFactory;
import org.example.model.LogEntry;
import org.example.model.LogType;
import org.example.parser.AbstractLogParser;
import org.example.storage.LogRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class LogService {
    private final LogRepository repo;
    private final LogEntryFactory factory = new LogEntryFactory();
    private final AbstractLogParser parser;


    public LogService(LogRepository repo, AbstractLogParser parser) {
        this.repo = repo;
        this.parser = parser;
    }


    public void log(LogType type, String message, String source) {
        repo.append(factory.create(type, message, source));
    }


    public List<LogEntry> getByType(LogType type) {
        return repo.byType(type);
    }


    public void ingestFile(Path path) throws IOException {
        for (LogEntry e : parser.parseFile(path)) {
            repo.append(e);
        }
    }
}
