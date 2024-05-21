package com.exceptionress.exceptionressclient.services;

public interface LastReferenceIdManagerIF {
    String getLast();
    void clearLast();
    void setLast(String eventId);
}
