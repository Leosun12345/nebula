package io.nebula.common.event;

import org.springframework.stereotype.Component;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Component
public @interface Listener {
    String eventTag();
}
