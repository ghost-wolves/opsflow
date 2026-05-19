package com.opsflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OpsflowApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpsflowApiApplication.class, args);
    }
}
