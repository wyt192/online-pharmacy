package com.example.online_pharmacy.order.service;

import com.example.online_pharmacy.TestFixtures;
import com.example.online_pharmacy.cart.entity.CartItem;
import com.example.online_pharmacy.cart.repository.CartItemRepository;
import com.example.online_pharmacy.common.BusinessException;
import com.example.online_pharmacy.drug.entity.Drug;
import com.example.online_pharmacy.drug.entity.DrugStatus;
import com.example.online_pharmacy.drug.repository.DrugRepository;
import com.example.online_pharmacy.order.dto.OrderDetailResponse;
import com.example.online_pharmacy.order.entity.Order;
import com.example.online_pharmacy.order.entity.OrderItem;
import com.example.online_pharmacy.order.repository.OrderItemRepository;
import com.example.online_pharmacy.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private DrugRepository drugRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void checkoutShouldCreateOrderDeductStockAndClearCart() {
        CartItem cartItem = TestFixtures.cartItem(1L, 1L, 1L, 2);
        Drug drug = TestFixtures.drug(1L, "Ibuprofen", new BigDecimal("18.80"), 10, false, DrugStatus.ON_SALE);
        when(cartItemRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(cartItem));
        when(drugRepository.findById(1L)).thenReturn(Optional.of(drug));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });
        when(orderItemRepository.saveAll(any())).thenAnswer(invocation -> {
            List<OrderItem> orderItems = invocation.getArgument(0);
            orderItems.get(0).setId(200L);
            return orderItems;
        });

        OrderDetailResponse response = orderService.checkout(TestFixtures.checkoutRequest(1L));

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getOrderNo()).startsWith("PO");
        assertThat(response.getTotalAmount()).isEqualByComparingTo("37.60");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getSubtotal()).isEqualByComparingTo("37.60");
        assertThat(drug.getStockQuantity()).isEqualTo(8);
        verify(cartItemRepository).deleteAll(List.of(cartItem));
    }

    @Test
    void checkoutShouldThrowWhenCartIsEmpty() {
        when(cartItemRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.checkout(TestFixtures.checkoutRequest(1L)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Cart is empty");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void checkoutShouldThrowWhenStockIsInsufficient() {
        CartItem cartItem = TestFixtures.cartItem(1L, 1L, 1L, 5);
        Drug drug = TestFixtures.drug(1L, "Ibuprofen", new BigDecimal("18.80"), 3, false, DrugStatus.ON_SALE);
        when(cartItemRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(cartItem));
        when(drugRepository.findById(1L)).thenReturn(Optional.of(drug));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });

        assertThatThrownBy(() -> orderService.checkout(TestFixtures.checkoutRequest(1L)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient stock");
        verify(orderItemRepository, never()).saveAll(any());
        verify(cartItemRepository, never()).deleteAll(any());
    }

    @Test
    void getOrderDetailShouldReturnOrderWithItems() {
        Order order = TestFixtures.order(100L, 1L, new BigDecimal("37.60"));
        OrderItem orderItem = TestFixtures.orderItem(200L, 100L, 1L, "Ibuprofen", new BigDecimal("18.80"), 2, false);
        when(orderRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdOrderByIdAsc(100L)).thenReturn(List.of(orderItem));

        OrderDetailResponse response = orderService.getOrderDetail(100L, 1L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getDrugName()).isEqualTo("Ibuprofen");
    }

    @Test
    void getOrderDetailShouldThrowWhenOrderDoesNotExist() {
        when(orderRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderDetail(100L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Order not found");
    }
}
