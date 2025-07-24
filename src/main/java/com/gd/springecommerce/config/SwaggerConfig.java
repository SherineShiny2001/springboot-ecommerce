package com.gd.springecommerce.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/**")
                .packagesToScan("com.gd.springecommerce.controller")
                .build();
    }

    @Bean
    public OpenAPI springSwaggerExample() {
        return new OpenAPI().info(new Info().title("Example API")
                        .description("Spring Swagger example")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.0").url("http://example.com")))
                .externalDocs(new ExternalDocumentation().description("Spring Swagger Example")
                        .url("https://example.com/docs"));
    }
}

