package com.exceptionress.exceptionressclient.storage;


import com.exceptionress.exceptionressclient.models.Event;
import com.exceptionress.exceptionressclient.settings.ServerSettings;

public interface StorageProviderIF {
    StorageIF<Event> getQueue();
    StorageIF<ServerSettings> getSettings();
}
