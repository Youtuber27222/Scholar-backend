package com.scholarmatch.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ScholarMatchApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(ScholarMatchApiApplication.class, args);
  }
}
