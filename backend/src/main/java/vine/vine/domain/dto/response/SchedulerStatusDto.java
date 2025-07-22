package vine.vine.domain.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
    private LocalDateTime lastStartTime;
    private LocalDateTime nextFireTime;
    private LocalDateTime lastRunTime;
    private LocalDateTime lastStopTime;
    private LocalDateTime nextRunTime;
    private LocalDateTime startFromTime;
    private String triggerState;
    private long totalExecutions;
    private long successfulExecutions;
    private long failedExecutions;
}