package vine.vine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "vine.batch")
@Data
public class BatchProcessingConfig {
    
    private int batchSize = 1000;
    private int bufferSize = 8192;
    private boolean enableMemoryMonitoring = true;
    private int memoryThresholdMB = 500;
    private int maxRetries = 3;
    
    @Bean
    public TaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("batch-");
        executor.initialize();
        return executor;
    }
}