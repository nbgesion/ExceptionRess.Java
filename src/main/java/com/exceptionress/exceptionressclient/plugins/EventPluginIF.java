package com.exceptionress.exceptionressclient.plugins;

import com.exceptionress.exceptionressclient.configuration.Configuration;
import com.exceptionress.exceptionressclient.models.EventPluginContext;

public interface EventPluginIF {
  int getPriority();

  default String getName() {
    return getClass().getName();
  }

  void run(EventPluginContext eventPluginContext, Configuration configuration);
}
