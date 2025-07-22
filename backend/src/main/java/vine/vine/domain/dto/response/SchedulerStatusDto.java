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
    private String status; // Added: RUNNING, STOPPED, ERROR, etc.
    private int intervalMinutes;
    private LocalDateTime lastStartTime;
    private LocalDateTime lastStopTime; // Fixed: This was missing
    private LocalDateTime nextFireTime;
    private LocalDateTime lastRunTime;
    private LocalDateTime nextRunTime;
    private LocalDateTime startFromTime;
    private String triggerState;
    private long totalExecutions;
    private long successfulExecutions;
    private long failedExecutions;
    
    // Added: Additional useful fields
    private LocalDateTime lastSuccessfulRun;
    private String lastErrorMessage;
}