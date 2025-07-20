package vine.vine.config;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ServiceLog {

    public void logError(String message, String fileName, boolean includeTimestamp) {
        String entry = includeTimestamp
                ? String.format("[%s] %s%n", LocalDateTime.now(), message)
                : message + System.lineSeparator();

        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write(entry);
        } catch (IOException e) {
            log.error("Failed to write to {}: {}", fileName, e.getMessage(), e);
        }
    }
}