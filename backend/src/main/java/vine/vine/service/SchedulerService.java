package vine.vine.service;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
            log.info("🔧 Initializing scheduler configuration - MANUAL START ONLY...");

            if (schedulerConfigRepository == null) {
                log.error("SchedulerConfigRepository is null!");
                return;
            }

            // ✅ ALWAYS CREATE/RESET CONFIG AS DISABLED ON STARTUP
            Optional<SchedulerConfigEntity> existingConfig = schedulerConfigRepository.findById(CONFIG_NAME);
            SchedulerConfigEntity config;
            
            if (existingConfig.isPresent()) {
                config = existingConfig.get();
                log.info("🔄 Resetting existing scheduler configuration to DISABLED state");
            } else {
                log.info("🆕 Creating new scheduler configuration in DISABLED state");
                config = new SchedulerConfigEntity();
                config.setConfigName(CONFIG_NAME);
            }
            
            // ✅ FORCE DISABLED STATE ON STARTUP
            config.setEnabled(false);
            config.setIntervalMinutes(30); // Default interval
            if (config.getStartFromTime() == null) {
                config.setStartFromTime(LocalDateTime.now().minusDays(30));
            }
            config.setLastStopTime(LocalDateTime.now()); // Mark as stopped on startup
            config.setNextRunTime(null); // Clear any scheduled runs
            
            schedulerConfigRepository.save(config);
            log.info("✅ Scheduler configuration saved in DISABLED state");
            
            // ✅ START QUARTZ SCHEDULER BUT WITH NO ACTIVE TRIGGERS
            if (scheduler != null && !scheduler.isStarted()) {
                log.info("🔄 Starting Quartz scheduler framework (without active jobs)...");
                scheduler.start();
                log.info("✅ Quartz scheduler framework started - waiting for manual trigger");
            }
            
            // ✅ REMOVE ANY EXISTING TRIGGERS FROM PREVIOUS SESSIONS
            if (scheduler != null && scheduler.checkExists(TriggerKey.triggerKey(TRIGGER_NAME, TRIGGER_GROUP))) {
                log.info("🧹 Removing any existing triggers from previous session...");
                scheduler.unscheduleJob(TriggerKey.triggerKey(TRIGGER_NAME, TRIGGER_GROUP));
                log.info("✅ Previous triggers cleaned up");
            }
            
            // ✅ ENSURE JOB IS REGISTERED BUT NOT SCHEDULED
            ensureJobIsRegistered();
            
            log.info("✅ Scheduler initialization complete - Ready for MANUAL START via frontend");
            
        } catch (Exception e) {
            log.error("❌ Error initializing scheduler configuration", e);
        }
    }

    private void ensureJobIsRegistered() {
        try {
            if (scheduler == null) {
                log.warn("⚠️ Scheduler not available for job registration");
                return;
            }

            JobKey jobKey = new JobKey(JOB_NAME, JOB_GROUP);

            if (!scheduler.checkExists(jobKey)) {
                log.info("📝 Registering job definition (without scheduling)...");

                JobDetail jobDetail = bookingProcessorJobDetail;
                if (jobDetail == null) {
                    log.info("🔧 Creating JobDetail manually");
                    jobDetail = JobBuilder.newJob(BookingProcessorJob.class)
                            .withIdentity(JOB_NAME, JOB_GROUP)
                            .withDescription("Job to process bookings - Manual start only")
                            .storeDurably(true) // ✅ Important: allows job to exist without trigger
                            .requestRecovery(false) // ✅ Don't auto-recover on startup
                            .build();
                }

                scheduler.addJob(jobDetail, true);
                log.info("✅ Job registered successfully: {} (not scheduled)", JOB_NAME);
            } else {
                log.info("✅ Job already exists: {} (checking if scheduled...)", JOB_NAME);
                
                // ✅ DOUBLE CHECK: Remove any triggers even if job exists
                if (scheduler.checkExists(TriggerKey.triggerKey(TRIGGER_NAME, TRIGGER_GROUP))) {
                    scheduler.unscheduleJob(TriggerKey.triggerKey(TRIGGER_NAME, TRIGGER_GROUP));
                    log.info("🧹 Removed existing trigger for job");
                }
            }
        } catch (SchedulerException e) {
            log.error("❌ Error ensuring job is registered", e);
        }
    }

    @Transactional
    public SchedulerStatusDto startScheduler(int intervalMinutes) {
        try {
            log.info("🚀 MANUAL START requested with interval: {} minutes", intervalMinutes);
            
            // ✅ Validate interval
            if (intervalMinutes < 1) {
                throw new IllegalArgumentException("Interval must be at least 1 minute");
            }
            
            // ✅ Check if already running
            if (isSchedulerRunning()) {
                log.warn("⚠️ Scheduler is already running");
                return getSchedulerStatus();
            }
            
            // ✅ Ensure Quartz is started
            if (scheduler != null && !scheduler.isStarted()) {
                log.info("🔄 Starting Quartz scheduler framework...");
                scheduler.start();
            }
            
            // ✅ Ensure job is registered
            ensureJobIsRegistered();
            
            // ✅ Create and schedule trigger
            Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(TRIGGER_NAME, TRIGGER_GROUP)
                .forJob(JOB_NAME, JOB_GROUP)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInMinutes(intervalMinutes)
                    .repeatForever())
                .startNow() // ✅ Start immediately when manually triggered
                .build();

            if (scheduler != null) {
                scheduler.scheduleJob(trigger);
                log.info("✅ Trigger scheduled successfully");
            }
            
            // ✅ Update configuration
            SchedulerConfigEntity config = getSchedulerConfig();
            config.setEnabled(true);
            config.setIntervalMinutes(intervalMinutes);
            config.setLastStartTime(LocalDateTime.now());
            config.setLastStopTime(null); // Clear stop time when starting
            
            if (trigger.getNextFireTime() != null) {
                config.setNextRunTime(trigger.getNextFireTime().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
            
            schedulerConfigRepository.save(config);
            
            log.info("✅ Scheduler started successfully with {} minute intervals", intervalMinutes);
            log.info("📅 Next execution scheduled for: {}", config.getNextRunTime());
            
            return getSchedulerStatus();
            
        } catch (Exception e) {
            log.error("❌ Error starting scheduler", e);
            throw new RuntimeException("Failed to start scheduler: " + e.getMessage());
        }
    }

    @Transactional
    public SchedulerStatusDto stopScheduler() {
        try {
            log.info("🛑 MANUAL STOP requested");
            
            // ✅ Remove the trigger to stop scheduled executions
            if (scheduler != null && scheduler.checkExists(TriggerKey.triggerKey(TRIGGER_NAME, TRIGGER_GROUP))) {
                scheduler.unscheduleJob(TriggerKey.triggerKey(TRIGGER_NAME, TRIGGER_GROUP));
                log.info("✅ Trigger removed - no more scheduled executions");
            } else {
                log.info("ℹ️ No active trigger found to remove");
            }
            
            // ✅ Update configuration
            SchedulerConfigEntity config = getSchedulerConfig();
            config.setEnabled(false);
            config.setLastStopTime(LocalDateTime.now());
            config.setNextRunTime(null); // Clear next run time
            schedulerConfigRepository.save(config);
            
            log.info("✅ Scheduler stopped successfully");
            
            return getSchedulerStatus();
            
        } catch (Exception e) {
            log.error("❌ Error stopping scheduler", e);
            throw new RuntimeException("Failed to stop scheduler: " + e.getMessage());
        }
    }

    public SchedulerStatusDto getSchedulerStatus() {
        try {
            boolean isRunning = isSchedulerRunning();
            SchedulerConfigEntity config = getSchedulerConfig();
            
            SchedulerStatusDto status = new SchedulerStatusDto();
            
            // ✅ Basic job information
            status.setJobName(JOB_NAME);
            status.setJobGroup(JOB_GROUP);
            status.setEnabled(config.isEnabled());
            status.setRunning(isRunning);
            status.setStatus(isRunning ? "RUNNING" : "STOPPED");
            
            // ✅ Configuration fields
            status.setIntervalMinutes(config.getIntervalMinutes());
            status.setLastStartTime(config.getLastStartTime());
            status.setLastStopTime(config.getLastStopTime());
            status.setLastRunTime(config.getLastRunTime());
            status.setNextRunTime(config.getNextRunTime());
            status.setStartFromTime(config.getStartFromTime());
            
            // ✅ Trigger information (only if running)
            if (isRunning && scheduler != null) {
                try {
                    Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey(TRIGGER_NAME, TRIGGER_GROUP));
                    if (trigger != null) {
                        if (trigger.getNextFireTime() != null) {
                            status.setNextFireTime(trigger.getNextFireTime().toInstant()
                                .atZone(ZoneId.systemDefault()).toLocalDateTime());
                        }
                        
                        Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                        status.setTriggerState(triggerState.name());
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
            
            // ✅ Execution statistics
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
                
                // Get last error
                allExecutions.stream()
                    .filter(exec -> exec.getStatus() == JobExecutionHistoryEntity.ExecutionStatus.FAILED)
                    .findFirst()
                    .ifPresent(exec -> status.setLastErrorMessage(exec.getErrorMessage()));
                
            } catch (Exception e) {
                log.debug("Could not get execution statistics: {}", e.getMessage());
                status.setTotalExecutions(0);
                status.setSuccessfulExecutions(0);
                status.setFailedExecutions(0);
            }
            
            return status;
            
        } catch (Exception e) {
            log.error("❌ Error getting scheduler status", e);
            
            SchedulerStatusDto errorStatus = new SchedulerStatusDto();
            errorStatus.setJobName(JOB_NAME);
            errorStatus.setJobGroup(JOB_GROUP);
            errorStatus.setEnabled(false);
            errorStatus.setRunning(false);
            errorStatus.setStatus("ERROR");
            errorStatus.setTriggerState("ERROR");
            errorStatus.setIntervalMinutes(30);
            errorStatus.setTotalExecutions(0);
            errorStatus.setSuccessfulExecutions(0);
            errorStatus.setFailedExecutions(0);
            return errorStatus;
        }
    }

    @Transactional
    public void triggerJobNow() {
        try {
            log.info("🎯 MANUAL JOB TRIGGER requested");

            if (scheduler == null) {
                throw new RuntimeException("Scheduler not available");
            }

            ensureJobIsRegistered();

            JobKey jobKey = new JobKey(JOB_NAME, JOB_GROUP);
            if (scheduler.checkExists(jobKey)) {
                scheduler.triggerJob(jobKey);
                log.info("✅ Job triggered manually: {}", JOB_NAME);
            } else {
                log.error("❌ Job does not exist: {}", JOB_NAME);
                throw new RuntimeException("Job does not exist: " + JOB_NAME);
            }
        } catch (Exception e) {
            log.error("❌ Error triggering job manually", e);
            throw new RuntimeException("Failed to trigger job manually: " + e.getMessage(), e);
        }
    }

    public boolean isSchedulerRunning() {
        try {
            return scheduler != null && 
                   scheduler.isStarted() && 
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

    public SchedulerStatusDto updateSchedulerConfig(boolean enabled, int intervalMinutes, LocalDateTime startFromTime) {
        try {
            if (intervalMinutes < 1) {
                throw new IllegalArgumentException("Interval minutes must be at least 1");
            }

            SchedulerConfigEntity config = getSchedulerConfig();
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
            
            List<JobExecutionHistoryEntity> executions;
            
            if (days != null && days > 0) {
                LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
                Pageable pageable = PageRequest.of(page, size);
                Page<JobExecutionHistoryEntity> pagedExecutions = jobHistoryRepository
                        .findByStartTimeAfter(cutoff, pageable);
                
                executions = pagedExecutions.getContent();
                
                List<JobExecutionDto> executionDtos = executions.stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
                
                SchedulerHistoryResponse response = new SchedulerHistoryResponse();
                response.setExecutions(executionDtos);
                response.setTotalCount(pagedExecutions.getTotalElements());
                response.setPage(page);
                response.setSize(size);
                
                return response;
            } else {
                executions = jobHistoryRepository
                        .findByJobNameAndJobGroupOrderByStartTimeDesc(JOB_NAME, JOB_GROUP);
                
                List<JobExecutionDto> executionDtos = executions.stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
                
                int start = page * size;
                int end = Math.min(start + size, executionDtos.size());
                
                List<JobExecutionDto> paginatedResults = executionDtos.subList(start, end);
                
                SchedulerHistoryResponse response = new SchedulerHistoryResponse();
                response.setExecutions(paginatedResults);
                response.setTotalCount(executions.size());
                response.setPage(page);
                response.setSize(size);
                
                return response;
            }
            
        } catch (Exception e) {
            log.error("❌ Error getting job history", e);
            
            SchedulerHistoryResponse emptyResponse = new SchedulerHistoryResponse();
            emptyResponse.setExecutions(List.of());
            emptyResponse.setTotalCount(0);
            emptyResponse.setPage(page);
            emptyResponse.setSize(size);
            return emptyResponse;
        }
    }

    // ✅ Job execution recording methods (unchanged)
    public LocalDateTime getProcessingStartTime(String jobName) {
        try {
            List<JobExecutionHistoryEntity> executions = jobHistoryRepository
                .findByJobNameAndJobGroupOrderByStartTimeDesc(jobName, JOB_GROUP);
            
            Optional<JobExecutionHistoryEntity> lastSuccessful = executions.stream()
                .filter(exec -> exec.getStatus() == JobExecutionHistoryEntity.ExecutionStatus.COMPLETED)
                .filter(exec -> exec.getProcessToTime() != null)
                .findFirst();
            
            if (lastSuccessful.isPresent()) {
                LocalDateTime lastProcessedTo = lastSuccessful.get().getProcessToTime();
                log.info("📅 Using last successful run end time: {}", lastProcessedTo);
                return lastProcessedTo;
            }
            
            SchedulerConfigEntity config = getSchedulerConfig();
            LocalDateTime startFromTime = config.getStartFromTime();
            
            if (startFromTime != null) {
                log.info("📅 Using configured start time: {}", startFromTime);
                return startFromTime;
            }
            
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
            return null;
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
            
            if (execution.getStartTime() != null) {
                long durationMs = java.time.Duration.between(execution.getStartTime(), endTime).toMillis();
                execution.setDurationMs(durationMs);
            }
            
            jobHistoryRepository.save(execution);
            
            log.info("✅ Recorded job completion: {} records processed", recordsProcessed);
            
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
            Optional<JobExecutionHistoryEntity> executionOpt = jobHistoryRepository.findById(executionId);
            if (executionOpt.isEmpty()) {
                log.error("❌ Execution record not found for ID: {}", executionId);
                return;
            }
            
            JobExecutionHistoryEntity execution = executionOpt.get();
            execution.setEndTime(endTime);
            execution.setStatus(JobExecutionHistoryEntity.ExecutionStatus.FAILED);
            execution.setErrorMessage(errorMessage);
            
            if (execution.getStartTime() != null) {
                long durationMs = java.time.Duration.between(execution.getStartTime(), endTime).toMillis();
                execution.setDurationMs(durationMs);
            }
            
            jobHistoryRepository.save(execution);
            
            log.info("❌ Recorded job failure: {}", errorMessage);
            
        } catch (Exception e) {
            log.error("❌ Error recording job failure for execution {}: {}", executionId, e.getMessage(), e);
        }
    }

    @Transactional
    public void updateLastRunTime(String jobName, LocalDateTime lastRunTime) {
        try {
            SchedulerConfigEntity config = getSchedulerConfig();
            config.setLastRunTime(lastRunTime);
            schedulerConfigRepository.save(config);
            
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