package com.example.onlineshop.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customApiConfig() {
        return new OpenAPI()
                .info(new Info()
                        .title("Online Shop API")
                        .version("1.0")
                        .description("REST API for products"));
    }
}
