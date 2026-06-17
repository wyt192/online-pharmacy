package com.example.online_pharmacy.repository;

import com.example.online_pharmacy.TestFixtures;
import com.example.online_pharmacy.cart.entity.CartItem;
import com.example.online_pharmacy.cart.repository.CartItemRepository;
import com.example.online_pharmacy.consultation.entity.ConsultationRecord;
import com.example.online_pharmacy.consultation.entity.RiskLevel;
import com.example.online_pharmacy.consultation.repository.ConsultationRecordRepository;
import com.example.online_pharmacy.drug.entity.Drug;
import com.example.online_pharmacy.drug.entity.DrugStatus;
import com.example.online_pharmacy.drug.repository.DrugRepository;
import com.example.online_pharmacy.order.entity.Order;
import com.example.online_pharmacy.order.entity.OrderItem;
import com.example.online_pharmacy.order.repository.OrderItemRepository;
import com.example.online_pharmacy.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.sql.init.mode=never")
class RepositoryTest {

    @Autowired
    private DrugRepository drugRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ConsultationRecordRepository consultationRecordRepository;

    @Test
    void drugRepositoryShouldSaveAndFindDrug() {
        Drug drug = TestFixtures.drug(null, "Ibuprofen", new BigDecimal("18.80"), 80, false, DrugStatus.ON_SALE);

        Drug saved = drugRepository.saveAndFlush(drug);

        assertThat(saved.getId()).isNotNull();
        assertThat(drugRepository.findById(saved.getId())).hasValueSatisfying(found -> {
            assertThat(found.getName()).isEqualTo("Ibuprofen");
            assertThat(found.getStockQuantity()).isEqualTo(80);
        });
    }

    @Test
    void cartItemRepositoryShouldFindByUserIdAndDrugId() {
        CartItem firstItem = TestFixtures.cartItem(null, 1L, 10L, 2);
        CartItem secondItem = TestFixtures.cartItem(null, 1L, 11L, 1);
        cartItemRepository.saveAllAndFlush(List.of(firstItem, secondItem));

        assertThat(cartItemRepository.findByUserIdAndDrugId(1L, 10L))
                .hasValueSatisfying(item -> assertThat(item.getQuantity()).isEqualTo(2));
        assertThat(cartItemRepository.findByUserIdOrderByCreatedAtDesc(1L)).hasSize(2);
    }

    @Test
    void orderRepositoriesShouldFindOrderAndItems() {
        Order order = TestFixtures.order(null, 1L, new BigDecimal("37.60"));
        order.setOrderNo("PO-TEST-001");
        Order savedOrder = orderRepository.saveAndFlush(order);
        OrderItem firstItem = TestFixtures.orderItem(null, savedOrder.getId(), 10L, "Ibuprofen", new BigDecimal("18.80"), 1, false);
        OrderItem secondItem = TestFixtures.orderItem(null, savedOrder.getId(), 11L, "Amoxicillin", new BigDecimal("26.00"), 1, true);
        orderItemRepository.saveAllAndFlush(List.of(firstItem, secondItem));

        assertThat(orderRepository.findByIdAndUserId(savedOrder.getId(), 1L)).contains(savedOrder);
        assertThat(orderItemRepository.findByOrderIdOrderByIdAsc(savedOrder.getId()))
                .extracting(OrderItem::getDrugName)
                .containsExactly("Ibuprofen", "Amoxicillin");
    }

    @Test
    void consultationRecordRepositoryShouldSaveRecord() {
        ConsultationRecord record = new ConsultationRecord();
        record.setUserId(1L);
        record.setQuestion("general question");
        record.setAnswer("general answer");
        record.setRiskLevel(RiskLevel.LOW);
        record.setNeedHumanPharmacist(false);
        record.setDisclaimer("disclaimer");
        record.setAiProvider("mock");

        ConsultationRecord saved = consultationRecordRepository.saveAndFlush(record);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(consultationRecordRepository.findById(saved.getId()))
                .hasValueSatisfying(found -> assertThat(found.getAiProvider()).isEqualTo("mock"));
    }
}
