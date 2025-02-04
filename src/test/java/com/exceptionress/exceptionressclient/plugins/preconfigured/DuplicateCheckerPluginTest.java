package com.exceptionress.exceptionressclient.plugins.preconfigured;

import com.exceptionress.exceptionressclient.configuration.Configuration;
import com.exceptionress.exceptionressclient.enums.EventPropertyKey;
import com.exceptionress.exceptionressclient.models.Event;
import com.exceptionress.exceptionressclient.models.EventPluginContext;
import com.exceptionress.exceptionressclient.models.error.Error;
import com.exceptionress.exceptionressclient.models.error.StackFrame;
import com.exceptionress.exceptionressclient.queue.DefaultEventQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DuplicateCheckerPluginTest {
  private static final String EVENT_REF_ID = "test-event";
  private static final Long EVENT_COUNT = 100L;

  @Mock private DefaultEventQueue eventQueue;

  private EventPluginContext context;
  private Configuration configuration;
  private DuplicateCheckerPlugin plugin;

  @BeforeEach
  public void setup() {
    Error error =
        Error.builder()
            .message("test-error-message")
            .stackTrace(List.of(StackFrame.builder().column(1).filename("test-file").build()))
            .build();
    context =
        EventPluginContext.from(
            Event.builder()
                .referenceId(EVENT_REF_ID)
                .property(EventPropertyKey.ERROR.value(), error)
                .count(EVENT_COUNT)
                .build());
    configuration =
        Configuration.builder().apiKey("12456790abcdef").queue(eventQueue).build();
  }

  @Test
  public void itCanDetectAPotentialToBeMergedEvent() throws InterruptedException {
    plugin = DuplicateCheckerPlugin.builder().mergedEventsResubmissionInSecs(1).build();
    plugin.run(context, configuration);
    plugin.run(context, configuration);
    Thread.sleep(1500);
    assertThat(context.getContext().isEventCancelled()).isTrue();
    verify(eventQueue)
        .enqueue(
            argThat(
                event ->
                    event.getCount().equals(EVENT_COUNT)
                        && event.getReferenceId().equals(EVENT_REF_ID)));
  }

  @Test
  public void itCanMergeEventsWithSameHash() throws InterruptedException {
    plugin = DuplicateCheckerPlugin.builder().mergedEventsResubmissionInSecs(1).build();
    plugin.run(context, configuration);
    plugin.run(context, configuration);
    plugin.run(context, configuration);
    Thread.sleep(1500);
    assertThat(context.getContext().isEventCancelled()).isTrue();
    verify(eventQueue)
        .enqueue(
            argThat(
                event ->
                    event.getCount().equals(EVENT_COUNT * 2)
                        && event.getReferenceId().equals(EVENT_REF_ID)));
  }
}
