package com.exceptionress.exceptionressclient.models;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Builder
@Value
public class ManualStackingInfo {
  String title;
  Map<String, String> signatureData;
}
