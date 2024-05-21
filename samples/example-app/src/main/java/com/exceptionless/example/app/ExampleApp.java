package com.exceptionress.example.app;

import com.exceptionress.exceptionressclient.exceptionressClient;
import com.exceptionress.exceptionressclient.models.EventPluginContext;

public class ExampleApp {
  private static final exceptionressClient client =
      exceptionressClient.from(
          System.getenv("exceptionress_SAMPLE_APP_API_KEY"),
          System.getenv("exceptionress_SAMPLE_APP_SERVER_URL"));

  public static void sampleEventSubmissions() {
    client.submitException(new RuntimeException("Test exception"));
    client.submitUnhandledException(
        new RuntimeException("Test exception"), "Test submission method");
    client.submitFeatureUsage("Test feature");
    client.submitLog("Test log");
    client.submitNotFound("Test resource");
  }

  public static void sampleUseOfSessions() {
    client.getConfiguration().useSessions();
    client.submitEvent(client.createSessionStart().userIdentity("test-user").build());
    client.submitSessionEnd("test-user");
  }

  public static void sampleUseOfUpdatingEmailAndDescription() {
    client.submitEvent(client.createLog("test-log").referenceId("test-reference-id").build());
    client.updateEmailAndDescription("test-reference-id", "test@email.com", "test-description");
  }

  public static void main(String[] args) {
    sampleEventSubmissions();
    sampleUseOfUpdatingEmailAndDescription();
    sampleUseOfSessions();
  }
}
