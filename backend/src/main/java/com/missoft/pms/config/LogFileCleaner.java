package com.missoft.pms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LogFileCleaner implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${pms.log.dir:backend/logs}")
    private String logDir;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Path dir = Paths.get(logDir);
        try {
            Files.createDirectories(dir);
            List<Path> logs = Files.list(dir)
                    .filter(p -> Files.isRegularFile(p))
                    .filter(p -> p.getFileName().toString().startsWith("app-") && p.getFileName().toString().endsWith(".log"))
                    .sorted(Comparator.comparingLong((Path p) -> {
                        try {
                            return Files.getLastModifiedTime(p).toMillis();
                        } catch (IOException e) {
                            return 0L;
                        }
                    }).reversed())
                    .collect(Collectors.toList());

            int max = 15;
            if (logs.size() > max) {
                for (int i = max; i < logs.size(); i++) {
                    try {
                        Files.deleteIfExists(logs.get(i));
                    } catch (IOException ignored) {
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }
}
