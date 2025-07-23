package vine.vine.domain.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import vine.vine.domain.JobExecutionHistoryEntity.ExecutionStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobExecutionDto {
    private Long id;
    private String jobName;
    private String jobGroup;
    private String triggerName;
    private String triggerGroup;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ExecutionStatus status;
    private String errorMessage;
    private Long recordsProcessed;
    private Long durationMs;
    private LocalDateTime processFromTime;
    private LocalDateTime processToTime;
    
    // ✅ NEW: Add display message
    private String message;
    
    // ✅ Helper method to format duration nicely
    public String getFormattedDuration() {
        if (durationMs == null) return "N/A";
        
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}