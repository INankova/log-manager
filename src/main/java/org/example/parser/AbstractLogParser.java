package org.example.parser;

import org.example.model.LogEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractLogParser {
    public final List<LogEntry> parseFile(Path path) throws IOException {
        try (var lines = Files.lines(path)) {
            return lines.map(this::parseLineSafe).filter(e -> e != null).collect(Collectors.toList());
        }
    }


    private LogEntry parseLineSafe(String line) {
        try { return parseLine(line); } catch (Exception ex) { return null; }
    }


    protected abstract LogEntry parseLine(String line);
}
