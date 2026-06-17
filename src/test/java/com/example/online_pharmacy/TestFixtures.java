package com.example.online_pharmacy;

import com.example.online_pharmacy.ai.dto.AiConsultRequest;
import com.example.online_pharmacy.cart.dto.AddCartItemRequest;
import com.example.online_pharmacy.cart.entity.CartItem;
import com.example.online_pharmacy.drug.entity.Drug;
import com.example.online_pharmacy.drug.entity.DrugStatus;
import com.example.online_pharmacy.order.dto.CheckoutRequest;
import com.example.online_pharmacy.order.entity.Order;
import com.example.online_pharmacy.order.entity.OrderItem;
import com.example.online_pharmacy.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static Drug drug(Long id,
                            String name,
                            BigDecimal price,
                            int stockQuantity,
                            boolean prescriptionRequired,
                            DrugStatus status) {
        Drug drug = new Drug();
        drug.setId(id);
        drug.setName(name);
        drug.setCategory("Pain Relief");
        drug.setManufacturer("Test Pharma");
        drug.setSpecification("10 tablets");
        drug.setDescription("Test drug");
        drug.setPrice(price);
        drug.setStockQuantity(stockQuantity);
        drug.setPrescriptionRequired(prescriptionRequired);
        drug.setStatus(status);
        drug.setCreatedAt(LocalDateTime.now());
        drug.setUpdatedAt(LocalDateTime.now());
        return drug;
    }

    public static AddCartItemRequest addCartItemRequest(Long userId, Long drugId, int quantity) {
        AddCartItemRequest request = new AddCartItemRequest();
        request.setUserId(userId);
        request.setDrugId(drugId);
        request.setQuantity(quantity);
        return request;
    }

    public static CartItem cartItem(Long id, Long userId, Long drugId, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setId(id);
        cartItem.setUserId(userId);
        cartItem.setDrugId(drugId);
        cartItem.setQuantity(quantity);
        cartItem.setCreatedAt(LocalDateTime.now());
        cartItem.setUpdatedAt(LocalDateTime.now());
        return cartItem;
    }

    public static CheckoutRequest checkoutRequest(Long userId) {
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId(userId);
        return request;
    }

    public static Order order(Long id, Long userId, BigDecimal totalAmount) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNo("PO202601010101010001234");
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    public static OrderItem orderItem(Long id,
                                      Long orderId,
                                      Long drugId,
                                      String drugName,
                                      BigDecimal unitPrice,
                                      int quantity,
                                      boolean prescriptionRequired) {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(id);
        orderItem.setOrderId(orderId);
        orderItem.setDrugId(drugId);
        orderItem.setDrugName(drugName);
        orderItem.setSpecification("10 tablets");
        orderItem.setUnitPrice(unitPrice);
        orderItem.setQuantity(quantity);
        orderItem.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        orderItem.setPrescriptionRequired(prescriptionRequired);
        return orderItem;
    }

    public static AiConsultRequest aiConsultRequest(Long userId, String question) {
        AiConsultRequest request = new AiConsultRequest();
        request.setUserId(userId);
        request.setQuestion(question);
        return request;
    }
}
