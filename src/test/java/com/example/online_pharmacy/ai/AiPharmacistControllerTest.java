package com.example.online_pharmacy.ai;

import com.example.online_pharmacy.ai.dto.AiConsultResponse;
import com.example.online_pharmacy.consultation.entity.RiskLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiPharmacistController.class)
class AiPharmacistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiPharmacistService aiPharmacistService;

    @Test
    void consultShouldReturnAiConsultResponse() throws Exception {
        when(aiPharmacistService.consult(any()))
                .thenReturn(new AiConsultResponse("Use with care", RiskLevel.HIGH, true, "Disclaimer"));

        mockMvc.perform(post("/api/ai/pharmacist/consult")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "question": "Can pregnant patient take ibuprofen?"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.answer").value("Use with care"))
                .andExpect(jsonPath("$.data.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.data.needHumanPharmacist").value(true));
    }

    @Test
    void consultShouldRejectBlankQuestion() throws Exception {
        mockMvc.perform(post("/api/ai/pharmacist/consult")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "question": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void consultShouldRejectInvalidUserId() throws Exception {
        mockMvc.perform(post("/api/ai/pharmacist/consult")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 0,
                                  "question": "general question"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
