package com.exceptionress.exceptionressclient.plugins.preconfigured;

import com.exceptionress.exceptionressclient.configuration.Configuration;
import com.exceptionress.exceptionressclient.models.Event;
import com.exceptionress.exceptionressclient.models.EventPluginContext;
import com.exceptionress.exceptionressclient.plugins.EventPluginIF;
import lombok.Builder;

public class ConfigurationDefaultsPlugin implements EventPluginIF {
  private static final Integer DEFAULT_PRIORITY = 10;

  @Builder
  public ConfigurationDefaultsPlugin() {}

  @Override
  public int getPriority() {
    return DEFAULT_PRIORITY;
  }

  @Override
  public void run(
      EventPluginContext eventPluginContext, Configuration configuration) {
    Event event = eventPluginContext.getEvent();
    for (String tag : configuration.getDefaultTags()) {
      event.addTags(tag);
    }

    event.addData(configuration.getDefaultData(), configuration.getDataExclusions());
  }
}
