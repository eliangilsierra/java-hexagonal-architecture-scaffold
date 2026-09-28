package com.eliangilsierra.hexagonalscaffold;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.eliangilsierra.hexagonalscaffold.application.dto.request.CreateOrderRequest;
import com.eliangilsierra.hexagonalscaffold.application.dto.response.OrderResponse;
import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Exercises the real, wired-up application (H2 swapped for a real Postgres
 * container, and a real RabbitMQ broker instead of an in-process event — see
 * ADR-0004 and ADR-0005) end to end through its HTTP API, proving the
 * eventually-consistent flow: right after creation the order is
 * {@code CREATED}; the RabbitMQ listener flips it to {@code NOTIFIED} once the
 * message round-trips through the broker.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);

        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
    }

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void createdOrderEventuallyBecomesNotified() {
        var request = new CreateOrderRequest("demo@example.com", "Widget", 2, BigDecimal.valueOf(15));

        ResponseEntity<OrderResponse> createResponse =
                restTemplate.postForEntity(url("/orders"), request, OrderResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().status()).isEqualTo("CREATED");

        Long orderId = createResponse.getBody().id();

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            ResponseEntity<OrderResponse> getResponse =
                    restTemplate.getForEntity(url("/orders/" + orderId), OrderResponse.class);
            assertThat(getResponse.getBody()).isNotNull();
            assertThat(getResponse.getBody().status()).isEqualTo("NOTIFIED");
        });
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
