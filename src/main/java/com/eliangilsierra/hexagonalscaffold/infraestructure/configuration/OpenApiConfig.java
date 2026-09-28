package com.eliangilsierra.hexagonalscaffold.infraestructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hexagonalScaffoldOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Hexagonal Architecture Scaffold")
                .description("Reference order-processing API — hexagonal (ports and adapters) architecture")
                .version("v1"));
    }
}
