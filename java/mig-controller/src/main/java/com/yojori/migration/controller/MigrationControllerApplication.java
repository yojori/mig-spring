package com.yojori.migration.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.yojori")
public class MigrationControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MigrationControllerApplication.class, args);
    }

}
