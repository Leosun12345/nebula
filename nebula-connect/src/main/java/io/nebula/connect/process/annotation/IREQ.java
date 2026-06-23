package io.nebula.connect.process.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface IREQ {
    String[] codes();
}
