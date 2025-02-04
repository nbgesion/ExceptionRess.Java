package com.exceptionress.exceptionressclient.plugins;

import com.exceptionress.exceptionressclient.models.Event;
import com.exceptionress.exceptionressclient.queue.EventQueueIF;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class MergedEvent {
  private Long hash;
  private Event event;
  private EventQueueIF eventQueue;

  @Builder
  public MergedEvent(Long hash, Event event, EventQueueIF eventQueue) {
    this.hash = hash;
    this.event = event;
    this.eventQueue = eventQueue;
  }

  public void incrementCount(long count) {
    event.setCount(event.getCount() + count);
  }

  public void updateDate(OffsetDateTime date) {
    if (date.isAfter(event.getDate())) {
      event.setDate(date);
    }
  }

  public void resubmit() {
    eventQueue.enqueue(event);
  }
}
