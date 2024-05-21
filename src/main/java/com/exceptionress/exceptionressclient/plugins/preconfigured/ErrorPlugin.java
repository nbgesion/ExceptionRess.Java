package com.exceptionress.exceptionressclient.plugins.preconfigured;

import com.exceptionress.exceptionressclient.configuration.Configuration;
import com.exceptionress.exceptionressclient.enums.EventPropertyKey;
import com.exceptionress.exceptionressclient.enums.EventType;
import com.exceptionress.exceptionressclient.models.Event;
import com.exceptionress.exceptionressclient.models.EventPluginContext;
import com.exceptionress.exceptionressclient.models.error.Error;
import com.exceptionress.exceptionressclient.models.error.StackFrame;
import com.exceptionress.exceptionressclient.plugins.EventPluginIF;
import lombok.Builder;

import java.util.*;
import java.util.stream.Collectors;

public class ErrorPlugin implements EventPluginIF {
  private static final Integer DEFAULT_PRIORITY = 30;

  @Builder
  public ErrorPlugin() {}

  @Override
  public int getPriority() {
    return DEFAULT_PRIORITY;
  }

  @Override
  public void run(
      EventPluginContext eventPluginContext, Configuration configuration) {
    Exception exception = eventPluginContext.getContext().getException();
    if (exception == null) {
      return;
    }

    Event event = eventPluginContext.getEvent();
    event.setType(EventType.ERROR.value());
    if (event.getError().isPresent()) {
      return;
    }

    event.addError(parse(exception));

    Set<String> dataExclusions = new HashSet<>(configuration.getDataExclusions());
    event.addData(Map.of(EventPropertyKey.EXTRA.value(), exception), dataExclusions);
  }

  private Error parse(Exception exception) {
    return Error.builder()
        .type(exception.getClass().getCanonicalName())
        .message(exception.getMessage())
        .stackTrace(getStackFrames(exception))
        .build();
  }

  private List<StackFrame> getStackFrames(Exception exception) {
    return Arrays.stream(exception.getStackTrace())
        .map(
            stackTraceElement ->
                StackFrame.builder()
                    .name(stackTraceElement.getMethodName())
                    .filename(stackTraceElement.getFileName())
                    .lineNumber(stackTraceElement.getLineNumber())
                    .declaringType(stackTraceElement.getClassName())
                    .build())
        .collect(Collectors.toList());
  }
}
