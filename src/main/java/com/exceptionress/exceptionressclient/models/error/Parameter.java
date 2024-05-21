package com.exceptionress.exceptionressclient.models.error;

import com.exceptionress.exceptionressclient.models.base.Model;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Value
@EqualsAndHashCode(callSuper = true)
public class Parameter extends Model {
    List<String> genericArguments;
    String name;
    String type;
    String typeNamespace;
}
