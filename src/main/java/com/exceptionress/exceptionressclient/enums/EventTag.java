package com.exceptionress.exceptionressclient.enums;

public enum EventTag {
  CRITICAL("Critical");

  private final String value;

  EventTag(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
}
