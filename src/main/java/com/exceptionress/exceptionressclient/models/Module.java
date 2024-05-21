package com.exceptionress.exceptionressclient.models;

import com.exceptionress.exceptionressclient.models.base.Model;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@SuperBuilder
@Value
@EqualsAndHashCode(callSuper = true)
public class Module extends Model {
    Long moduleId;
    String name;
    String version;
    Boolean isEntry;
    LocalDate createdDate;
    LocalDate modifiedDate;
}
