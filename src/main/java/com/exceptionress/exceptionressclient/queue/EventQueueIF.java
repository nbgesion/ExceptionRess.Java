package com.exceptionress.exceptionressclient.queue;

import com.exceptionress.exceptionressclient.models.Event;
import com.exceptionress.exceptionressclient.submission.SubmissionResponse;

import java.time.Duration;
import java.util.List;
import java.util.function.BiConsumer;

public interface EventQueueIF {
  void enqueue(Event event);

  void process();

  void suspendProcessing(Duration duration, boolean discardFutureQueueItems, boolean clearQueue);

  void onEventsPosted(BiConsumer<List<Event>, SubmissionResponse> handler);
}
