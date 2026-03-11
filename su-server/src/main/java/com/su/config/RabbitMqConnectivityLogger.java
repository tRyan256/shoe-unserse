package com.su.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMqConnectivityLogger {

    @Bean
    public ApplicationRunner rabbitMqConnectivityProbe(ConnectionFactory connectionFactory) {
        return args -> {
            String target = describeTarget(connectionFactory);

            if (connectionFactory instanceof CachingConnectionFactory) {
                CachingConnectionFactory caching = (CachingConnectionFactory) connectionFactory;
                caching.addConnectionListener(new ConnectionListener() {
                    @Override
                    public void onCreate(Connection connection) {
                        log.info("RabbitMQ connection created: {} open={}", target, connection.isOpen());
                    }

                    @Override
                    public void onClose(Connection connection) {
                        log.warn("RabbitMQ connection closed: {} open={}", target, connection.isOpen());
                    }

                    @Override
                    public void onFailed(Exception exception) {
                        log.error("RabbitMQ connection failed: {}", target, exception);
                    }
                });
            }

            log.info("RabbitMQ connectivity probe starting: {}", target);
            try (Connection connection = connectionFactory.createConnection()) {
                log.info("RabbitMQ connectivity probe success: {} open={}", target, connection.isOpen());
            } catch (Exception ex) {
                log.error("RabbitMQ connectivity probe failed: {}", target, ex);
            }
        };
    }

    private String describeTarget(ConnectionFactory connectionFactory) {
        if (connectionFactory instanceof CachingConnectionFactory) {
            CachingConnectionFactory caching = (CachingConnectionFactory) connectionFactory;
            return String.format(
                    "%s:%d vhost=%s user=%s",
                    caching.getHost(),
                    caching.getPort(),
                    caching.getVirtualHost(),
                    caching.getUsername()
            );
        }
        return String.valueOf(connectionFactory);
    }
}
