package com.dusty.sandoval;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SandovalApplication {

	public static void main(String[] args) {
		SpringApplication.run(SandovalApplication.class, args);
	}

}
