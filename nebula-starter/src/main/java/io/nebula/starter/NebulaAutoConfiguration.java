package io.nebula.starter;

import io.nebula.common.event.EventUtil;
import io.nebula.common.worker.WorkManager;
import io.nebula.connect.WebSocketContext;
import io.nebula.storage.mongodb.config.MongoConfig;
import io.nebula.storage.mysql.config.MyBatisPlusConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    WebSocketContext.class,
    EventUtil.class,
    MyBatisPlusConfig.class,
    MongoConfig.class
})
public class NebulaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WorkManager workManager() {
        WorkManager manager = WorkManager.getInstance();
        manager.start();
        return manager;
    }
}
