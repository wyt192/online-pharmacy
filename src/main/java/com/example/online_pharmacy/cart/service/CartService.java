package com.example.online_pharmacy.cart.service;

import com.example.online_pharmacy.cart.dto.AddCartItemRequest;
import com.example.online_pharmacy.cart.dto.CartItemResponse;
import com.example.online_pharmacy.cart.dto.CartResponse;
import com.example.online_pharmacy.cart.entity.CartItem;
import com.example.online_pharmacy.cart.repository.CartItemRepository;
import com.example.online_pharmacy.common.BusinessException;
import com.example.online_pharmacy.drug.entity.Drug;
import com.example.online_pharmacy.drug.entity.DrugStatus;
import com.example.online_pharmacy.drug.repository.DrugRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final DrugRepository drugRepository;

    public CartService(CartItemRepository cartItemRepository, DrugRepository drugRepository) {
        this.cartItemRepository = cartItemRepository;
        this.drugRepository = drugRepository;
    }

    /**
     * Adds a drug to a user's cart, merging quantity when the same drug already exists.
     *
     * @param request user id, drug id, and quantity to add
     * @return latest cart snapshot after persistence
     * @throws BusinessException if the drug does not exist, is off sale, is out of stock, or requested quantity exceeds stock
     */
    @Transactional
    public CartResponse addItem(AddCartItemRequest request) {
        Drug drug = getAvailableDrug(request.getDrugId());
        CartItem cartItem = cartItemRepository.findByUserIdAndDrugId(request.getUserId(), request.getDrugId())
                .orElseGet(CartItem::new);

        int currentQuantity = cartItem.getQuantity() == null ? 0 : cartItem.getQuantity();
        int newQuantity = currentQuantity + request.getQuantity();
        if (newQuantity > drug.getStockQuantity()) {
            throw new BusinessException("Insufficient stock for drug: " + drug.getName());
        }

        cartItem.setUserId(request.getUserId());
        cartItem.setDrugId(request.getDrugId());
        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);

        return getCart(request.getUserId());
    }

    /**
     * Builds a user's cart view with current drug price, stock, prescription flag, and totals.
     *
     * @param userId simulated user id
     * @return cart response for the user
     * @throws BusinessException if a cart item references a drug that no longer exists
     */
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByCreatedAtDesc(userId);
        CartResponse response = new CartResponse();
        response.setUserId(userId);

        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;
        for (CartItem cartItem : cartItems) {
            Drug drug = drugRepository.findById(cartItem.getDrugId())
                    .orElseThrow(() -> new BusinessException("Drug not found in cart"));
            CartItemResponse itemResponse = toCartItemResponse(cartItem, drug);
            response.getItems().add(itemResponse);
            totalAmount = totalAmount.add(itemResponse.getSubtotal());
            totalQuantity += itemResponse.getQuantity();
        }

        response.setTotalAmount(totalAmount);
        response.setTotalQuantity(totalQuantity);
        return response;
    }

    private Drug getAvailableDrug(Long drugId) {
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new BusinessException("Drug not found"));
        if (drug.getStatus() != DrugStatus.ON_SALE) {
            throw new BusinessException("Drug is off sale");
        }
        if (drug.getStockQuantity() <= 0) {
            throw new BusinessException("Drug is out of stock");
        }
        return drug;
    }

    private CartItemResponse toCartItemResponse(CartItem cartItem, Drug drug) {
        CartItemResponse response = new CartItemResponse();
        BigDecimal subtotal = drug.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        response.setId(cartItem.getId());
        response.setDrugId(drug.getId());
        response.setDrugName(drug.getName());
        response.setSpecification(drug.getSpecification());
        response.setUnitPrice(drug.getPrice());
        response.setQuantity(cartItem.getQuantity());
        response.setSubtotal(subtotal);
        response.setStockQuantity(drug.getStockQuantity());
        response.setPrescriptionRequired(drug.getPrescriptionRequired());
        return response;
    }
}
