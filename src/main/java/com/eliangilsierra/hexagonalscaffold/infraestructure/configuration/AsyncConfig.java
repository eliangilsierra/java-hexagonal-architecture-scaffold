package com.eliangilsierra.hexagonalscaffold.infraestructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables {@code @Async}. With {@code spring.threads.virtual.enabled=true}
 * (application.yml), Spring Boot backs the default async executor with
 * virtual threads — no custom {@code Executor} bean needed for this scaffold.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
