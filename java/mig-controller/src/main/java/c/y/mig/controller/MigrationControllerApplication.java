package c.y.mig.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "c.y.mig")
public class MigrationControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MigrationControllerApplication.class, args);
    }

}
