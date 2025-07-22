package vine.vine.config;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import vine.vine.scheduler.BookingProcessorJob;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class QuartzConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AutoWiringSpringBeanJobFactory springBeanJobFactory;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(springBeanJobFactory);
        factory.setDataSource(dataSource);
        factory.setQuartzProperties(quartzProperties());
        factory.setStartupDelay(10); // Start after 10 seconds
        factory.setOverwriteExistingJobs(true);
        factory.setAutoStartup(true); // ✅ Keep this true - starts Quartz framework only
        
        // ✅ IMPORTANT: Register job definition but DON'T schedule it automatically
        factory.setJobDetails(bookingProcessorJobDetail());
        
        // ✅ DO NOT SET TRIGGERS HERE - this prevents auto-execution
        // factory.setTriggers(...); // ❌ REMOVED - no auto triggers
        
        return factory;
    }

    @Bean
    public Properties quartzProperties() {
        Properties properties = new Properties();
        
        // Scheduler properties
        properties.setProperty("org.quartz.scheduler.instanceName", "VineScheduler");
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        properties.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");
        
        // ThreadPool properties
        properties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        properties.setProperty("org.quartz.threadPool.threadCount", "10");
        properties.setProperty("org.quartz.threadPool.threadPriority", "5");
        
        // JobStore properties - Use binary storage (default for SQL Server)
        properties.setProperty("org.quartz.jobStore.class", "org.springframework.scheduling.quartz.LocalDataSourceJobStore");
        properties.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.MSSQLDelegate");
        properties.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
        properties.setProperty("org.quartz.jobStore.isClustered", "false");
        properties.setProperty("org.quartz.jobStore.useProperties", "false"); // Use binary storage
        
        // SQL Server specific properties to fix autocommit issues
        properties.setProperty("org.quartz.jobStore.dontSetAutoCommitFalse", "false");
        properties.setProperty("org.quartz.jobStore.dontSetNonManagedTXConnectionAutoCommitFalse", "false");
        properties.setProperty("org.quartz.jobStore.selectWithLockSQL", "SELECT * FROM {0}LOCKS WITH (UPDLOCK,ROWLOCK) WHERE SCHED_NAME = {1} AND LOCK_NAME = ?");
        
        return properties;
    }

    @Bean
    public JobDetail bookingProcessorJobDetail() {
        return JobBuilder.newJob(BookingProcessorJob.class)
                .withIdentity("bookingProcessorJob", "vine-group")
                .withDescription("Job to process bookings - MANUAL START ONLY")
                .storeDurably(true) // ✅ CRITICAL: Job exists without trigger
                .requestRecovery(false) // ✅ CRITICAL: Don't auto-recover on startup
                .build();
    }
    
    // ✅ NO TRIGGER BEANS HERE - prevents auto-execution
    // If you had any @Bean methods creating triggers, remove them
}