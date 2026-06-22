
### 10.2 nebula-storage/README.md

```markdown
# Nebula Storage

MySQL (ShardingSphere) + MongoDB dual storage abstraction.

## Features

- MySQL with HikariCP connection pool
- ShardingSphere day-based table sharding
- Auto table creation with `TableCreator`
- MongoDB with Spring Data MongoDB
- Dual data source auto-configuration

## Configuration

```yaml
nebula:
  storage:
    mysql:
      enabled: true
      jdbc-url: jdbc:mysql://localhost:3306/nebula_game
      username: root
      password: ${MYSQL_PASSWORD}
      sharding:
        enabled: true
        ttl-days: 180
    mongodb:
      enabled: true
      uri: mongodb://localhost:27017
      database: nebula_game_log