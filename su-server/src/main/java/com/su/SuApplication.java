package com.su;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
@EnableScheduling//开启定时任务功能
@EnableWebSocket
public class SuApplication {
    public static void main(String[] args) {
        SpringApplication.run(SuApplication.class, args);
        log.info("server started");
    }
}
