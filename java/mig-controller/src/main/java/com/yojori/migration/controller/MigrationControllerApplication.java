package com.yojori.migration.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import com.yojori.db.DBManager;

@SpringBootApplication
@ComponentScan(basePackages = "com.yojori")
@Import(DBManager.class)
public class MigrationControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MigrationControllerApplication.class, args);
    }

}
