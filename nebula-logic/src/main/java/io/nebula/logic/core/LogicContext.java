package io.nebula.logic.core;

import io.nebula.logic.handler.HandlerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Logic 层 Spring 上下文工具
 * 用于在非 Spring 管理的类中获取 Spring Bean
 *
 * @author leo
 * @since 1.0.0
 */
@Component
public class LogicContext implements ApplicationContextAware, ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(LogicContext.class);
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    /**
     * 通过 Class 获取 Bean
     * 支持驼峰命名自动转换 (如: eventUtil → EventUtil)
     */
    public static <T> T getInstance(Class<?> clz) {
        if (context == null) {
            log.error("LogicContext not initialized yet!");
            return null;
        }

        try {
            return (T) context.getBean(clz);
        } catch (BeansException e) {
            // 尝试通过名称获取
            String clzName = clz.getSimpleName();
            String beanName = clzName.substring(0, 1).toLowerCase() + clzName.substring(1);
            try {
                return (T) context.getBean(beanName);
            } catch (BeansException ex) {
                log.error("Failed to get bean: {}", clzName, ex);
                return null;
            }
        }
    }

    /**
     * 通过 Bean 名称获取
     */
    @SuppressWarnings("unchecked")
    public static <T> T getInstance(String name) {
        if (context == null) {
            log.error("LogicContext not initialized yet!");
            return null;
        }
        try {
            return (T) context.getBean(name);
        } catch (BeansException e) {
            log.error("Failed to get bean by name: {}", name, e);
            return null;
        }
    }

    /**
     * 获取 ApplicationContext (用于特殊场景)
     */
    public static ApplicationContext getApplicationContext() {
        return context;
    }

    /**
     * 检查是否已初始化
     */
    public static boolean isInitialized() {
        return context != null;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 启动 HandlerManager 扫描
        HandlerManager.getInstance().start();
        log.info("LogicContext initialized, HandlerManager started");
    }
}
