package com.exceptionress.exceptionressclient.submission;

import com.exceptionress.exceptionressclient.models.Event;
import com.exceptionress.exceptionressclient.models.UserDescription;

import java.util.List;

public interface SubmissionClientIF {
  SubmissionResponse postEvents(
      List<Event> events);

  SubmissionResponse postUserDescription(
      String referenceId,
      UserDescription description);

  void sendHeartBeat(String sessionIdOrUserId, boolean closeSession);
}
