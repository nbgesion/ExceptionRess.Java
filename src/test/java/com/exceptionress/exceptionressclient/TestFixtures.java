package com.exceptionress.exceptionressclient;

import com.exceptionress.exceptionressclient.configuration.Configuration;
import com.exceptionress.exceptionressclient.queue.DefaultEventQueue;
import com.exceptionress.exceptionressclient.storage.InMemoryStorageProvider;
import org.mockito.Mockito;

public final class TestFixtures {
  private TestFixtures() {}

  public static Configuration.ConfigurationBuilder aDefaultConfigurationManager() {
    return Configuration.builder()
        .apiKey("12456790abcdef")
        .queue(Mockito.mock(DefaultEventQueue.class))
        .storageProvider(InMemoryStorageProvider.builder().build());
  }
}
