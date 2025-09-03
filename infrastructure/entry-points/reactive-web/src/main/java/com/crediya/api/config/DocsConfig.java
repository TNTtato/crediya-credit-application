package com.crediya.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Credit Application Service API",
        version = "1.0",
        description = "Provides credit request operations for crediya"
))
public class DocsConfig {
}
