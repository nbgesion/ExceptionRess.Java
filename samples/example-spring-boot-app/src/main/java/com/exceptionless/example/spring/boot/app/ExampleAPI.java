package com.exceptionress.example.spring.boot.app;

import com.exceptionress.exceptionressclient.exceptionressClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ExampleAPI {
  public static void main(String[] args) {
    SpringApplication.run(ExampleAPI.class, args);
  }

  @Bean
  public exceptionressClient exceptionressClient() {
    return exceptionressClient.from(
        System.getenv("exceptionress_SAMPLE_APP_API_KEY"),
        System.getenv("exceptionress_SAMPLE_APP_SERVER_URL"));
  }
}
