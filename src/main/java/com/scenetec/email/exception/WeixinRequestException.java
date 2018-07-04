package com.scenetec.email.exception;

public class WeixinRequestException extends RuntimeException{

    public WeixinRequestException(String msg) {
        super(msg);
    }

    public WeixinRequestException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

}
