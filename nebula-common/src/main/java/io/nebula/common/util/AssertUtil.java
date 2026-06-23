package io.nebula.common.util;

import io.nebula.common.exception.BusinessException;
import io.nebula.common.exception.HttpErrorCodeEnum;

public class AssertUtil {
    public static void isTrue(boolean expression, HttpErrorCodeEnum errorCode, String... reasons) {
        if (!expression) {
            throw new BusinessException(errorCode, reasons);
        }
    }

    public static void notNull(Object obj, HttpErrorCodeEnum errorCode, String... reasons) {
        if (obj == null) {
            throw new BusinessException(errorCode, reasons);
        }
    }

    public static void notEmpty(String str, HttpErrorCodeEnum errorCode, String... reasons) {
        if (str == null || str.trim().isEmpty()) {
            throw new BusinessException(errorCode, reasons);
        }
    }
}
