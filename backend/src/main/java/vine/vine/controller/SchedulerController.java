package vine.vine.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vine.vine.domain.dto.request.SchedulerConfigRequest;
import vine.vine.domain.dto.response.SchedulerHistoryResponse;
import vine.vine.domain.dto.response.SchedulerStatusDto;
import vine.vine.service.SchedulerService;

@RestController
@RequestMapping("/api/v1/scheduler")
@RequiredArgsConstructor
@Slf4j
public class SchedulerController {

    private final SchedulerService schedulerService;

    @GetMapping("/status")
    public ResponseEntity<SchedulerStatusDto> getSchedulerStatus() {
        try {
            SchedulerStatusDto status = schedulerService.getSchedulerStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting scheduler status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/start")
    public ResponseEntity<SchedulerStatusDto> startScheduler(@RequestParam(defaultValue = "30") int intervalMinutes) {
        try {
            if (intervalMinutes < 1) {
                return ResponseEntity.badRequest().build();
            }
            
            SchedulerStatusDto status = schedulerService.startScheduler(intervalMinutes);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error starting scheduler", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<SchedulerStatusDto> stopScheduler() {
        try {
            SchedulerStatusDto status = schedulerService.stopScheduler();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error stopping scheduler", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/config")
    public ResponseEntity<SchedulerStatusDto> updateSchedulerConfig(@RequestBody SchedulerConfigRequest request) {
        try {
            if (request.getIntervalMinutes() < 1) {
                return ResponseEntity.badRequest().build();
            }
            
            SchedulerStatusDto status = schedulerService.updateSchedulerConfig(
                request.isEnabled(), 
                request.getIntervalMinutes(), 
                request.getStartFromTime()
            );
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error updating scheduler configuration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<SchedulerHistoryResponse> getJobHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer days) {
        try {
            if (page < 0 || size < 1 || size > 100) {
                return ResponseEntity.badRequest().build();
            }
            
            SchedulerHistoryResponse history = schedulerService.getJobHistory(page, size, days);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting job history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/history/latest")
    public ResponseEntity<SchedulerHistoryResponse> getLatestExecutions(@RequestParam(defaultValue = "10") int limit) {
        try {
            if (limit < 1 || limit > 50) {
                return ResponseEntity.badRequest().build();
            }
            
            SchedulerHistoryResponse history = schedulerService.getJobHistory(0, limit, null);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting latest job executions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/run-now")
    public ResponseEntity<String> runJobNow() {
        try {
            // This will trigger the job immediately
            schedulerService.triggerJobNow();
            return ResponseEntity.ok("Job triggered successfully");
        } catch (Exception e) {
            log.error("Error triggering job manually", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to trigger job: " + e.getMessage());
        }
    }
}