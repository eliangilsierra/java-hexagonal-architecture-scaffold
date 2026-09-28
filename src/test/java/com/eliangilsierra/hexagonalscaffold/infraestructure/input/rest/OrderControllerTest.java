package com.eliangilsierra.hexagonalscaffold.infraestructure.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eliangilsierra.hexagonalscaffold.application.dto.response.OrderResponse;
import com.eliangilsierra.hexagonalscaffold.application.handler.OrderHandler;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The web-layer slice of the test pyramid: verifies request validation and
 * response shape without a real domain, database or broker behind it — the
 * handler is mocked, exactly as {@code OrderControllerIntegrationTest} exists
 * to prove the real wiring, and {@code OrderUseCaseTest} exists to prove the
 * business logic in isolation.
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderHandler orderHandler;

    @Test
    void createOrder_returnsCreatedWithBody() throws Exception {
        when(orderHandler.createOrder(any())).thenReturn(
                new OrderResponse(1L, "demo@example.com", "Widget", 2, BigDecimal.TEN, BigDecimal.valueOf(20), "CREATED"));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerEmail":"demo@example.com","productName":"Widget","quantity":2,"unitPrice":10}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void createOrder_rejectsInvalidEmailAndQuantity() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerEmail":"not-an-email","productName":"","quantity":0,"unitPrice":10}
                                """))
                .andExpect(status().isBadRequest());
    }
}
