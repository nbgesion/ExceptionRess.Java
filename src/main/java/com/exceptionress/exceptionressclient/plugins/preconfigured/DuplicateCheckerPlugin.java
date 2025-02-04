package com.exceptionress.exceptionressclient.plugins.preconfigured;

import com.exceptionress.exceptionressclient.configuration.Configuration;
import com.exceptionress.exceptionressclient.models.Event;
import com.exceptionress.exceptionressclient.models.EventPluginContext;
import com.exceptionress.exceptionressclient.plugins.EventPluginIF;
import com.exceptionress.exceptionressclient.plugins.MergedEvent;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DuplicateCheckerPlugin implements EventPluginIF {
  private static final Logger LOG = LoggerFactory.getLogger(DuplicateCheckerPlugin.class);
  private static final String MERGED_EVENTS_RESUBMISSION_TIMER_NAME =
      "merged-events-resubmission-timer";
  private static final Integer DEFAULT_PRIORITY = 1010;
  private static final Integer DEFAULT_MAX_HASHES_COUNT = 50;
  private static final Integer DEFAULT_MERGED_EVENTS_RESUBMISSION_IN_SECS = 30;

  private final int maxHashesCount;
  private final Queue<MergedEvent> mergedEvents;
  private final Timer mergedEventsResubmissionTimer;
  private final List<TimeStampedHash> hashes;
  private final Integer mergedEventsResubmissionInSecs;

  @Builder
  public DuplicateCheckerPlugin(Integer mergedEventsResubmissionInSecs, Integer maxHashesCount) {
    this.maxHashesCount = maxHashesCount == null ? DEFAULT_MAX_HASHES_COUNT : maxHashesCount;
    this.mergedEvents = new ArrayDeque<>();
    this.mergedEventsResubmissionTimer = new Timer(MERGED_EVENTS_RESUBMISSION_TIMER_NAME);
    this.hashes = new ArrayList<>();
    this.mergedEventsResubmissionInSecs =
        mergedEventsResubmissionInSecs == null
            ? DEFAULT_MERGED_EVENTS_RESUBMISSION_IN_SECS
            : mergedEventsResubmissionInSecs;
    init();
  }

  private void init() {
    mergedEventsResubmissionTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            try {
              MergedEvent event = mergedEvents.poll();
              if (event != null) {
                event.resubmit();
              }
            } catch (Exception e) {
              LOG.error("Error in resubmitting merged events", e);
            }
          }
        },
        mergedEventsResubmissionInSecs * 1000,
        mergedEventsResubmissionInSecs * 1000);
  }

  @Override
  public int getPriority() {
    return DEFAULT_PRIORITY;
  }

  @Override
  public void run(
      EventPluginContext eventPluginContext, Configuration configuration) {
    Event event = eventPluginContext.getEvent();
    long hash = getHash(event);
    Optional<MergedEvent> maybeMergedEvent =
        mergedEvents.stream().filter(mergedEvent -> mergedEvent.getHash() == hash).findFirst();
    if (maybeMergedEvent.isPresent()) {
      MergedEvent mergedEvent = maybeMergedEvent.get();
      mergedEvent.incrementCount(event.getCount());
      mergedEvent.updateDate(event.getDate());
      LOG.info(String.format("Ignoring duplicate event with hash: %s", hash));
      eventPluginContext.getContext().setEventCancelled(true);
      return;
    }

    long now = System.currentTimeMillis();
    // All the merged events of one hash are supposed to be processed by the timer every
    // `mergedEventsResubmissionInSecs` seconds
    if (hashes.stream()
        .anyMatch(
            timeStampedHash ->
                timeStampedHash.getHash() == hash
                    && timeStampedHash.getTimestamp()
                        >= (now - mergedEventsResubmissionInSecs * 1000))) {
      LOG.trace(String.format("Adding event with hash :%s", hash));
      mergedEvents.add(
          MergedEvent.builder()
              .event(event)
              .eventQueue(configuration.getQueue())
              .hash(hash)
              .build());
      eventPluginContext.getContext().setEventCancelled(true);
      return;
    }

    addNewHashIfPossible(hash, now);
  }

  private void addNewHashIfPossible(long hash, long now) {
    if (hashes.size() == maxHashesCount) {
      return;
    }

    hashes.add(TimeStampedHash.builder().hash(hash).timestamp(now).build());
  }

  private long getHash(Event event) {
    return Objects.hash(
        event.getType(),
        event.getSource(),
        event.getDate(),
        event.getTags(),
        event.getMessage(),
        event.getData(),
        event.getGeo(),
        event.getValue());
  }

  @Builder
  @Getter
  private static class TimeStampedHash {
    private final long hash;
    private final long timestamp;
  }
}
