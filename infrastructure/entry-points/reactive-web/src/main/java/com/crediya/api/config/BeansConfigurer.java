package com.crediya.api.config;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfigurer {
    @Bean
    WebProperties.Resources getResources() {
        return new WebProperties.Resources();
    }
}