package org.example.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.example.model.LogType;
import org.example.service.LogService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class HttpServerApp {
    private final LogService service;


    public HttpServerApp(LogService service) { this.service = service; }


    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/health", exchange -> respond(exchange, 200, "OK"));
        server.createContext("/log", new LogHandler());
        server.createContext("/logs", new LogsByTypeHandler());
        server.createContext("/model", new IngestHandler());
        server.start();
        System.out.println("HTTP server started on port " + port);
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        exchange.sendResponseHeaders(status, body.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(body.getBytes()); }
    }


    public class LogHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            var q = ex.getRequestURI().getQuery();
            var params = Query.parse(q);
            var type = LogType.fromTag(params.getOrDefault("type", "INFO"));
            var msg = params.getOrDefault("msg", "");
            var src = params.getOrDefault("src", "api");
            service.log(type, msg, src);
            respond(ex, 201, "logged");
        }
    }
    public class LogsByTypeHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            var q = ex.getRequestURI().getQuery();
            var params = Query.parse(q);
            var type = LogType.fromTag(params.getOrDefault("type", "INFO"));
            var list = service.getByType(type);
            String body = list.stream().map(Object::toString).collect(Collectors.joining("\n"));
            respond(ex, 200, body + (body.isEmpty()?"":"\n"));
        }
    }


    public class IngestHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            var q = ex.getRequestURI().getQuery();
            var params = Query.parse(q);
            var path = params.get("path");
            if (path == null) { respond(ex, 400, "missing ?path="); return; }
            service.ingestFile(Path.of(path));
            respond(ex, 200, "ingested");
        }
    }

    public static class Query {
        public static java.util.Map<String,String> parse(String q) {
            java.util.Map<String,String> m = new java.util.HashMap<>();
            if (q == null || q.isEmpty()) return m;
            for (String p : q.split("&")) {
                var kv = p.split("=",2);
                m.put(urlDecode(kv[0]), kv.length>1? urlDecode(kv[1]): "");
            }
            return m;
        }
        static String urlDecode(String s) {
            try { return java.net.URLDecoder.decode(s, java.nio.charset.StandardCharsets.UTF_8); }
            catch (Exception e) { return s; }
        }
    }
}