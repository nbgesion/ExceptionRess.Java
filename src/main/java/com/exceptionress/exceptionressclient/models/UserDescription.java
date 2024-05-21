package com.exceptionress.exceptionressclient.models;

import com.exceptionress.exceptionressclient.models.base.Model;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Value
@EqualsAndHashCode(callSuper = true)
public class UserDescription extends Model {
    String emailAddress;
    String description;
}
