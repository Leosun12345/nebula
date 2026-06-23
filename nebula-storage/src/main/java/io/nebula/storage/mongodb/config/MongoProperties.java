package io.nebula.storage.mongodb.config;

public class MongoProperties {
    private String uri = "mongodb://localhost:27017";
    private String database = "nebula_game";
    private String username;
    private String password;

    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }
    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
