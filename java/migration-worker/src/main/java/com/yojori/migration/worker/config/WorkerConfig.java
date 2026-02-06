package com.yojori.migration.worker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WorkerConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
