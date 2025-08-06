package boot.data.jobhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "boot.data")
@EnableJpaRepositories("boot.data.repository") 
@EntityScan("boot.data.entity")
// @ComponentScan("boot.data.*")
public class JobhubApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobhubApplication.class, args);
	}

}
