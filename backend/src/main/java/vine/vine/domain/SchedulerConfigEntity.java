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
@Table(name = "scheduler_config")
public class SchedulerConfigEntity {

    @Id
    @Column(name = "config_name")
    private String configName;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "last_start_time")
    private LocalDateTime lastStartTime;
    
    @Column(name = "last_stop_time")
    private LocalDateTime lastStopTime;

    @Column(name = "interval_minutes")
    private int intervalMinutes;

    @Column(name = "last_run_time")
    private LocalDateTime lastRunTime;

    @Column(name = "next_run_time")
    private LocalDateTime nextRunTime;

    @Column(name = "start_from_time")
    private LocalDateTime startFromTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (startFromTime == null) {
            startFromTime = LocalDateTime.now().minusDays(30);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}