package com.exceptionress.exceptionressclient.plugins.preconfigured;

import com.exceptionress.exceptionressclient.TestFixtures;
import com.exceptionress.exceptionressclient.configuration.Configuration;
import com.exceptionress.exceptionressclient.models.Event;
import com.exceptionress.exceptionressclient.models.EventPluginContext;
import com.exceptionress.exceptionressclient.models.PluginContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SubmissionMethodPluginTest {
  private Configuration configuration;
  private SubmissionMethodPlugin plugin;
  private EventPluginContext context;

  @BeforeEach
  public void setup() {
    plugin = SubmissionMethodPlugin.builder().build();
    configuration = TestFixtures.aDefaultConfigurationManager().build();
  }

  @Test
  public void itShouldNotDoAnythingIfNoSubmissionMethodInTheContext() {
    context = EventPluginContext.from(Event.builder().build());

    plugin.run(context, configuration);

    assertThat(context.getEvent().getSubmissionMethod()).isNotPresent();
  }

  @Test
  public void itShouldAddSubmissionMethodToTheEvent() {
    context =
        EventPluginContext.builder()
            .event(Event.builder().build())
            .context(PluginContext.builder().submissionMethod("test-submission-method").build())
            .build();

    plugin.run(context, configuration);

    assertThat(context.getEvent().getSubmissionMethod()).isPresent();
    assertThat(context.getEvent().getSubmissionMethod().get()).isEqualTo("test-submission-method");
  }
}
