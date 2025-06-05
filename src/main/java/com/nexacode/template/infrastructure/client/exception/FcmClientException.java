package com.nexacode.template.infrastructure.client.exception;

import com.google.firebase.messaging.MessagingErrorCode;

public class FcmClientException extends RuntimeException {
    private static final String BASE_MESSAGE = "파이어베이스 서버와의 통신에 실패했습니다. ";
    private final int statusCode;
    private final MessagingErrorCode errorCode; // Firebase MessagingErrorCode 추가


    public FcmClientException(int statusCode, String message, MessagingErrorCode errorCode) {
        super(BASE_MESSAGE + " 관리자에게 문의해주세요." + message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public MessagingErrorCode getErrorCode() {
        return errorCode;
    }
}