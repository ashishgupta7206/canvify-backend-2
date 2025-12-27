package com.canvify.test.exception;

public class ConflictException extends BaseException {

    public ConflictException(String message) {
        super(message, "CONFLICT");
    }
}
