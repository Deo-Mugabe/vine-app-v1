package vine.vine.config;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import vine.vine.scheduler.BookingProcessorJob;

@Configuration
public class QuartzConfig {

    @Autowired
    private AutoWiringSpringBeanJobFactory springBeanJobFactory;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(springBeanJobFactory);
        factory.setStartupDelay(10); // Start after 10 seconds
        factory.setOverwriteExistingJobs(true);
        return factory;
    }

    @Bean
    public JobDetail bookingProcessorJobDetail() {
        return JobBuilder.newJob(BookingProcessorJob.class)
                .withIdentity("bookingProcessorJob", "vine-group")
                .withDescription("Job to process bookings")
                .storeDurably(true)
                .build();
    }

    @Bean
    public Trigger bookingProcessorTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(bookingProcessorJobDetail())
                .withIdentity("bookingProcessorTrigger", "vine-group")
                .withDescription("Trigger for booking processor job")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(30) // Default interval
                        .repeatForever())
                .build();
    }
}