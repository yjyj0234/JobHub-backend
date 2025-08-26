package boot.data.jobhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan(basePackages = {"boot.data.controller", "boot.data.service", "boot.data.config", "boot.data.jobhub", "boot.data.jwt", "boot.data.security"})
@EntityScan(basePackages = "boot.data.entity")
@EnableJpaRepositories(basePackages = "boot.data.repository")
@SpringBootApplication
public class JobhubApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobhubApplication.class, args);
    }
}