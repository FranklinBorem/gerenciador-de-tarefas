package com.taskapi.domain.exception;

/** Uma regra de negócio foi violada. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
