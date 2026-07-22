package com.mgt.governance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.mgt")
@EntityScan(basePackages = {"com.mgt"})
@EnableJpaRepositories(basePackages = "com.mgt.dao")
@EnableAsync
@EnableScheduling
public class GovernanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GovernanceApplication.class, args);
	}

}
