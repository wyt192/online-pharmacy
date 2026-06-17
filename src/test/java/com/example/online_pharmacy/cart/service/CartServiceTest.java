package com.example.online_pharmacy.cart.service;

import com.example.online_pharmacy.TestFixtures;
import com.example.online_pharmacy.cart.dto.CartResponse;
import com.example.online_pharmacy.cart.entity.CartItem;
import com.example.online_pharmacy.cart.repository.CartItemRepository;
import com.example.online_pharmacy.common.BusinessException;
import com.example.online_pharmacy.drug.entity.Drug;
import com.example.online_pharmacy.drug.entity.DrugStatus;
import com.example.online_pharmacy.drug.repository.DrugRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private DrugRepository drugRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void addItemShouldCreateCartItemAndReturnLatestCart() {
        Drug drug = TestFixtures.drug(1L, "Ibuprofen", new BigDecimal("18.80"), 10, false, DrugStatus.ON_SALE);
        AtomicReference<CartItem> savedItem = new AtomicReference<>();
        when(drugRepository.findById(1L)).thenReturn(Optional.of(drug));
        when(cartItemRepository.findByUserIdAndDrugId(1L, 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem item = invocation.getArgument(0);
            item.setId(10L);
            savedItem.set(item);
            return item;
        });
        when(cartItemRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenAnswer(invocation -> List.of(savedItem.get()));

        CartResponse response = cartService.addItem(TestFixtures.addCartItemRequest(1L, 1L, 2));

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getTotalQuantity()).isEqualTo(2);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("37.60");
        assertThat(savedItem.get().getQuantity()).isEqualTo(2);
    }

    @Test
    void addItemShouldMergeQuantityWhenSameDrugAlreadyExists() {
        Drug drug = TestFixtures.drug(1L, "Ibuprofen", new BigDecimal("18.80"), 10, false, DrugStatus.ON_SALE);
        CartItem existingItem = TestFixtures.cartItem(10L, 1L, 1L, 2);
        when(drugRepository.findById(1L)).thenReturn(Optional.of(drug));
        when(cartItemRepository.findByUserIdAndDrugId(1L, 1L)).thenReturn(Optional.of(existingItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartItemRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(existingItem));

        CartResponse response = cartService.addItem(TestFixtures.addCartItemRequest(1L, 1L, 3));

        assertThat(existingItem.getQuantity()).isEqualTo(5);
        assertThat(response.getTotalQuantity()).isEqualTo(5);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("94.00");
    }

    @Test
    void addItemShouldThrowWhenQuantityExceedsStock() {
        Drug drug = TestFixtures.drug(1L, "Ibuprofen", new BigDecimal("18.80"), 3, false, DrugStatus.ON_SALE);
        when(drugRepository.findById(1L)).thenReturn(Optional.of(drug));
        when(cartItemRepository.findByUserIdAndDrugId(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(TestFixtures.addCartItemRequest(1L, 1L, 4)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient stock");
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void getCartShouldCalculateTotalAmountAndQuantity() {
        CartItem firstItem = TestFixtures.cartItem(1L, 1L, 1L, 2);
        CartItem secondItem = TestFixtures.cartItem(2L, 1L, 2L, 3);
        Drug firstDrug = TestFixtures.drug(1L, "Ibuprofen", new BigDecimal("10.00"), 10, false, DrugStatus.ON_SALE);
        Drug secondDrug = TestFixtures.drug(2L, "Loratadine", new BigDecimal("4.75"), 10, false, DrugStatus.ON_SALE);
        when(cartItemRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(firstItem, secondItem));
        when(drugRepository.findById(1L)).thenReturn(Optional.of(firstDrug));
        when(drugRepository.findById(2L)).thenReturn(Optional.of(secondDrug));

        CartResponse response = cartService.getCart(1L);

        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getTotalQuantity()).isEqualTo(5);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("34.25");
    }
}
