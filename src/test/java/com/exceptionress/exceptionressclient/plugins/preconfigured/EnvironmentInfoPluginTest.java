package com.exceptionress.exceptionressclient.plugins.preconfigured;

import com.exceptionress.exceptionressclient.TestFixtures;
import com.exceptionress.exceptionressclient.configuration.Configuration;
import com.exceptionress.exceptionressclient.configuration.PrivateInformationInclusions;
import com.exceptionress.exceptionressclient.models.Event;
import com.exceptionress.exceptionressclient.models.EventPluginContext;
import com.exceptionress.exceptionressclient.models.EnvironmentInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EnvironmentInfoPluginTest {
  private EventPluginContext context;
  private Configuration configuration;
  private EnvironmentInfoPlugin plugin;

  @BeforeEach
  public void setup() {
    context = EventPluginContext.from(Event.builder().build());
    configuration = TestFixtures.aDefaultConfigurationManager().build();
    plugin = EnvironmentInfoPlugin.builder().build();
  }

  @Test
  public void itShouldNotIncludeMachineNameAndIpAddressUntilExplicitlyTold() {
    assertThat(context.getEvent().getEnvironmentInfo()).isEmpty();

    plugin.run(context, configuration);

    assertThat(context.getEvent().getEnvironmentInfo()).isPresent();
    EnvironmentInfo info = context.getEvent().getEnvironmentInfo().get();
    assertThat(info.getMachineName()).isNull();
    assertThat(info.getIpAddress()).isNull();

    assertThat(info.getProcessorCount()).isNotNull();
  }

  @Test
  public void itCanIncludeMachineNameAndIpAddress() {
    assertThat(context.getEvent().getEnvironmentInfo()).isEmpty();
    PrivateInformationInclusions inclusions =
        configuration.getPrivateInformationInclusions();
    inclusions.setIpAddress(true);
    inclusions.setMachineName(true);

    plugin.run(context, configuration);

    assertThat(context.getEvent().getEnvironmentInfo()).isPresent();
    EnvironmentInfo info = context.getEvent().getEnvironmentInfo().get();

    assertThat(info.getMachineName()).isNotNull();
    assertThat(info.getIpAddress()).isNotNull();

    assertThat(info.getProcessorCount()).isNotNull();
  }
}
