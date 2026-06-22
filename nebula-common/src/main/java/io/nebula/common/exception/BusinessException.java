package io.nebula.common.exception;

public class BusinessException extends RuntimeException {
    private final HttpErrorCodeEnum errorCode;
    private final String[] reasons;

    public BusinessException(HttpErrorCodeEnum errorCode, String... reasons) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.reasons = reasons;
    }

    public BusinessException(HttpErrorCodeEnum errorCode, Throwable cause, String... reasons) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.reasons = reasons;
    }

    public HttpErrorCodeEnum getErrorCode() {
        return errorCode;
    }

    public String[] getReasons() {
        return reasons;
    }
}
