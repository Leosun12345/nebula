package io.nebula.demo.chat;

import io.nebula.starter.EnableNebula;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableNebula
public class ChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
        System.out.println("Chat Demo started!");
        System.out.println("Open http://localhost:8080");
    }
}
