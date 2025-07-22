package vine.vine.service;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vine.vine.domain.JobExecutionHistoryEntity;
import vine.vine.domain.SchedulerConfigEntity;
import vine.vine.domain.dto.response.JobExecutionDto;
import vine.vine.domain.dto.response.SchedulerHistoryResponse;
import vine.vine.domain.dto.response.SchedulerStatusDto;
import vine.vine.repository.JobExecutionHistoryRepository;
import vine.vine.repository.SchedulerConfigRepository;
import vine.vine.scheduler.BookingProcessorJob;

import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    @Autowired(required = false)
    private Scheduler scheduler;

    @Autowired(required = false)
    private JobDetail bookingProcessorJobDetail;

    private final JobExecutionHistoryRepository jobHistoryRepository;
    private final SchedulerConfigRepository schedulerConfigRepository;

    private static final String JOB_NAME = "bookingProcessorJob";
    private static final String JOB_GROUP = "vine-group";
    private static final String TRIGGER_NAME = "bookingProcessorTrigger";
    private static final String TRIGGER_GROUP = "vine-group";
    private static final String CONFIG_NAME = "booking-processor";

    @PostConstruct
    public void initializeSchedulerConfig() {
        try {
            log.info("Initializing scheduler configuration...");

            if (schedulerConfigRepository == null) {
                log.error("SchedulerConfigRepository is null!");
                return;
            }

            if (!schedulerConfigRepository.existsById(CONFIG_NAME)) {
                log.info("Creating new scheduler configuration");
                SchedulerConfigEntity config = new SchedulerConfigEntity();
                config.setConfigName(CONFIG_NAME);
                config.setEnabled(false);
                config.setIntervalMinutes(30);
                config.setStartFromTime(LocalDateTime.now().minusDays(30));
                schedulerConfigRepository.save(config);
                log.info("Scheduler configuration created successfully");
            } else {
                log.info("Scheduler configuration already exists");
            }

            // Ensure job is registered
            ensureJobIsRegistered();

        } catch (Exception e) {
            log.error("Error initializing scheduler configuration", e);
        }
    }

    private void ensureJobIsRegistered() {
        try {
            if (scheduler == null) {
                log.warn("Scheduler not available for job registration");
                return;
            }

            JobKey jobKey = new JobKey(JOB_NAME, JOB_GROUP);

            if (!scheduler.checkExists(jobKey)) {
                log.info("Job does not exist, registering it...");

                // Create job detail if not injected
                JobDetail jobDetail = bookingProcessorJobDetail;
                if (jobDetail == null) {
                    log.info("Creating JobDetail manually");
                    jobDetail = JobBuilder.newJob(BookingProcessorJob.class)
                            .withIdentity(JOB_NAME, JOB_GROUP)
                            .withDescription("Job to process bookings")
                            .storeDurably(true)
                            .requestRecovery(true)
                            .build();
                }

                scheduler.addJob(jobDetail, true);
                log.info("Job registered successfully: {}", JOB_NAME);
            } else {
                log.info("Job already exists: {}", JOB_NAME);
            }
        } catch (SchedulerException e) {
            log.error("Error ensuring job is registered", e);
        }
    }

    public SchedulerStatusDto getSchedulerStatus() {
        try {
            log.info("Getting scheduler status...");

            // Check if scheduler exists
            if (scheduler == null) {
                log.error("Scheduler bean is null!");
                throw new RuntimeException("Scheduler not available");
            }

            SchedulerConfigEntity config = getSchedulerConfig();
            log.info("Retrieved config: enabled={}, interval={}", config.isEnabled(), config.getIntervalMinutes());

            TriggerKey triggerKey = new TriggerKey(TRIGGER_NAME, TRIGGER_GROUP);

            boolean isRunning = false;
            LocalDateTime nextRunTime = null;
            String triggerState = "NONE";

            try {
                if (scheduler.checkExists(triggerKey)) {
                    Trigger.TriggerState state = scheduler.getTriggerState(triggerKey);
                    triggerState = state.name();
                    isRunning = (state == Trigger.TriggerState.NORMAL || state == Trigger.TriggerState.BLOCKED);

                    Trigger trigger = scheduler.getTrigger(triggerKey);
                    if (trigger != null && trigger.getNextFireTime() != null) {
                        nextRunTime = LocalDateTime.ofInstant(
                                trigger.getNextFireTime().toInstant(),
                                ZoneId.systemDefault()
                        );
                    }
                }
            } catch (SchedulerException e) {
                log.warn("Error checking trigger status: {}", e.getMessage());
            }

            // Get execution statistics
            List<JobExecutionHistoryEntity> allExecutions = jobHistoryRepository
                    .findByJobNameAndJobGroupOrderByStartTimeDesc(JOB_NAME, JOB_GROUP);

            long totalExecutions = allExecutions.size();
            long successfulExecutions = allExecutions.stream()
                    .mapToLong(e -> e.getStatus() == JobExecutionHistoryEntity.ExecutionStatus.COMPLETED ? 1 : 0)
                    .sum();
            long failedExecutions = totalExecutions - successfulExecutions;

            SchedulerStatusDto status = new SchedulerStatusDto();
            status.setJobName(JOB_NAME);
            status.setJobGroup(JOB_GROUP);
            status.setEnabled(config.isEnabled());
            status.setRunning(isRunning);
            status.setIntervalMinutes(config.getIntervalMinutes());
            status.setLastRunTime(config.getLastRunTime());
            status.setNextRunTime(nextRunTime);
            status.setStartFromTime(config.getStartFromTime());
            status.setTriggerState(triggerState);
            status.setTotalExecutions(totalExecutions);
            status.setSuccessfulExecutions(successfulExecutions);
            status.setFailedExecutions(failedExecutions);

            log.info("Scheduler status created successfully");
            return status;
        } catch (Exception e) {
            log.error("Error getting scheduler status", e);
            throw new RuntimeException("Failed to get scheduler status: " + e.getMessage(), e);
        }
    }

    @Transactional
    public SchedulerStatusDto startScheduler(int intervalMinutes) {
        try {
            log.info("Starting scheduler with interval: {} minutes", intervalMinutes);

            if (scheduler == null) {
                throw new RuntimeException("Scheduler not available");
            }

            if (intervalMinutes < 1) {
                throw new IllegalArgumentException("Interval minutes must be at least 1");
            }

            // Ensure job is registered before creating trigger
            ensureJobIsRegistered();

            SchedulerConfigEntity config = getSchedulerConfig();
            config.setEnabled(true);
            config.setIntervalMinutes(intervalMinutes);
            schedulerConfigRepository.save(config);
            log.info("Config updated and saved");

            // Remove existing trigger if it exists
            TriggerKey triggerKey = new TriggerKey(TRIGGER_NAME, TRIGGER_GROUP);
            if (scheduler.checkExists(triggerKey)) {
                scheduler.unscheduleJob(triggerKey);
                log.info("Removed existing trigger");
            }

            // Verify job exists before creating trigger
            JobKey jobKey = new JobKey(JOB_NAME, JOB_GROUP);
            if (!scheduler.checkExists(jobKey)) {
                log.error("Job still does not exist after registration attempt: {}", JOB_NAME);
                throw new RuntimeException("Job does not exist: " + JOB_NAME);
            }

            // Create new trigger with updated interval
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(TRIGGER_NAME, TRIGGER_GROUP)
                    .forJob(JOB_NAME, JOB_GROUP)
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMinutes(intervalMinutes)
                            .repeatForever())
                    .startNow()
                    .build();

            scheduler.scheduleJob(trigger);
            log.info("Scheduled new trigger with interval: {} minutes", intervalMinutes);

            if (trigger.getNextFireTime() != null) {
                config.setNextRunTime(LocalDateTime.ofInstant(
                        trigger.getNextFireTime().toInstant(),
                        ZoneId.systemDefault()
                ));
                schedulerConfigRepository.save(config);
            }

            log.info("Scheduler started successfully");
            return getSchedulerStatus();
        } catch (SchedulerException e) {
            log.error("SchedulerException starting scheduler", e);
            throw new RuntimeException("Failed to start scheduler: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("General error starting scheduler", e);
            throw new RuntimeException("Failed to start scheduler: " + e.getMessage(), e);
        }
    }

    @Transactional
    public SchedulerStatusDto stopScheduler() {
        try {
            log.info("Stopping scheduler");

            if (scheduler == null) {
                throw new RuntimeException("Scheduler not available");
            }

            SchedulerConfigEntity config = getSchedulerConfig();
            config.setEnabled(false);
            config.setNextRunTime(null);
            schedulerConfigRepository.save(config);

            TriggerKey triggerKey = new TriggerKey(TRIGGER_NAME, TRIGGER_GROUP);
            if (scheduler.checkExists(triggerKey)) {
                scheduler.unscheduleJob(triggerKey);
                log.info("Unscheduled trigger");
            }

            log.info("Scheduler stopped successfully");
            return getSchedulerStatus();
        } catch (SchedulerException e) {
            log.error("SchedulerException stopping scheduler", e);
            throw new RuntimeException("Failed to stop scheduler: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("General error stopping scheduler", e);
            throw new RuntimeException("Failed to stop scheduler: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void triggerJobNow() {
        try {
            log.info("Triggering job manually");

            if (scheduler == null) {
                throw new RuntimeException("Scheduler not available");
            }

            // Ensure job is registered
            ensureJobIsRegistered();

            JobKey jobKey = new JobKey(JOB_NAME, JOB_GROUP);
            if (scheduler.checkExists(jobKey)) {
                scheduler.triggerJob(jobKey);
                log.info("Job triggered manually: {}", JOB_NAME);
            } else {
                log.error("Job does not exist: {}", JOB_NAME);
                throw new RuntimeException("Job does not exist: " + JOB_NAME);
            }
        } catch (SchedulerException e) {
            log.error("SchedulerException triggering job manually", e);
            throw new RuntimeException("Failed to trigger job manually: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("General error triggering job manually", e);
            throw new RuntimeException("Failed to trigger job manually: " + e.getMessage(), e);
        }
    }

    private SchedulerConfigEntity getSchedulerConfig() {
        return schedulerConfigRepository.findById(CONFIG_NAME)
                .orElseThrow(() -> new RuntimeException("Scheduler configuration not found"));
    }

    // Simplified methods for other operations
    public SchedulerStatusDto updateSchedulerConfig(boolean enabled, int intervalMinutes, LocalDateTime startFromTime) {
        try {
            if (intervalMinutes < 1) {
                throw new IllegalArgumentException("Interval minutes must be at least 1");
            }

            SchedulerConfigEntity config = getSchedulerConfig();
            config.setEnabled(enabled);
            config.setIntervalMinutes(intervalMinutes);
            if (startFromTime != null) {
                config.setStartFromTime(startFromTime);
            }
            schedulerConfigRepository.save(config);

            if (enabled) {
                return startScheduler(intervalMinutes);
            } else {
                return stopScheduler();
            }
        } catch (Exception e) {
            log.error("Error updating scheduler config", e);
            throw new RuntimeException("Failed to update scheduler configuration: " + e.getMessage(), e);
        }
    }

    public SchedulerHistoryResponse getJobHistory(int page, int size, Integer days) {
        try {
            List<JobExecutionHistoryEntity> executions = jobHistoryRepository
                    .findByJobNameAndJobGroupOrderByStartTimeDesc(JOB_NAME, JOB_GROUP);

            SchedulerHistoryResponse response = new SchedulerHistoryResponse();
            response.setExecutions(List.of()); // Simplified for now
            response.setTotalCount(executions.size());
            response.setPage(page);
            response.setSize(size);

            return response;
        } catch (Exception e) {
            log.error("Error getting job history", e);
            throw new RuntimeException("Failed to get job history: " + e.getMessage(), e);
        }
    }

    // Stub methods for job execution recording
    public LocalDateTime getProcessingStartTime(String jobName) {
        return LocalDateTime.now().minusDays(30);
    }

    public Long recordJobStart(String jobName, String jobGroup, String triggerName,
                               String triggerGroup, LocalDateTime startTime) {
        return 1L; // Stub
    }

    public void recordJobCompletion(Long executionId, LocalDateTime endTime,
                                    long recordsProcessed, LocalDateTime processFromTime,
                                    LocalDateTime processToTime) {
        // Stub
    }

    public void recordJobFailure(Long executionId, LocalDateTime endTime, String errorMessage) {
        // Stub
    }

    public void updateLastRunTime(String jobName, LocalDateTime lastRunTime) {
        // Stub
    }
}