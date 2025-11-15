package model;

import org.example.model.LogType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogTypeTest {

    @Test
    void fromTag_ShouldReturnInfo() {
        assertEquals(LogType.INFO, LogType.fromTag("INFO"));
        assertEquals(LogType.INFO, LogType.fromTag("info"));
    }

    @Test
    void fromTag_ShouldReturnWarning() {
        assertEquals(LogType.WARNING, LogType.fromTag("WARNING"));
        assertEquals(LogType.WARNING, LogType.fromTag("WARN"));
        assertEquals(LogType.WARNING, LogType.fromTag("warn"));
    }

    @Test
    void fromTag_ShouldReturnError() {
        assertEquals(LogType.ERROR, LogType.fromTag("ERROR"));
        assertEquals(LogType.ERROR, LogType.fromTag("ERR"));
        assertEquals(LogType.ERROR, LogType.fromTag("error"));
    }

    @Test
    void fromTag_ShouldReturnDebug() {
        assertEquals(LogType.DEBUG, LogType.fromTag("DEBUG"));
        assertEquals(LogType.DEBUG, LogType.fromTag("DBG"));
        assertEquals(LogType.DEBUG, LogType.fromTag("dbg"));
    }

    @Test
    void fromTag_ShouldFallbackToInfo_IfUnknown() {
        assertEquals(LogType.INFO, LogType.fromTag("something"));
        assertEquals(LogType.INFO, LogType.fromTag(""));
        assertEquals(LogType.INFO, LogType.fromTag("123"));
    }
}
