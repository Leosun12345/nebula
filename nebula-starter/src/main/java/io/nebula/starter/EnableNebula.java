package io.nebula.starter;

import org.springframework.context.annotation.Import;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(NebulaAutoConfiguration.class)
public @interface EnableNebula {
}
