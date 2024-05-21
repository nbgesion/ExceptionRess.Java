package com.exceptionress.exceptionressclient.models.error;

import com.exceptionress.exceptionressclient.models.base.Model;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Value
@NonFinal
@EqualsAndHashCode(callSuper = true)
public class InnerError extends Model {
  String message;
  String type;
  String code;
  InnerError inner;
  List<StackFrame> stackTrace;
  Method targetMethod;
}
