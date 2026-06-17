package com.example.online_pharmacy.drug.controller;

import com.example.online_pharmacy.common.BusinessException;
import com.example.online_pharmacy.drug.dto.DrugResponse;
import com.example.online_pharmacy.drug.service.DrugService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DrugController.class)
class DrugControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DrugService drugService;

    @Test
    void searchDrugsShouldReturnSuccessResult() throws Exception {
        when(drugService.searchDrugs(eq("ibu"), eq("Pain Relief"), eq(false), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/drugs/search")
                        .param("keyword", "ibu")
                        .param("category", "Pain Relief")
                        .param("prescriptionRequired", "false")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        verify(drugService).searchDrugs(eq("ibu"), eq("Pain Relief"), eq(false), any(Pageable.class));
    }

    @Test
    void searchDrugsShouldRejectInvalidSize() throws Exception {
        mockMvc.perform(get("/api/drugs/search").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void getDrugDetailShouldReturnDrugDetail() throws Exception {
        DrugResponse response = new DrugResponse();
        response.setId(1L);
        response.setName("Ibuprofen");
        response.setPrice(new BigDecimal("18.80"));
        response.setStockQuantity(80);
        response.setPrescriptionRequired(false);
        when(drugService.getDrugDetail(1L)).thenReturn(response);

        mockMvc.perform(get("/api/drugs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Ibuprofen"));
    }

    @Test
    void getDrugDetailShouldMapBusinessExceptionToBadRequest() throws Exception {
        when(drugService.getDrugDetail(99L)).thenThrow(new BusinessException("Drug not found"));

        mockMvc.perform(get("/api/drugs/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Drug not found"));
    }
}
