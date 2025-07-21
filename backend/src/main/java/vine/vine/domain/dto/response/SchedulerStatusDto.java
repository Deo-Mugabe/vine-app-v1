package vine.vine.domain.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerStatusDto {
    private String jobName;
    private String jobGroup;
    private boolean enabled;
    private boolean running;
    private int intervalMinutes;
    private LocalDateTime lastRunTime;
    private LocalDateTime nextRunTime;
    private LocalDateTime startFromTime;
    private String triggerState;
    private long totalExecutions;
    private long successfulExecutions;
    private long failedExecutions;
}