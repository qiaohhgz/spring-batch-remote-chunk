package com.example.springbatchremotechunk;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableBatchIntegration
@EnableBatchProcessing
@SpringBootApplication
public class SpringBatchRemoteChunkApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(SpringBatchRemoteChunkApplication.class, args);
    }

}
