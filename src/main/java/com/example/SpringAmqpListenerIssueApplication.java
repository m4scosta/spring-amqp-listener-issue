package com.example;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Run ./mvnw spring-boot:run
 */
@SpringBootApplication
public class SpringAmqpListenerIssueApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAmqpListenerIssueApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(ConnectionFactory connectionFactory) {
        return args -> connectionFactory.addConnectionListener(new SimpleConnectionListener());
    }

    @RabbitListener(bindings = @QueueBinding(
            key = "rabbit.test",
            value = @Queue(value = "test-queue"),
            exchange = @Exchange(value = "test-exchange")
    ))
    public void dummyListener() {
        System.out.println("OK");
    }

    private class SimpleConnectionListener implements ConnectionListener {

        @Override
        public void onCreate(Connection connection) {
            System.out.println("Connection CREATED");
        }

        @Override
        public void onClose(Connection connection) {
            System.out.println("Connection CLOSED");
        }
    }
}
