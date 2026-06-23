package io.nebula.connect;

import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class WebSocketContext implements ApplicationContextAware, ApplicationRunner {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static <T> T getInstance(Class<?> clz) {
        return (T) context.getBean(clz);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        io.nebula.connect.manager.ProcessManager.getInstance().start();
    }
}
