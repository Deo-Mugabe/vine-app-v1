package vine.vine.domain;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "job_execution_history")
public class JobExecutionHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "execution_id")
    private Long id;

    @Column(name = "job_name", nullable = false)
    private String jobName;

    @Column(name = "job_group", nullable = false)
    private String jobGroup;

    @Column(name = "trigger_name")
    private String triggerName;

    @Column(name = "trigger_group")
    private String triggerGroup;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "records_processed")
    private Long recordsProcessed;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "process_from_time")
    private LocalDateTime processFromTime;

    @Column(name = "process_to_time")
    private LocalDateTime processToTime;

    public enum ExecutionStatus {
        STARTED,
        COMPLETED,
        FAILED,
        INTERRUPTED
    }
}
