package com.exceptionress.exceptionressclient;

import com.exceptionress.exceptionressclient.configuration.Configuration;
import com.exceptionress.exceptionressclient.enums.EventPropertyKey;
import com.exceptionress.exceptionressclient.enums.EventType;
import com.exceptionress.exceptionressclient.models.Event;
import com.exceptionress.exceptionressclient.models.UserDescription;
import com.exceptionress.exceptionressclient.queue.DefaultEventQueue;
import com.exceptionress.exceptionressclient.settings.DefaultSettingsClient;
import com.exceptionress.exceptionressclient.settings.ServerSettings;
import com.exceptionress.exceptionressclient.settings.SettingsResponse;
import com.exceptionress.exceptionressclient.storage.InMemoryStorage;
import com.exceptionress.exceptionressclient.storage.InMemoryStorageProvider;
import com.exceptionress.exceptionressclient.submission.DefaultSubmissionClient;
import com.exceptionress.exceptionressclient.submission.SubmissionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExceptionRessClientTest {
  @Mock private DefaultSettingsClient settingsClient;
  @Mock private DefaultSubmissionClient submissionClient;
  @Mock private InMemoryStorageProvider storageProvider;
  @Mock private DefaultEventQueue eventQueue;
  private Configuration configuration;
  private ExceptionRessClient client;
  private InMemoryStorage<ServerSettings> settingsStorage;

  @BeforeEach
  public void setup() {
    settingsStorage = InMemoryStorage.<ServerSettings>builder().build();
    configuration =
        TestFixtures.aDefaultConfigurationManager()
            // We don't want the timer to run by default
            .updateSettingsWhenIdleInterval(3600L)
            .settingsClient(settingsClient)
            .storageProvider(storageProvider)
            .submissionClient(submissionClient)
            .queue(eventQueue)
            .build();
    client = new ExceptionRessClient(configuration, 1000, 10);
  }

  @Test
  public void itCanSetupASettingsChangeTimer() throws InterruptedException {
    doReturn(settingsStorage).when(storageProvider).getSettings();
    settingsStorage.save(ServerSettings.builder().version(3L).build());
    doReturn(SettingsResponse.builder().body("test-message").code(400).build())
        .when(settingsClient)
        .getSettings(anyLong());

    Thread.sleep(1500);
    verify(settingsClient, times(1)).getSettings(3L);
  }

  @Test
  public void itCanSubmitAnExceptionEvent() {
    doReturn(settingsStorage).when(storageProvider).getSettings();

    client.submitException(new RuntimeException("test"));

    verify(eventQueue, times(1))
        .enqueue(
            argThat(
                event ->
                    event.getType().equals(EventType.ERROR.value())
                        && event.getReferenceId() != null
                        && event.getDate().toLocalDate().equals(LocalDate.now())
                        && event.getError().isPresent()
                        && event.getEnvironmentInfo().isPresent()
                        && event.getData().containsKey(EventPropertyKey.EXTRA.value())));
    assertThat(configuration.getLastReferenceIdManager().getLast()).isNotNull();
  }

  @Test
  public void itCanCreateAnExceptionEvent() {
    doReturn(settingsStorage).when(storageProvider).getSettings();

    Event event = client.createError().build();

    assertThat(event.getType()).isEqualTo(EventType.ERROR.value());
    assertThat(event.getDate()).isNotNull();
  }

  @Test
  public void itCanSubmitUnhandledExceptionEvent() {
    doReturn(settingsStorage).when(storageProvider).getSettings();

    client.submitUnhandledException(new RuntimeException("test"), "test-submission-method");

    verify(eventQueue, times(1))
        .enqueue(
            argThat(
                event ->
                    event.getType().equals(EventType.ERROR.value())
                        && event.getDate().toLocalDate().equals(LocalDate.now())
                        && event.getReferenceId() != null
                        && event.getError().isPresent()
                        && event.getEnvironmentInfo().isPresent()
                        && event.getData().containsKey(EventPropertyKey.EXTRA.value())
                        && event.getSubmissionMethod().isPresent()
                        && event.getSubmissionMethod().get().equals("test-submission-method")));
    assertThat(configuration.getLastReferenceIdManager().getLast()).isNotNull();
  }

  @Test
  public void itCanSubmitFeatureUsageEvent() {
    doReturn(settingsStorage).when(storageProvider).getSettings();

    client.submitFeatureUsage("test-feature");

    verify(eventQueue, times(1))
        .enqueue(
            argThat(
                event ->
                    event.getType().equals(EventType.USAGE.value())
                        && event.getSource().equals("test-feature")
                        && event.getReferenceId() != null
                        && event.getDate().toLocalDate().equals(LocalDate.now())
                        && event.getEnvironmentInfo().isPresent()));
    assertThat(configuration.getLastReferenceIdManager().getLast()).isNotNull();
  }

  @Test
  public void itCanCreateFeatureUsageEvent() {
    doReturn(settingsStorage).when(storageProvider).getSettings();

    Event event = client.createFeatureUsage("test-feature").build();

    assertThat(event.getType()).isEqualTo(EventType.USAGE.value());
    assertThat(event.getSource()).isEqualTo("test-feature");
    assertThat(event.getDate()).isNotNull();
  }

  @Test
  public void itCanSubmitALogEvent() {
    doReturn(settingsStorage).when(storageProvider).getSettings();

    client.submitLog("test-log-message", "test-log-source", "test-log-level");

    verify(eventQueue, times(1))
        .enqueue(
            argThat(
                event ->
                    event.getType().equals(EventType.LOG.value())
                        && event.getSource().equals("test-log-source")
                        && event.getMessage().equals("test-log-message")
                        && event.getDate().toLocalDate().equals(LocalDate.now())
                        && event.getReferenceId() != null
                        && event.getEnvironmentInfo().isPresent()
                        && event.getData().containsKey(EventPropertyKey.LOG_LEVEL.value())));
    assertThat(configuration.getLastReferenceIdManager().getLast()).isNotNull();
  }

  @Test
  public void itCanCreateALogEvent() {
    doReturn(settingsStorage).when(storageProvider).getSettings();

    Event event = client.createLog("test-message", "test-source", "test-level").build();

    assertThat(event.getType()).isEqualTo(EventType.LOG.value());
    assertThat(event.getSource()).isEqualTo("test-source");
    assertThat(event.getMessage()).isEqualTo("test-message");
    assertThat(event.getData().get(EventPropertyKey.LOG_LEVEL.value())).isEqualTo("test-level");
    assertThat(event.getDate()).isNotNull();
  }

  @Test
  public void itCanCreateALogEventWithoutLevel() {
    doReturn(settingsStorage).when(storageProvider).getSettings();

    Event event = client.createLog("test-message", "test-source").build();

    assertThat(event.getType()).isEqualTo(EventType.LOG.value());
    assertThat(event.getSource()).isEqualTo("test-source");
    assertThat(event.getMessage()).isEqualTo("test-message");
    assertThat(event.getDate()).isNotNull();
  }

  @Test
  public void itCanCreateALogEventWithoutLevelAndSourceUsingOverridedMethod() {
    doReturn(settingsStorage).when(storageProvider).getSettings();

    Event event = client.createLog("test-message").build();

    assertThat(event.getType()).isEqualTo(EventType.LOG.value());
    assertThat(event.getSource())
        .isEqualTo(
            "itCanCreateALogEventWithoutLevelAndSourceUsingOverridedMethod"); // should be equal to
    // the test method
    assertThat(event.getMessage()).isEqualTo("test-message");
    assertThat(event.getDate()).isNotNull();
  }

  @Test
  public void itCanCreateALogEventWithoutLevelAndSource() {
    doReturn(settingsStorage).when(storageProvider).getSettings();

    Event event = client.createLog("test-message", null, null).build();

    assertThat(event.getType()).isEqualTo(EventType.LOG.value());
    assertThat(event.getSource())
        .isEqualTo("itCanCreateALogEventWithoutLevelAndSource"); // should be equal to
    // the test method
    assertThat(event.getMessage()).isEqualTo("test-message");
    assertThat(event.getDate()).isNotNull();
  }

  @Test
  public void itCanSubmitANotFoundEvent() {
    doReturn(settingsStorage).when(storageProvider).getSettings();

    client.submitNotFound("test-resource");

    verify(eventQueue, times(1))
        .enqueue(
            argThat(
                event ->
                    event.getType().equals(EventType.NOT_FOUND.value())
                        && event.getSource().equals("test-resource")
                        && event.getDate().toLocalDate().equals(LocalDate.now())
                        && event.getReferenceId() != null
                        && event.getEnvironmentInfo().isPresent()));
    assertThat(configuration.getLastReferenceIdManager().getLast()).isNotNull();
  }

  @Test
  public void itCanCreateANotFoundEvent() {
    doReturn(settingsStorage).when(storageProvider).getSettings();

    Event event = client.createNotFound("test-resource").build();

    assertThat(event.getType()).isEqualTo(EventType.NOT_FOUND.value());
    assertThat(event.getSource()).isEqualTo("test-resource");
    assertThat(event.getDate()).isNotNull();
  }

  @Test
  public void itCanSubmitASessionStartEvent() {
    doReturn(settingsStorage).when(storageProvider).getSettings();

    client.submitSessionStart();

    verify(eventQueue, times(1))
        .enqueue(
            argThat(
                event ->
                    event.getType().equals(EventType.SESSION.value())
                        && event.getDate().toLocalDate().equals(LocalDate.now())
                        && event.getReferenceId() != null
                        && event.getEnvironmentInfo().isPresent()));
    assertThat(configuration.getLastReferenceIdManager().getLast()).isNotNull();
  }

  @Test
  public void itCanCreateASessionStartEvent() {
    doReturn(settingsStorage).when(storageProvider).getSettings();

    Event event = client.createSessionStart().build();

    assertThat(event.getType()).isEqualTo(EventType.SESSION.value());
    assertThat(event.getDate()).isNotNull();
  }

  @Test
  public void itCanSubmitSessionEnd() {
    client.submitSessionEnd("test-user-id");

    verify(submissionClient, times(1)).sendHeartBeat("test-user-id", true);
  }

  @Test
  public void itCanUpdateEmailAndDescription() {
    SubmissionResponse expectedResponse = SubmissionResponse.builder().code(200).build();
    doReturn(expectedResponse)
        .when(submissionClient)
        .postUserDescription(
            "test-reference-id",
            UserDescription.builder()
                .description("test-description")
                .emailAddress("test-email")
                .build());

    SubmissionResponse actualResponse =
        client.updateEmailAndDescription("test-reference-id", "test-email", "test-description");

    assertThat(actualResponse).isEqualTo(expectedResponse);
  }

  @Test
  public void itCanGetLastReferenceId() {
    configuration.getLastReferenceIdManager().setLast("123");

    assertThat(client.getLastReferenceId()).isEqualTo("123");
  }
}
