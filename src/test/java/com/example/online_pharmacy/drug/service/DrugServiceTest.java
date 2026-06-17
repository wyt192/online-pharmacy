package com.example.online_pharmacy.drug.service;

import com.example.online_pharmacy.TestFixtures;
import com.example.online_pharmacy.common.BusinessException;
import com.example.online_pharmacy.drug.dto.DrugResponse;
import com.example.online_pharmacy.drug.entity.Drug;
import com.example.online_pharmacy.drug.entity.DrugStatus;
import com.example.online_pharmacy.drug.repository.DrugRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DrugServiceTest {

    @Mock
    private DrugRepository drugRepository;

    @InjectMocks
    private DrugService drugService;

    @Test
    void getDrugDetailShouldReturnDrugResponse() {
        Drug drug = TestFixtures.drug(1L, "Ibuprofen", new BigDecimal("18.80"), 80, false, DrugStatus.ON_SALE);
        when(drugRepository.findById(1L)).thenReturn(Optional.of(drug));

        DrugResponse response = drugService.getDrugDetail(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Ibuprofen");
        assertThat(response.getPrice()).isEqualByComparingTo("18.80");
        assertThat(response.getPrescriptionRequired()).isFalse();
    }

    @Test
    void getDrugDetailShouldThrowWhenDrugDoesNotExist() {
        when(drugRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> drugService.getDrugDetail(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Drug not found");
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchDrugsShouldMapRepositoryPageToResponsePage() {
        Pageable pageable = PageRequest.of(0, 10);
        Drug drug = TestFixtures.drug(1L, "Ibuprofen", new BigDecimal("18.80"), 80, false, DrugStatus.ON_SALE);
        when(drugRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(drug), pageable, 1));

        Page<DrugResponse> result = drugService.searchDrugs("ibu", "Pain Relief", false, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Ibuprofen");
        verify(drugRepository).findAll(any(Specification.class), eq(pageable));
    }
}
