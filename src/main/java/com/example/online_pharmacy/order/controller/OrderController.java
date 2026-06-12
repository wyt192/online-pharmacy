package com.example.online_pharmacy.order.controller;

import com.example.online_pharmacy.common.Result;
import com.example.online_pharmacy.order.dto.CheckoutRequest;
import com.example.online_pharmacy.order.dto.OrderDetailResponse;
import com.example.online_pharmacy.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Submits an order from the simulated user's current cart.
     *
     * @param request simulated user id for checkout
     * @return created order detail, including item snapshots and total amount
     * @throws org.springframework.web.bind.MethodArgumentNotValidException if request body validation fails
     * @throws com.example.online_pharmacy.common.BusinessException if the cart is empty or any item cannot be purchased
     */
    @PostMapping("/checkout")
    public Result<OrderDetailResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        return Result.success(orderService.checkout(request));
    }

    /**
     * Gets order detail for the simulated user.
     *
     * @param id order id
     * @param userId simulated user id used to limit access to the user's own order
     * @return order detail with order items
     * @throws jakarta.validation.ConstraintViolationException if id or userId is not positive
     * @throws com.example.online_pharmacy.common.BusinessException if the order does not exist for the user
     */
    @GetMapping("/{id}")
    public Result<OrderDetailResponse> getOrderDetail(@PathVariable @Positive Long id,
                                                      @RequestParam @Positive Long userId) {
        return Result.success(orderService.getOrderDetail(id, userId));
    }
}
