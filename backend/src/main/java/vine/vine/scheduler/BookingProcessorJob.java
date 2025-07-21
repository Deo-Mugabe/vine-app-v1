package vine.vine.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import vine.vine.service.Impl.ChargesServiceImpl;
import vine.vine.service.SchedulerService;

import java.time.LocalDateTime;

@Slf4j
@Component
public class BookingProcessorJob implements Job {

    @Autowired
    private ChargesServiceImpl chargesService;
    
    @Autowired
    private SchedulerService schedulerService;

    private static final String JOB_NAME = "bookingProcessorJob";
    private static final String JOB_GROUP = "vine-group";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long executionId = null;
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            log.info("Starting Quartz booking processor job execution");
            
            // Record job start
            executionId = schedulerService.recordJobStart(
                JOB_NAME, 
                JOB_GROUP, 
                context.getTrigger().getKey().getName(),
                context.getTrigger().getKey().getGroup(),
                startTime
            );

            // Get the processing start time
            LocalDateTime processFromTime = schedulerService.getProcessingStartTime(JOB_NAME);
            
            // Process bookings
            long recordsProcessed = chargesService.processBookings(processFromTime);
            
            // Record successful completion
            schedulerService.recordJobCompletion(
                executionId, 
                LocalDateTime.now(), 
                recordsProcessed, 
                processFromTime, 
                LocalDateTime.now()
            );
            
            // Update last successful run time
            schedulerService.updateLastRunTime(JOB_NAME, LocalDateTime.now());
            
            log.info("Completed Quartz booking processor job execution. Processed {} records", recordsProcessed);
            
        } catch (Exception ex) {
            log.error("Error during Quartz booking processor job execution", ex);
            
            if (executionId != null) {
                schedulerService.recordJobFailure(executionId, LocalDateTime.now(), ex.getMessage());
            }
            
            throw new JobExecutionException("Job execution failed", ex);
        }
    }
}