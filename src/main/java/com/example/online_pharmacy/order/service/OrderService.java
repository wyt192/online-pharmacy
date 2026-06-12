package com.example.online_pharmacy.order.service;

import com.example.online_pharmacy.cart.entity.CartItem;
import com.example.online_pharmacy.cart.repository.CartItemRepository;
import com.example.online_pharmacy.common.BusinessException;
import com.example.online_pharmacy.drug.entity.Drug;
import com.example.online_pharmacy.drug.entity.DrugStatus;
import com.example.online_pharmacy.drug.repository.DrugRepository;
import com.example.online_pharmacy.order.dto.CheckoutRequest;
import com.example.online_pharmacy.order.dto.OrderDetailResponse;
import com.example.online_pharmacy.order.entity.Order;
import com.example.online_pharmacy.order.entity.OrderItem;
import com.example.online_pharmacy.order.entity.OrderStatus;
import com.example.online_pharmacy.order.repository.OrderItemRepository;
import com.example.online_pharmacy.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {

    private static final DateTimeFormatter ORDER_NO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final CartItemRepository cartItemRepository;
    private final DrugRepository drugRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(CartItemRepository cartItemRepository,
                        DrugRepository drugRepository,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository) {
        this.cartItemRepository = cartItemRepository;
        this.drugRepository = drugRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * Creates an order from all items in the user's cart.
     * <p>
     * The method validates drug availability and stock, writes order item snapshots,
     * deducts drug stock, and clears the cart in one transaction.
     *
     * @param request simulated user checkout request
     * @return created order detail
     * @throws BusinessException if the cart is empty, a drug is missing, a drug is off sale, or stock is insufficient
     */
    @Transactional
    public OrderDetailResponse checkout(CheckoutRequest request) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByCreatedAtDesc(request.getUserId());
        if (cartItems.isEmpty()) {
            throw new BusinessException("Cart is empty");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(BigDecimal.ZERO);
        orderRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Drug drug = drugRepository.findById(cartItem.getDrugId())
                    .orElseThrow(() -> new BusinessException("Drug not found in cart"));
            validatePurchasable(drug, cartItem.getQuantity());

            BigDecimal subtotal = drug.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = buildOrderItem(order.getId(), drug, cartItem.getQuantity(), subtotal);
            orderItems.add(orderItem);

            drug.setStockQuantity(drug.getStockQuantity() - cartItem.getQuantity());
        }

        order.setTotalAmount(totalAmount);
        orderItemRepository.saveAll(orderItems);
        cartItemRepository.deleteAll(cartItems);

        return OrderDetailResponse.from(order, orderItems);
    }

    /**
     * Loads an order and its items for a simulated user.
     *
     * @param orderId order id
     * @param userId simulated user id
     * @return order detail response
     * @throws BusinessException if no order exists for the given id and user id
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new BusinessException("Order not found"));
        List<OrderItem> orderItems = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId());
        return OrderDetailResponse.from(order, orderItems);
    }

    private void validatePurchasable(Drug drug, Integer quantity) {
        if (drug.getStatus() != DrugStatus.ON_SALE) {
            throw new BusinessException("Drug is off sale: " + drug.getName());
        }
        if (drug.getStockQuantity() < quantity) {
            throw new BusinessException("Insufficient stock for drug: " + drug.getName());
        }
    }

    private OrderItem buildOrderItem(Long orderId, Drug drug, Integer quantity, BigDecimal subtotal) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderId);
        orderItem.setDrugId(drug.getId());
        orderItem.setDrugName(drug.getName());
        orderItem.setSpecification(drug.getSpecification());
        orderItem.setUnitPrice(drug.getPrice());
        orderItem.setQuantity(quantity);
        orderItem.setSubtotal(subtotal);
        orderItem.setPrescriptionRequired(drug.getPrescriptionRequired());
        return orderItem;
    }

    private String generateOrderNo() {
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "PO" + LocalDateTime.now().format(ORDER_NO_TIME_FORMATTER) + random;
    }
}
