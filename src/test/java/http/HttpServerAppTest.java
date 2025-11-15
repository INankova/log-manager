package http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpPrincipal;
import org.example.http.HttpServerApp;
import org.example.model.LogEntry;
import org.example.model.LogType;
import org.example.service.LogService;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpServerAppTest {

    static class FakeExchange extends HttpExchange {
        private final String method;
        private final URI uri;
        private final Headers requestHeaders = new Headers();
        private final Headers responseHeaders = new Headers();
        private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
        private int responseCode;

        FakeExchange(String method, URI uri) {
            this.method = method;
            this.uri = uri;
        }

        @Override public Headers getRequestHeaders() { return requestHeaders; }
        @Override public Headers getResponseHeaders() { return responseHeaders; }
        @Override public URI getRequestURI() { return uri; }
        @Override public String getRequestMethod() { return method; }
        @Override public HttpContext getHttpContext() { return null; }
        @Override public void close() {}
        @Override public InputStream getRequestBody() { return InputStream.nullInputStream(); }
        @Override public OutputStream getResponseBody() { return responseBody; }
        @Override public void sendResponseHeaders(int rCode, long responseLength) {
            this.responseCode = rCode;
        }
        @Override public InetSocketAddress getRemoteAddress() { return null; }
        @Override public InetSocketAddress getLocalAddress() { return null; }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public void setAttribute(String name, Object value) {}
        @Override public void setStreams(InputStream i, OutputStream o) {}
        @Override public HttpPrincipal getPrincipal() { return null; }

        public int getResponseCode() { return responseCode; }
        String getResponseBodyAsString() {
            return responseBody.toString(StandardCharsets.UTF_8);
        }
    }

    @Test
    void logHandler_LogsMessageAndReturns201() throws Exception {
        LogService service = mock(LogService.class);
        HttpServerApp app = new HttpServerApp(service);
        HttpHandler handler = app.new LogHandler();

        URI uri = new URI("/log?type=ERROR&msg=Hello%20World&src=test-src");
        FakeExchange ex = new FakeExchange("GET", uri);

        handler.handle(ex);

        verify(service).log(LogType.ERROR, "Hello World", "test-src");

        assertEquals(201, ex.getResponseCode());
        assertEquals("logged", ex.getResponseBodyAsString());
    }

    @Test
    void logsByTypeHandler_ReturnsJoinedLogsAnd200() throws Exception {
        LogService service = mock(LogService.class);
        HttpServerApp app = new HttpServerApp(service);
        HttpHandler handler = app.new LogsByTypeHandler();
        LogEntry e1 = mock(LogEntry.class);
        LogEntry e2 = mock(LogEntry.class);
        when(e1.toString()).thenReturn("first-log");
        when(e2.toString()).thenReturn("second-log");

        when(service.getByType(LogType.INFO)).thenReturn(List.of(e1, e2));

        URI uri = new URI("/logs?type=INFO");
        FakeExchange ex = new FakeExchange("GET", uri);

        handler.handle(ex);

        verify(service).getByType(LogType.INFO);

        assertEquals(200, ex.getResponseCode());
        assertEquals("first-log\nsecond-log\n", ex.getResponseBodyAsString());
    }

    @Test
    void ingestHandler_MissingPath_Returns400() throws Exception {
        LogService service = mock(LogService.class);
        HttpServerApp app = new HttpServerApp(service);
        HttpHandler handler = app.new IngestHandler();

        URI uri = new URI("/model");
        FakeExchange ex = new FakeExchange("GET", uri);

        handler.handle(ex);

        verify(service, never()).ingestFile(any());
        assertEquals(400, ex.getResponseCode());
        assertEquals("missing ?path=", ex.getResponseBodyAsString());
    }

    @Test
    void ingestHandler_WithPath_CallsServiceAndReturns200() throws Exception {
        LogService service = mock(LogService.class);
        HttpServerApp app = new HttpServerApp(service);
        HttpHandler handler = app.new IngestHandler();

        String filePath = "/tmp/test.log";
        URI uri = new URI("/ingest?path=" + filePath);
        FakeExchange ex = new FakeExchange("GET", uri);

        handler.handle(ex);

        verify(service).ingestFile(Path.of(filePath));
        assertEquals(200, ex.getResponseCode());
        assertEquals("ingested", ex.getResponseBodyAsString());
    }

    @Test
    void queryParse_ReturnsEmptyMapForNullOrEmpty() {
        assertTrue(HttpServerApp.Query.parse(null).isEmpty());
        assertTrue(HttpServerApp.Query.parse("").isEmpty());
    }

    @Test
    void queryParse_ParsesAndDecodesParams() {
        String q = "type=ERROR&msg=Hello%20World&src=test%20src&noValue";
        Map<String, String> params = HttpServerApp.Query.parse(q);

        assertEquals("ERROR", params.get("type"));
        assertEquals("Hello World", params.get("msg"));
        assertEquals("test src", params.get("src"));
        assertEquals("", params.get("noValue"));
    }
}
