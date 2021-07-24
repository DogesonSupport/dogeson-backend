package com.dogeson;

public enum ErrorCodes {

    SUCCESS(0),
    INTERNAL_ERROR(101);

    private int code = 0;

    ErrorCodes(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
