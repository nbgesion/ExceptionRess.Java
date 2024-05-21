package com.exceptionress.example.spring.boot.app.api;

import com.exceptionress.exceptionressclient.exceptionressClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class ExampleResource {
  private final exceptionressClient exceptionressClient;

  @Autowired
  public ExampleResource(exceptionressClient exceptionressClient) {
    this.exceptionressClient = exceptionressClient;
  }

  @PostMapping("/log")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void submitLog() {
    exceptionressClient.submitLog("test-log");
  }
}
