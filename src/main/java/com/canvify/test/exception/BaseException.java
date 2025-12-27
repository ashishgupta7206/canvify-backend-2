package com.canvify.test.exception;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {

    private final String errorCode;

    protected BaseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
