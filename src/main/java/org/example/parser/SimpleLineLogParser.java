package org.example.parser;

import org.example.model.LogEntry;
import org.example.model.LogType;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleLineLogParser extends AbstractLogParser {
    private static final Pattern P = Pattern.compile(
            "^(?<ts>\\S+) \\[(?<type>\\w+)\\] \\((?<src>[^)]+)\\) (?<msg>.*)$");


    @Override
    public LogEntry parseLine(String line) {
        Matcher m = P.matcher(line);
        if (!m.matches()) return null;
        var ts = Instant.parse(m.group("ts"));
        var type = LogType.fromTag(m.group("type"));
        var src = m.group("src");
        var msg = m.group("msg");
        return new LogEntry(ts, type, msg, src);
    }
}
