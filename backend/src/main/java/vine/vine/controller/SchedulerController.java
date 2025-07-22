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

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@RestController
@RequestMapping("/api/v1/scheduler")
@RequiredArgsConstructor
@Slf4j
public class SchedulerController {

    private final SchedulerService schedulerService;

    @GetMapping("/status")
    public ResponseEntity<?> getSchedulerStatus() {
        try {
            SchedulerStatusDto status = schedulerService.getSchedulerStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting scheduler status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get scheduler status: " + e.getMessage());
        }
    }

    @PostMapping("/start")
    public ResponseEntity<?> startScheduler(
            @RequestParam(defaultValue = "30")
            @Min(value = 1, message = "Interval must be at least 1 minute")
            @Max(value = 1440, message = "Interval cannot exceed 1440 minutes (24 hours)")
            int intervalMinutes) {
        try {
            log.info("Starting scheduler with interval: {} minutes", intervalMinutes);
            SchedulerStatusDto status = schedulerService.startScheduler(intervalMinutes);
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            log.error("Invalid interval parameter: {}", intervalMinutes, e);
            return ResponseEntity.badRequest().body("Invalid interval: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error starting scheduler with interval: {}", intervalMinutes, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start scheduler: " + e.getMessage());
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stopScheduler() {
        try {
            log.info("Stopping scheduler");
            SchedulerStatusDto status = schedulerService.stopScheduler();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error stopping scheduler", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to stop scheduler: " + e.getMessage());
        }
    }

    @PutMapping("/config")
    public ResponseEntity<?> updateSchedulerConfig(@Valid @RequestBody SchedulerConfigRequest request) {
        try {
            log.info("Updating scheduler config: enabled={}, interval={}, startFromTime={}",
                    request.isEnabled(), request.getIntervalMinutes(), request.getStartFromTime());

            if (request.getIntervalMinutes() < 1) {
                return ResponseEntity.badRequest().body("Interval minutes must be at least 1");
            }

            if (request.getIntervalMinutes() > 1440) {
                return ResponseEntity.badRequest().body("Interval minutes cannot exceed 1440 (24 hours)");
            }

            // Use the helper method to convert time string to LocalDateTime
            SchedulerStatusDto status = schedulerService.updateSchedulerConfig(
                    request.isEnabled(),
                    request.getIntervalMinutes(),
                    request.getStartFromTimeAsDateTime()
            );
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            log.error("Invalid configuration request: {}", request, e);
            return ResponseEntity.badRequest().body("Invalid configuration: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error updating scheduler configuration: {}", request, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update scheduler configuration: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getJobHistory(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be non-negative")
            int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Size must be at least 1")
            @Max(value = 100, message = "Size cannot exceed 100")
            int size,
            @RequestParam(required = false)
            @Min(value = 1, message = "Days must be at least 1")
            Integer days) {
        try {
            log.debug("Getting job history: page={}, size={}, days={}", page, size, days);
            SchedulerHistoryResponse history = schedulerService.getJobHistory(page, size, days);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            log.error("Invalid history request parameters: page={}, size={}, days={}", page, size, days, e);
            return ResponseEntity.badRequest().body("Invalid parameters: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error getting job history: page={}, size={}, days={}", page, size, days, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get job history: " + e.getMessage());
        }
    }

    @GetMapping("/history/latest")
    public ResponseEntity<?> getLatestExecutions(
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Limit must be at least 1")
            @Max(value = 50, message = "Limit cannot exceed 50")
            int limit) {
        try {
            log.debug("Getting latest {} job executions", limit);
            SchedulerHistoryResponse history = schedulerService.getJobHistory(0, limit, null);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            log.error("Invalid limit parameter: {}", limit, e);
            return ResponseEntity.badRequest().body("Invalid limit: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error getting latest job executions with limit: {}", limit, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get latest executions: " + e.getMessage());
        }
    }

    @PostMapping("/run-now")
    public ResponseEntity<?> runJobNow() {
        try {
            log.info("Triggering job manually");
            schedulerService.triggerJobNow();
            return ResponseEntity.ok("Job triggered successfully");
        } catch (Exception e) {
            log.error("Error triggering job manually", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to trigger job: " + e.getMessage());
        }
    }
}