package com.deungsanlog.record;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.deungsanlog.record.client")
@SpringBootApplication
public class RecordServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecordServiceApplication.class, args);
	}

}
