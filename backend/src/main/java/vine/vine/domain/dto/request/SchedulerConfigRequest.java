package vine.vine.domain.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerConfigRequest {
    private boolean enabled;
    private int intervalMinutes;
    private LocalDateTime startFromTime;
}