package com.exceptionress.exceptionressclient.storage;

import com.exceptionress.exceptionressclient.models.Event;
import com.exceptionress.exceptionressclient.settings.ServerSettings;
import lombok.Builder;

public class InMemoryStorageProvider implements StorageProviderIF {
  private static final Integer DEFAULT_MAX_QUEUE_ITEMS = 250;

  private final StorageIF<Event> eventQueue;
  private final StorageIF<ServerSettings> settingsStore;

  @Builder
  private InMemoryStorageProvider(Integer maxQueueItems) {
    this.eventQueue =
        InMemoryStorage.<Event>builder()
            .maxItems(maxQueueItems == null ? DEFAULT_MAX_QUEUE_ITEMS : maxQueueItems)
            .build();
    this.settingsStore = InMemoryStorage.<ServerSettings>builder().maxItems(1).build();
  }

  @Override
  public StorageIF<Event> getQueue() {
    return eventQueue;
  }

  @Override
  public StorageIF<ServerSettings> getSettings() {
    return settingsStore;
  }
}
