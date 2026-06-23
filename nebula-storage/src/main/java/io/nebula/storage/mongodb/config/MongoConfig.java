package io.nebula.storage.mongodb.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@ConditionalOnProperty(name = "nebula.storage.mongodb.enabled", havingValue = "true")
@EnableMongoRepositories(basePackages = "io.nebula.storage.mongodb.repository")
public class MongoConfig {

    @Bean
    @ConfigurationProperties(prefix = "nebula.storage.mongodb")
    public MongoProperties mongoProperties() {
        return new MongoProperties();
    }

    @Bean
    public MongoClient mongoClient(MongoProperties properties) {
        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(properties.getUri()))
            .build();
        return MongoClients.create(settings);
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient, MongoProperties properties) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, properties.getDatabase());
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory factory) {
        return new MongoTemplate(factory);
    }
}
