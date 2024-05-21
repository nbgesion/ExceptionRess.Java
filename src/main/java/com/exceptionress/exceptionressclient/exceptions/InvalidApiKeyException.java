package com.exceptionress.exceptionressclient.exceptions;

public class InvalidApiKeyException extends RuntimeException {
  public InvalidApiKeyException(String message) {
    super(message);
  }
}
