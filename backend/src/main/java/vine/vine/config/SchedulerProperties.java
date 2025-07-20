package vine.vine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix="vine.scheduler")
@Data
public class SchedulerProperties {
    private boolean enabled;
    private int fixedDelayMinutes;

    public long getTimerInMillis() {
        return fixedDelayMinutes * 60L * 1000L;
    }
}