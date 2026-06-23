package io.nebula.logic.handler.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ISubCode {
    String[] subCodes();
}
