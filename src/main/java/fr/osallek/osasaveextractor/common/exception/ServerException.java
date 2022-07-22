package fr.osallek.osasaveextractor.common.exception;

import fr.osallek.osasaveextractor.controller.object.ErrorCode;

public class ServerException extends RuntimeException {

    private final ErrorCode errorCode;

    public ServerException(ErrorCode errorCode) {
        this.errorCode = errorCode == null ? ErrorCode.DEFAULT_ERROR : errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
