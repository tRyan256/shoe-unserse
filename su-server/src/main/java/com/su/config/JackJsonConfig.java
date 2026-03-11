package com.su.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.json.JacksonObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JackJsonConfig {

    @Bean
    @Primary
    public ObjectMapper jacksonObjectMapper(){
        return new JacksonObjectMapper();
    }
}
