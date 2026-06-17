package com.example.online_pharmacy.order.controller;

import com.example.online_pharmacy.order.dto.OrderDetailResponse;
import com.example.online_pharmacy.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void checkoutShouldReturnCreatedOrder() throws Exception {
        OrderDetailResponse response = new OrderDetailResponse();
        response.setId(100L);
        response.setOrderNo("PO202601010101010001234");
        response.setUserId(1L);
        response.setTotalAmount(new BigDecimal("37.60"));
        response.setItems(List.of());
        when(orderService.checkout(any())).thenReturn(response);

        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.orderNo").value("PO202601010101010001234"));
    }

    @Test
    void checkoutShouldRejectMissingUserId() throws Exception {
        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void getOrderDetailShouldReturnOrderDetail() throws Exception {
        OrderDetailResponse response = new OrderDetailResponse();
        response.setId(100L);
        response.setOrderNo("PO202601010101010001234");
        response.setUserId(1L);
        response.setTotalAmount(new BigDecimal("37.60"));
        response.setItems(List.of());
        when(orderService.getOrderDetail(100L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/orders/100").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(100));
    }

    @Test
    void getOrderDetailShouldRejectInvalidUserId() throws Exception {
        mockMvc.perform(get("/api/orders/100").param("userId", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
