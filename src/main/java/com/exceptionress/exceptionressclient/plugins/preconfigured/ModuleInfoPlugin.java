package com.exceptionress.exceptionressclient.plugins.preconfigured;

import com.exceptionress.exceptionressclient.configuration.Configuration;
import com.exceptionress.exceptionressclient.models.EventPluginContext;
import com.exceptionress.exceptionressclient.models.Module;
import com.exceptionress.exceptionressclient.models.error.Error;
import com.exceptionress.exceptionressclient.plugins.EventPluginIF;
import lombok.Builder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModuleInfoPlugin implements EventPluginIF {
  private static final Integer DEFAULT_PRIORITY = 50;

  private final List<Module> modules;

  @Builder
  public ModuleInfoPlugin() {
    this.modules =
        ModuleLayer.boot().modules().stream()
            .map(module -> Module.builder().name(module.getName()).build())
            .collect(Collectors.toList());
  }

  @Override
  public int getPriority() {
    return DEFAULT_PRIORITY;
  }

  @Override
  public void run(
      EventPluginContext eventPluginContext, Configuration configuration) {
    Optional<Error> maybeError = eventPluginContext.getEvent().getError();
    if (maybeError.isEmpty()) {
      return;
    }
    Error error = maybeError.get();

    if (!error.getModules().isEmpty()) {
      return;
    }

    error.setModules(modules);
  }
}
