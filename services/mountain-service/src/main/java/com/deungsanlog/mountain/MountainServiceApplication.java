package com.deungsanlog.mountain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableFeignClients(basePackages = "com.deungsanlog.mountain.client")
public class MountainServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MountainServiceApplication.class, args);
    }

}
