package com.canvify.test.exception;

public class NotFoundException extends BaseException {

    public NotFoundException(String message) {
        super(message, "NOT_FOUND");
    }
}
