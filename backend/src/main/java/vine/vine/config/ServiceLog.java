package vine.vine.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class ServiceLog {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void logError(String message, String fileName, boolean appendTimestamp) {
        try {
            String logMessage = message;
            if (appendTimestamp) {
                logMessage = LocalDateTime.now().format(TIMESTAMP_FORMAT) + " - " + message;
            }
            logMessage += System.lineSeparator();

            Path logPath = Paths.get("logs", fileName);
            Files.createDirectories(logPath.getParent());

            Files.write(logPath, logMessage.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

        } catch (IOException e) {
            log.error("Failed to write to log file: {}", fileName, e);
        }
    }

    public void logInfo(String message, String fileName, boolean appendTimestamp) {
        try {
            String logMessage = message;
            if (appendTimestamp) {
                logMessage = LocalDateTime.now().format(TIMESTAMP_FORMAT) + " - " + message;
            }
            logMessage += System.lineSeparator();

            Path logPath = Paths.get("logs", fileName);
            Files.createDirectories(logPath.getParent());

            Files.write(logPath, logMessage.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

        } catch (IOException e) {
            log.error("Failed to write to log file: {}", fileName, e);
        }
    }
}