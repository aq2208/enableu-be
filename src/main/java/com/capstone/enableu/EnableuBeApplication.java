package com.capstone.enableu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class EnableuBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnableuBeApplication.class, args);
	}

}
