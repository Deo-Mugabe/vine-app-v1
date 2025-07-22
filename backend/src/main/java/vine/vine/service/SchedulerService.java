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
import java.time.ZoneId;
import java.util.List;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
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
            }
            
            // ✅ ENSURE SCHEDULER IS STARTED BUT NO TRIGGERS ARE ACTIVE
            if (!scheduler.isStarted()) {
                log.info("🔄 Starting Quartz scheduler (without triggers)...");
                scheduler.start();
                log.info("✅ Quartz scheduler started - waiting for manual start command");
            }
            
            // ✅ REMOVE ANY EXISTING TRIGGERS FROM PREVIOUS SESSIONS
            if (scheduler.checkExists(TriggerKey.triggerKey(TRIGGER_NAME, TRIGGER_GROUP))) {
                log.info("🧹 Removing existing trigger from previous session...");
                scheduler.unscheduleJob(TriggerKey.triggerKey(TRIGGER_NAME, TRIGGER_GROUP));
                log.info("✅ Previous trigger removed");
            }
            
            // ✅ MARK CONFIG AS DISABLED IF IT WAS LEFT ENABLED
            SchedulerConfigEntity config = getSchedulerConfig();
            if (config.isEnabled()) {
                log.info("🛑 Marking scheduler as disabled (was left enabled from previous session)");
                config.setEnabled(false);
                schedulerConfigRepository.save(config);
            }
            
            log.info("✅ Scheduler initialization complete - ready for manual start");
            
        } catch (Exception e) {
            log.error("❌ Error initializing scheduler configuration", e);
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

@Transactional
public SchedulerStatusDto startScheduler(int intervalMinutes) {
    try {
        log.info("🚀 User requested scheduler start with interval: {} minutes", intervalMinutes);
        
        // Check if already running
        if (isSchedulerRunning()) {
            log.warn("⚠️ Scheduler is already running");
            return getSchedulerStatus();
        }
        
        // Ensure scheduler is started
        if (!scheduler.isStarted()) {
            log.info("🔄 Starting Quartz scheduler...");
            scheduler.start();
        }
        
        // Create trigger for the job
        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity(TRIGGER_NAME, TRIGGER_GROUP)
            .forJob(JOB_NAME, JOB_GROUP)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMinutes(intervalMinutes)
                .repeatForever())
            .startNow()
            .build();

        // Schedule the job
        scheduler.scheduleJob(trigger);
        
        // Update configuration
        SchedulerConfigEntity config = getSchedulerConfig();
        config.setEnabled(true);
        config.setIntervalMinutes(intervalMinutes);
        config.setLastStartTime(LocalDateTime.now());
        
        // ✅ Set next run time based on trigger
        if (trigger.getNextFireTime() != null) {
            config.setNextRunTime(trigger.getNextFireTime().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        
        schedulerConfigRepository.save(config);
        
        log.info("✅ Scheduler started successfully with {} minute intervals", intervalMinutes);
        
        return getSchedulerStatus();
        
    } catch (Exception e) {
        log.error("❌ Error starting scheduler", e);
        throw new RuntimeException("Failed to start scheduler: " + e.getMessage());
    }
}

/**
 * ✅ Stop the scheduler (FIXED)
 */
@Transactional
public SchedulerStatusDto stopScheduler() {
    try {
        log.info("🛑 User requested scheduler stop");
        
        // Remove the trigger
        if (scheduler.checkExists(TriggerKey.triggerKey(TRIGGER_NAME, TRIGGER_GROUP))) {
            scheduler.unscheduleJob(TriggerKey.triggerKey(TRIGGER_NAME, TRIGGER_GROUP));
            log.info("✅ Trigger removed");
        }
        
        // Update configuration
        SchedulerConfigEntity config = getSchedulerConfig();
        config.setEnabled(false);
        config.setLastStopTime(LocalDateTime.now());
        config.setNextRunTime(null);  // ✅ Clear next run time when stopped
        schedulerConfigRepository.save(config);
        
        log.info("✅ Scheduler stopped successfully");
        
        return getSchedulerStatus();
        
    } catch (Exception e) {
        log.error("❌ Error stopping scheduler", e);
        throw new RuntimeException("Failed to stop scheduler: " + e.getMessage());
    }
}

/**
 * ✅ Get current scheduler status (FIXED)
 */
    public SchedulerStatusDto getSchedulerStatus() {
    try {
        boolean isRunning = isSchedulerRunning();
        SchedulerConfigEntity config = getSchedulerConfig();
        
        SchedulerStatusDto status = new SchedulerStatusDto();
        
        // ✅ Basic job information
        status.setJobName(JOB_NAME);
        status.setJobGroup(JOB_GROUP);
        status.setEnabled(isRunning);
        status.setRunning(isRunning);
        status.setStatus(isRunning ? "RUNNING" : "STOPPED");
        
        // ✅ Configuration fields
        status.setIntervalMinutes(config.getIntervalMinutes());
        status.setLastStartTime(config.getLastStartTime());
        status.setLastStopTime(config.getLastStopTime());  // Now this method will exist
        status.setLastRunTime(config.getLastRunTime());
        status.setNextRunTime(config.getNextRunTime());
        status.setStartFromTime(config.getStartFromTime());
        
        // ✅ Trigger information
        if (isRunning) {
            try {
                Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey(TRIGGER_NAME, TRIGGER_GROUP));
                if (trigger != null) {
                    // Next fire time
                    if (trigger.getNextFireTime() != null) {
                        status.setNextFireTime(trigger.getNextFireTime().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDateTime());
                    }
                    
                    // Trigger state
                    try {
                        Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                        status.setTriggerState(triggerState.name());
                    } catch (Exception e) {
                        status.setTriggerState("UNKNOWN");
                        log.debug("Could not get trigger state: {}", e.getMessage());
                    }
                } else {
                    status.setTriggerState("NOT_FOUND");
                }
            } catch (Exception e) {
                status.setTriggerState("ERROR");
                log.debug("Could not get trigger information: {}", e.getMessage());
            }
        } else {
            status.setTriggerState("NONE");
        }
        
        // ✅ Execution statistics from job history
        try {
            List<JobExecutionHistoryEntity> allExecutions = jobHistoryRepository
                .findByJobNameAndJobGroupOrderByStartTimeDesc(JOB_NAME, JOB_GROUP);
            
            status.setTotalExecutions(allExecutions.size());
            
            status.setSuccessfulExecutions(allExecutions.stream()
                .mapToLong(exec -> exec.getStatus() == JobExecutionHistoryEntity.ExecutionStatus.COMPLETED ? 1 : 0)
                .sum());
                
            status.setFailedExecutions(allExecutions.stream()
                .mapToLong(exec -> exec.getStatus() == JobExecutionHistoryEntity.ExecutionStatus.FAILED ? 1 : 0)
                .sum());
            
            // Get last successful run
            allExecutions.stream()
                .filter(exec -> exec.getStatus() == JobExecutionHistoryEntity.ExecutionStatus.COMPLETED)
                .findFirst()
                .ifPresent(exec -> status.setLastSuccessfulRun(exec.getEndTime()));
            
            // Get last error message
            allExecutions.stream()
                .filter(exec -> exec.getStatus() == JobExecutionHistoryEntity.ExecutionStatus.FAILED)
                .findFirst()
                .ifPresent(exec -> status.setLastErrorMessage(exec.getErrorMessage()));
                
        } catch (Exception e) {
            log.debug("Could not get execution statistics: {}", e.getMessage());
            // Set defaults on error
            status.setTotalExecutions(0);
            status.setSuccessfulExecutions(0);
            status.setFailedExecutions(0);
        }
        
        return status;
        
    } catch (Exception e) {
        log.error("❌ Error getting scheduler status", e);
        
        // Return default status on error
        SchedulerStatusDto errorStatus = new SchedulerStatusDto();
        errorStatus.setJobName(JOB_NAME);
        errorStatus.setJobGroup(JOB_GROUP);
        errorStatus.setEnabled(false);
        errorStatus.setRunning(false);
        errorStatus.setStatus("ERROR");
        errorStatus.setIntervalMinutes(30);
        errorStatus.setTriggerState("ERROR");
        errorStatus.setTotalExecutions(0);
        errorStatus.setSuccessfulExecutions(0);
        errorStatus.setFailedExecutions(0);
        return errorStatus;
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

    public boolean isSchedulerRunning() {
        try {
            return scheduler.isStarted() && 
                   scheduler.checkExists(TriggerKey.triggerKey(TRIGGER_NAME, TRIGGER_GROUP));
        } catch (Exception e) {
            log.error("❌ Error checking scheduler status", e);
            return false;
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
            log.info("📊 Getting job history: page={}, size={}, days={}", page, size, days);
            
            // Get all executions for this job
            List<JobExecutionHistoryEntity> executions = jobHistoryRepository
                    .findByJobNameAndJobGroupOrderByStartTimeDesc(JOB_NAME, JOB_GROUP);
            
            log.info("📊 Found {} total job executions in history", executions.size());
            
            // Convert to DTOs
            List<JobExecutionDto> executionDtos = executions.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            // Apply pagination manually (since we're not using Spring Data paging here)
            int start = page * size;
            int end = Math.min(start + size, executionDtos.size());
            
            List<JobExecutionDto> paginatedResults = executionDtos.subList(start, end);
            
            SchedulerHistoryResponse response = new SchedulerHistoryResponse();
            response.setExecutions(paginatedResults);
            response.setTotalCount(executions.size());
            response.setPage(page);
            response.setSize(size);
            
            log.info("📊 Returning {} executions (page {} of total {})", 
                    paginatedResults.size(), page, executions.size());
            
            return response;
            
        } catch (Exception e) {
            log.error("❌ Error getting job history", e);
            
            // Return empty response instead of throwing exception
            SchedulerHistoryResponse emptyResponse = new SchedulerHistoryResponse();
            emptyResponse.setExecutions(List.of());
            emptyResponse.setTotalCount(0);
            emptyResponse.setPage(page);
            emptyResponse.setSize(size);
            return emptyResponse;
        }
    }

    // Stub methods for job execution recording
    public LocalDateTime getProcessingStartTime(String jobName) {
    try {
        // Get the last successful execution
        List<JobExecutionHistoryEntity> executions = jobHistoryRepository
            .findByJobNameAndJobGroupOrderByStartTimeDesc(jobName, JOB_GROUP);
        
        // Find the last successful execution with a processToTime
        Optional<JobExecutionHistoryEntity> lastSuccessful = executions.stream()
            .filter(exec -> exec.getStatus() == JobExecutionHistoryEntity.ExecutionStatus.COMPLETED)
            .filter(exec -> exec.getProcessToTime() != null)
            .findFirst();
        
        if (lastSuccessful.isPresent()) {
            LocalDateTime lastProcessedTo = lastSuccessful.get().getProcessToTime();
            log.info("📅 Using last successful run end time: {}", lastProcessedTo);
            return lastProcessedTo;
        }
        
        // Get from config or default to 30 days ago
        SchedulerConfigEntity config = getSchedulerConfig();
        LocalDateTime startFromTime = config.getStartFromTime();
        
        if (startFromTime != null) {
            log.info("📅 Using configured start time: {}", startFromTime);
            return startFromTime;
        }
        
        // Default fallback
        LocalDateTime defaultStart = LocalDateTime.now().minusDays(30);
        log.info("📅 Using default start time (30 days ago): {}", defaultStart);
        return defaultStart;
        
    } catch (Exception e) {
        log.error("❌ Error getting processing start time, using default", e);
        return LocalDateTime.now().minusDays(30);
    }
}

    @Transactional
    public Long recordJobStart(String jobName, String jobGroup, String triggerName,
                            String triggerGroup, LocalDateTime startTime) {
        try {
            log.info("📝 Recording job start: {} in group {}", jobName, jobGroup);
            
            JobExecutionHistoryEntity execution = new JobExecutionHistoryEntity();
            execution.setJobName(jobName);
            execution.setJobGroup(jobGroup);
            execution.setTriggerName(triggerName);
            execution.setTriggerGroup(triggerGroup);
            execution.setStartTime(startTime);
            execution.setStatus(JobExecutionHistoryEntity.ExecutionStatus.STARTED);
            
            JobExecutionHistoryEntity saved = jobHistoryRepository.save(execution);
            
            log.info("✅ Recorded job start with execution ID: {}", saved.getId());
            return saved.getId();
            
        } catch (Exception e) {
            log.error("❌ Error recording job start for {}: {}", jobName, e.getMessage(), e);
            // Return null to indicate failure, but don't crash the job
            return null;
        }
    }

        public boolean isJobCurrentlyRunning(String jobName, String jobGroup) {
        try {
            // Check database for STARTED jobs without end time
            List<JobExecutionHistoryEntity> runningJobs = jobHistoryRepository
                .findByJobNameAndJobGroupAndStatus(jobName, jobGroup, 
                    JobExecutionHistoryEntity.ExecutionStatus.STARTED);
            
            // Filter for jobs that started recently (within last hour) and have no end time
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            
            long activeJobs = runningJobs.stream()
                .filter(job -> job.getEndTime() == null) // No end time means still running
                .filter(job -> job.getStartTime().isAfter(oneHourAgo)) // Started recently
                .count();
            
            if (activeJobs > 0) {
                log.warn("⚠️ Found {} active job(s) for {}/{}", activeJobs, jobName, jobGroup);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("❌ Error checking for running jobs: {}", e.getMessage());
            return false;
        }
    }

    @Transactional
    public void cleanupStaleJobs() {
        try {
            LocalDateTime staleThreshold = LocalDateTime.now().minusHours(2);
            
            List<JobExecutionHistoryEntity> staleJobs = jobHistoryRepository
                .findByStatusAndStartTimeBefore(
                    JobExecutionHistoryEntity.ExecutionStatus.STARTED, 
                    staleThreshold);
            
            for (JobExecutionHistoryEntity staleJob : staleJobs) {
                log.warn("🧹 Marking stale job as failed: ID={}, started={}", 
                        staleJob.getId(), staleJob.getStartTime());
                
                staleJob.setStatus(JobExecutionHistoryEntity.ExecutionStatus.FAILED);
                staleJob.setEndTime(LocalDateTime.now());
                staleJob.setErrorMessage("Job marked as failed due to stale status");
                
                if (staleJob.getStartTime() != null) {
                    long durationMs = java.time.Duration.between(staleJob.getStartTime(), LocalDateTime.now()).toMillis();
                    staleJob.setDurationMs(durationMs);
                }
                
                jobHistoryRepository.save(staleJob);
            }
            
            if (!staleJobs.isEmpty()) {
                log.info("🧹 Cleaned up {} stale job records", staleJobs.size());
            }
            
        } catch (Exception e) {
            log.error("❌ Error cleaning up stale jobs: {}", e.getMessage());
        }
    }

    @Transactional
    public void recordJobCompletion(Long executionId, LocalDateTime endTime,
                                long recordsProcessed, LocalDateTime processFromTime,
                                LocalDateTime processToTime) {
        if (executionId == null) {
            log.warn("⚠️ Cannot record job completion - executionId is null");
            return;
        }
        
        try {
            log.info("📝 Recording job completion for execution ID: {}", executionId);
            
            Optional<JobExecutionHistoryEntity> executionOpt = jobHistoryRepository.findById(executionId);
            if (executionOpt.isEmpty()) {
                log.error("❌ Execution record not found for ID: {}", executionId);
                return;
            }
            
            JobExecutionHistoryEntity execution = executionOpt.get();
            execution.setEndTime(endTime);
            execution.setStatus(JobExecutionHistoryEntity.ExecutionStatus.COMPLETED);
            execution.setRecordsProcessed(recordsProcessed);
            execution.setProcessFromTime(processFromTime);
            execution.setProcessToTime(processToTime);
            
            // Calculate duration
            if (execution.getStartTime() != null) {
                long durationMs = java.time.Duration.between(execution.getStartTime(), endTime).toMillis();
                execution.setDurationMs(durationMs);
            }
            
            jobHistoryRepository.save(execution);
            
            log.info("✅ Recorded job completion: {} records processed in {}ms", 
                    recordsProcessed, execution.getDurationMs());
            
        } catch (Exception e) {
            log.error("❌ Error recording job completion for execution {}: {}", executionId, e.getMessage(), e);
        }
    }


    @Transactional
    public void recordJobFailure(Long executionId, LocalDateTime endTime, String errorMessage) {
        if (executionId == null) {
            log.warn("⚠️ Cannot record job failure - executionId is null");
            return;
        }
        
        try {
            log.info("📝 Recording job failure for execution ID: {}", executionId);
            
            Optional<JobExecutionHistoryEntity> executionOpt = jobHistoryRepository.findById(executionId);
            if (executionOpt.isEmpty()) {
                log.error("❌ Execution record not found for ID: {}", executionId);
                return;
            }
            
            JobExecutionHistoryEntity execution = executionOpt.get();
            execution.setEndTime(endTime);
            execution.setStatus(JobExecutionHistoryEntity.ExecutionStatus.FAILED);
            execution.setErrorMessage(errorMessage);
            
            // Calculate duration
            if (execution.getStartTime() != null) {
                long durationMs = java.time.Duration.between(execution.getStartTime(), endTime).toMillis();
                execution.setDurationMs(durationMs);
            }
            
            jobHistoryRepository.save(execution);
            
            log.info("❌ Recorded job failure after {}ms: {}", execution.getDurationMs(), errorMessage);
            
        } catch (Exception e) {
            log.error("❌ Error recording job failure for execution {}: {}", executionId, e.getMessage(), e);
        }
    }


    @Transactional
    public void updateLastRunTime(String jobName, LocalDateTime lastRunTime) {
        try {
            log.info("📝 Updating last run time for {}: {}", jobName, lastRunTime);
            
            SchedulerConfigEntity config = getSchedulerConfig();
            config.setLastRunTime(lastRunTime);
            schedulerConfigRepository.save(config);
            
            log.info("✅ Updated last run time to: {}", lastRunTime);
            
        } catch (Exception e) {
            log.error("❌ Error updating last run time for {}: {}", jobName, e.getMessage(), e);
        }
    }


        private JobExecutionDto convertToDto(JobExecutionHistoryEntity entity) {
        JobExecutionDto dto = new JobExecutionDto();
        dto.setId(entity.getId());
        dto.setJobName(entity.getJobName());
        dto.setJobGroup(entity.getJobGroup());
        dto.setTriggerName(entity.getTriggerName());
        dto.setTriggerGroup(entity.getTriggerGroup());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setStatus(entity.getStatus());
        dto.setErrorMessage(entity.getErrorMessage());
        dto.setRecordsProcessed(entity.getRecordsProcessed());
        dto.setDurationMs(entity.getDurationMs());
        dto.setProcessFromTime(entity.getProcessFromTime());
        dto.setProcessToTime(entity.getProcessToTime());
        return dto;
        }
}