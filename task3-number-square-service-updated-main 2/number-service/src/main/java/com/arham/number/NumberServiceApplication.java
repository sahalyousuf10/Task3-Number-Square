package com.arham.number;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class NumberServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NumberServiceApplication.class, args);
	}

}
