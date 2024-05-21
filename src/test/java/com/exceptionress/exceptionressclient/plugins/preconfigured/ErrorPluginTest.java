package com.exceptionress.exceptionressclient.plugins.preconfigured;

import com.exceptionress.exceptionressclient.TestFixtures;
import com.exceptionress.exceptionressclient.configuration.Configuration;
import com.exceptionress.exceptionressclient.enums.EventPropertyKey;
import com.exceptionress.exceptionressclient.enums.EventType;
import com.exceptionress.exceptionressclient.models.Event;
import com.exceptionress.exceptionressclient.models.EventPluginContext;
import com.exceptionress.exceptionressclient.models.PluginContext;
import com.exceptionress.exceptionressclient.models.error.StackFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorPluginTest {
  private Configuration configuration;
  private ErrorPlugin plugin;

  @BeforeEach
  public void setup() {
    configuration = TestFixtures.aDefaultConfigurationManager().build();
    plugin = ErrorPlugin.builder().build();
  }

  @Test
  public void itCanAddExceptionToEventCorrectly() {
    Exception exc = new RuntimeException("test");
    EventPluginContext context =
        EventPluginContext.builder()
            .event(Event.builder().build())
            .context(PluginContext.builder().exception(exc).build())
            .build();

    plugin.run(context, configuration);

    Event event = context.getEvent();
    assertThat(event.getType()).isEqualTo(EventType.ERROR.value());
    assertThat(event.getError()).isPresent();
    assertThat(event.getError().get().getMessage()).isEqualTo("test");
    assertThat(event.getError().get().getType()).isEqualTo("java.lang.RuntimeException");
    assertThat(event.getError().get().getStackTrace()).isNotEmpty();

    StackFrame frame = event.getError().get().getStackTrace().get(0);
    // it should be the test file name.
    assertThat(frame.getFilename()).isEqualTo("ErrorPluginTest.java");

    Map<String, Object> data = event.getData();
    assertThat(data).isNotNull();
    assertThat(data).containsKey(EventPropertyKey.EXTRA.value());

    assertThat(data.get(EventPropertyKey.EXTRA.value())).isSameAs(exc);
  }
}
