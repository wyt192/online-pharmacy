package com.example.online_pharmacy.common;

public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(Result.BUSINESS_ERROR_CODE, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
