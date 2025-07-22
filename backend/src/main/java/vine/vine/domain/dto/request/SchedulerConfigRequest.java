package vine.vine.domain.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerConfigRequest {

    @NotNull(message = "Enabled flag cannot be null")
    private boolean enabled;

    @Min(value = 1, message = "Interval minutes must be at least 1")
    private int intervalMinutes;

    // Accept time as string and convert it properly
    private String startFromTime;

    // Helper method to convert string time to LocalDateTime
    public LocalDateTime getStartFromTimeAsDateTime() {
        if (startFromTime == null || startFromTime.trim().isEmpty()) {
            return null;
        }

        try {
            // If it's already a full datetime string, parse it
            if (startFromTime.contains("T") || startFromTime.length() > 10) {
                return LocalDateTime.parse(startFromTime);
            }

            // If it's just time (HH:mm), combine with today's date
            String[] parts = startFromTime.split(":");
            if (parts.length == 2) {
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                return LocalDateTime.now().withHour(hour).withMinute(minute).withSecond(0).withNano(0);
            }
        } catch (Exception e) {
            // Return null if parsing fails
        }

        return null;
    }
}