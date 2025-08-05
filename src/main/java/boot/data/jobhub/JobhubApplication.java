package boot.data.jobhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableJpaRepositories("boot.data.*") 
@EntityScan("boot.data.*")
@ComponentScan("boot.data.*")

public class JobhubApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobhubApplication.class, args);
	}

}
