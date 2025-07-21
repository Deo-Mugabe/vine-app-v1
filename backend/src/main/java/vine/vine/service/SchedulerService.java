package vine.vine.service;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    @Autowired
    private Scheduler scheduler;
    
    private final JobExecutionHistoryRepository jobHistoryRepository;
    private final SchedulerConfigRepository schedulerConfigRepository;
    
    private static final String JOB_NAME = "bookingProcessorJob";
    private static final String JOB_GROUP = "vine-group";
    private static final String TRIGGER_NAME = "bookingProcessorTrigger";
    private static final String TRIGGER_GROUP = "vine-group";
    private static final String CONFIG_NAME = "booking-processor";

    @PostConstruct
    public void initializeSchedulerConfig() {
        if (!schedulerConfigRepository.existsById(CONFIG_NAME)) {
            SchedulerConfigEntity config = new SchedulerConfigEntity();
            config.setConfigName(CONFIG_NAME);
            config.setEnabled(false); // Start disabled by default
            config.setIntervalMinutes(30);
            config.setStartFromTime(LocalDateTime.now().minusDays(30));
            schedulerConfigRepository.save(config);
            log.info("Initialized scheduler configuration");
        }
    }

    public SchedulerStatusDto getSchedulerStatus() {
        try {
            SchedulerConfigEntity config = getSchedulerConfig();
            TriggerKey triggerKey = new TriggerKey(TRIGGER_NAME, TRIGGER_GROUP);
            
            boolean isRunning = false;
            LocalDateTime nextRunTime = null;
            String triggerState = "NONE";
            
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

            // Get execution statistics
            List<JobExecutionHistoryEntity> allExecutions = jobHistoryRepository
                .findByJobNameAndJobGroupOrderByStartTimeDesc(JOB_NAME, JOB_GROUP);
            
            long totalExecutions = allExecutions.size();
            long successfulExecutions = allExecutions.stream()
                .mapToLong(e -> e.getStatus() == JobExecutionHistoryEntity.ExecutionStatus.COMPLETED ? 1 : 0)
                .sum();
            long failedExecutions = totalExecutions - successfulExecutions;

            return new SchedulerStatusDto(
                JOB_NAME,
                JOB_GROUP,
                config.isEnabled(),
                isRunning,
                config.getIntervalMinutes(),
                config.getLastRunTime(),
                nextRunTime,
                config.getStartFromTime(),
                triggerState,
                totalExecutions,
                successfulExecutions,
                failedExecutions
            );
        } catch (SchedulerException e) {
            log.error("Error getting scheduler status", e);
            throw new RuntimeException("Failed to get scheduler status", e);
        }
    }

    @Transactional
    public SchedulerStatusDto startScheduler(int intervalMinutes) {
        try {
            SchedulerConfigEntity config = getSchedulerConfig();
            config.setEnabled(true);
            config.setIntervalMinutes(intervalMinutes);
            schedulerConfigRepository.save(config);

            // Remove existing trigger if it exists
            TriggerKey triggerKey = new TriggerKey(TRIGGER_NAME, TRIGGER_GROUP);
            if (scheduler.checkExists(triggerKey)) {
                scheduler.unscheduleJob(triggerKey);
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
            
            config.setNextRunTime(LocalDateTime.ofInstant(
                trigger.getNextFireTime().toInstant(), 
                ZoneId.systemDefault()
            ));
            schedulerConfigRepository.save(config);

            log.info("Scheduler started with interval: {} minutes", intervalMinutes);
            return getSchedulerStatus();
        } catch (SchedulerException e) {
            log.error("Error starting scheduler", e);
            throw new RuntimeException("Failed to start scheduler", e);
        }
    }

    @Transactional
    public SchedulerStatusDto stopScheduler() {
        try {
            SchedulerConfigEntity config = getSchedulerConfig();
            config.setEnabled(false);
            config.setNextRunTime(null);
            schedulerConfigRepository.save(config);

            TriggerKey triggerKey = new TriggerKey(TRIGGER_NAME, TRIGGER_GROUP);
            if (scheduler.checkExists(triggerKey)) {
                scheduler.unscheduleJob(triggerKey);
            }

            log.info("Scheduler stopped");
            return getSchedulerStatus();
        } catch (SchedulerException e) {
            log.error("Error stopping scheduler", e);
            throw new RuntimeException("Failed to stop scheduler", e);
        }
    }

    @Transactional
    public SchedulerStatusDto updateSchedulerConfig(boolean enabled, int intervalMinutes, LocalDateTime startFromTime) {
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
    }

    public SchedulerHistoryResponse getJobHistory(int page, int size, Integer days) {
        LocalDateTime fromDate = days != null ? LocalDateTime.now().minusDays(days) : null;
        
        List<JobExecutionHistoryEntity> executions;
        if (fromDate != null) {
            executions = jobHistoryRepository.findJobHistoryFromDate(JOB_NAME, JOB_GROUP, fromDate);
        } else {
            executions = jobHistoryRepository.findByJobNameAndJobGroupOrderByStartTimeDesc(JOB_NAME, JOB_GROUP);
        }

        // Manual pagination since we're filtering by date
        int start = page * size;
        int end = Math.min(start + size, executions.size());
        List<JobExecutionHistoryEntity> pageExecutions = executions.subList(start, end);

        List<JobExecutionDto> executionDtos = pageExecutions.stream()
            .map(this::entityToDto)
            .collect(Collectors.toList());

        return new SchedulerHistoryResponse(
            executionDtos,
            executions.size(),
            page,
            size
        );
    }

    public LocalDateTime getProcessingStartTime(String jobName) {
        SchedulerConfigEntity config = getSchedulerConfig();
        
        // Get last successful execution
        JobExecutionHistoryEntity lastSuccess = jobHistoryRepository
            .findLastSuccessfulExecution(JOB_NAME, JOB_GROUP);
        
        if (lastSuccess != null && lastSuccess.getProcessToTime() != null) {
            return lastSuccess.getProcessToTime();
        }
        
        // Fall back to configured start time or default to 30 days ago
        return config.getStartFromTime() != null ? 
            config.getStartFromTime() : 
            LocalDateTime.now().minusDays(30);
    }

    @Transactional
    public Long recordJobStart(String jobName, String jobGroup, String triggerName, 
                              String triggerGroup, LocalDateTime startTime) {
        JobExecutionHistoryEntity execution = new JobExecutionHistoryEntity();
        execution.setJobName(jobName);
        execution.setJobGroup(jobGroup);
        execution.setTriggerName(triggerName);
        execution.setTriggerGroup(triggerGroup);
        execution.setStartTime(startTime);
        execution.setStatus(JobExecutionHistoryEntity.ExecutionStatus.STARTED);
        
        JobExecutionHistoryEntity saved = jobHistoryRepository.save(execution);
        return saved.getId();
    }

    @Transactional
    public void recordJobCompletion(Long executionId, LocalDateTime endTime, 
                                  long recordsProcessed, LocalDateTime processFromTime, 
                                  LocalDateTime processToTime) {
        jobHistoryRepository.findById(executionId).ifPresent(execution -> {
            execution.setEndTime(endTime);
            execution.setStatus(JobExecutionHistoryEntity.ExecutionStatus.COMPLETED);
            execution.setRecordsProcessed(recordsProcessed);
            execution.setProcessFromTime(processFromTime);
            execution.setProcessToTime(processToTime);
            execution.setDurationMs(
                java.time.Duration.between(execution.getStartTime(), endTime).toMillis()
            );
            jobHistoryRepository.save(execution);
        });
    }

    @Transactional
    public void recordJobFailure(Long executionId, LocalDateTime endTime, String errorMessage) {
        jobHistoryRepository.findById(executionId).ifPresent(execution -> {
            execution.setEndTime(endTime);
            execution.setStatus(JobExecutionHistoryEntity.ExecutionStatus.FAILED);
            execution.setErrorMessage(errorMessage);
            execution.setDurationMs(
                java.time.Duration.between(execution.getStartTime(), endTime).toMillis()
            );
            jobHistoryRepository.save(execution);
        });
    }

    @Transactional
    public void updateLastRunTime(String jobName, LocalDateTime lastRunTime) {
        SchedulerConfigEntity config = getSchedulerConfig();
        config.setLastRunTime(lastRunTime);
        schedulerConfigRepository.save(config);
    }

    @Transactional
    public void triggerJobNow() {
        try {
            JobKey jobKey = new JobKey(JOB_NAME, JOB_GROUP);
            if (scheduler.checkExists(jobKey)) {
                scheduler.triggerJob(jobKey);
                log.info("Job triggered manually: {}", JOB_NAME);
            } else {
                throw new RuntimeException("Job does not exist: " + JOB_NAME);
            }
        } catch (SchedulerException e) {
            log.error("Error triggering job manually", e);
            throw new RuntimeException("Failed to trigger job manually", e);
        }
    }

    private SchedulerConfigEntity getSchedulerConfig() {
        return schedulerConfigRepository.findById(CONFIG_NAME)
            .orElseThrow(() -> new RuntimeException("Scheduler configuration not found"));
    }

    private JobExecutionDto entityToDto(JobExecutionHistoryEntity entity) {
        return new JobExecutionDto(
            entity.getId(),
            entity.getJobName(),
            entity.getJobGroup(),
            entity.getTriggerName(),
            entity.getTriggerGroup(),
            entity.getStartTime(),
            entity.getEndTime(),
            entity.getStatus(),
            entity.getErrorMessage(),
            entity.getRecordsProcessed(),
            entity.getDurationMs(),
            entity.getProcessFromTime(),
            entity.getProcessToTime()
        );
    }
}