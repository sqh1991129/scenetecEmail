package com.scenetec.email.exception;

public class AccessTokenException extends RuntimeException{

    public AccessTokenException(String msg) {
        super(msg);
    }

    public AccessTokenException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

}
