package vine.vine.service.Impl;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vine.vine.config.SchedulerProperties;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingProcessorScheduler {

    private final ChargesServiceImpl chargesService;
    private final SchedulerProperties schedulerProperties;

    // Store last successful run time in memory
    private LocalDateTime lastSuccessfulRunTime = LocalDateTime.now().minusDays(1); // default

    @Scheduled(fixedDelayString = "#{@schedulerProperties.timerInMillis}")
    public void runProcessor() {
        if (!schedulerProperties.isEnabled()) return;

        try {
            log.info("Starting booking processing...");
            chargesService.processBookings(lastSuccessfulRunTime);
            lastSuccessfulRunTime = LocalDateTime.now();
            log.info("Finished booking processing.");
        } catch (Exception ex) {
            log.error("Error during booking processing", ex);
        }
    }
}