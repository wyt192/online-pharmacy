package com.example.online_pharmacy.cart.controller;

import com.example.online_pharmacy.cart.dto.AddCartItemRequest;
import com.example.online_pharmacy.cart.dto.CartResponse;
import com.example.online_pharmacy.cart.service.CartService;
import com.example.online_pharmacy.common.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Adds a drug to the current simulated user's cart.
     *
     * @param request user id, drug id, and quantity to add
     * @return latest cart snapshot after the item is added
     * @throws org.springframework.web.bind.MethodArgumentNotValidException if request body validation fails
     * @throws com.example.online_pharmacy.common.BusinessException if the drug is unavailable or stock is insufficient
     */
    @PostMapping("/items")
    public Result<CartResponse> addItem(@Valid @RequestBody AddCartItemRequest request) {
        return Result.success(cartService.addItem(request));
    }

    /**
     * Gets the cart for a simulated user.
     *
     * @param userId simulated user id
     * @return cart items, total amount, and total quantity
     * @throws jakarta.validation.ConstraintViolationException if userId is not positive
     * @throws com.example.online_pharmacy.common.BusinessException if a cart item references a missing drug
     */
    @GetMapping
    public Result<CartResponse> getCart(@RequestParam @Positive Long userId) {
        return Result.success(cartService.getCart(userId));
    }
}
