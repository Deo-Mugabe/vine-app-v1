package vine.vine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import vine.vine.config.SchedulerProperties;

@EnableScheduling
@EnableConfigurationProperties(SchedulerProperties.class)
@SpringBootApplication
public class VineApplication {

	public static void main(String[] args) {
		SpringApplication.run(VineApplication.class, args);
	}

}
