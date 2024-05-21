package com.exceptionress.exceptionressclient.plugins.preconfigured;

import com.exceptionress.exceptionressclient.configuration.Configuration;
import com.exceptionress.exceptionressclient.models.EventPluginContext;
import com.exceptionress.exceptionressclient.plugins.EventPluginIF;
import lombok.Builder;

public class SubmissionMethodPlugin implements EventPluginIF {
  private static final Integer DEFAULT_PRIORITY = 100;

  @Builder
  public SubmissionMethodPlugin() {}

  @Override
  public int getPriority() {
    return DEFAULT_PRIORITY;
  }

  @Override
  public void run(
      EventPluginContext eventPluginContext, Configuration configuration) {
    String submissionMethod = eventPluginContext.getContext().getSubmissionMethod();
    if (submissionMethod == null) {
      return;
    }

    eventPluginContext.getEvent().addSubmissionMethod(submissionMethod);
  }
}
