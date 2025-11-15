package org.example;

import org.example.http.HttpServerApp;
import org.example.parser.SimpleLineLogParser;
import org.example.service.LogService;
import org.example.storage.LogRepository;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        var storage = LogRepository.getInstance(Path.of("logs"));
        var service = new LogService(storage, new SimpleLineLogParser());

        service.log(org.example.model.LogType.INFO, "Приложението стартира", "main");
        service.log(org.example.model.LogType.ERROR, "Грешка при свързване", "db");

        new HttpServerApp(service).start(8080);

    }
}