package org.example.storage;

import org.example.model.LogEntry;
import org.example.model.LogType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public final class LogRepository {
    private static volatile LogRepository INSTANCE;


    private final Path rootDir;
    private final CopyOnWriteArrayList<LogEntry> entries = new CopyOnWriteArrayList<>();

    private final ExecutorService writer = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "LogWriter-1");
        t.setDaemon(true);
        return t;
    });


    private LogRepository(Path rootDir) {
        this.rootDir = Objects.requireNonNull(rootDir);
        try { Files.createDirectories(rootDir); } catch (IOException ignored) {}
    }


    public static LogRepository getInstance(Path rootDir) {
        if (INSTANCE == null) {
            synchronized (LogRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LogRepository(rootDir);
                }
            }
        }
        return INSTANCE;
    }


    public void append(LogEntry entry) {
        entries.add(entry);
        writer.submit(() -> writeToDisk(entry));
    }


    public List<LogEntry> all() { return Collections.unmodifiableList(entries); }


    public List<LogEntry> byType(LogType type) {
        return entries.stream().filter(e -> e.type() == type).collect(Collectors.toList());
    }


    private void writeToDisk(LogEntry e) {
        Path file = rootDir.resolve(e.type().name().toLowerCase() + ".log");
        String line = DateTimeFormatter.ISO_INSTANT.format(e.timestamp()) +
                " [" + e.type() + "] (" + e.source() + ") " + e.message() + System.lineSeparator();
        try {
            Files.writeString(file, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public void shutdown() {
        writer.shutdown();
    }
}
