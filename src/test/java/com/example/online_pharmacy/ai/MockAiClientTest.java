package com.example.online_pharmacy.ai;

import com.example.online_pharmacy.TestFixtures;
import com.example.online_pharmacy.ai.dto.AiConsultResponse;
import com.example.online_pharmacy.consultation.entity.RiskLevel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockAiClientTest {

    private final MockAiClient mockAiClient = new MockAiClient();

    @Test
    void providerNameShouldBeMock() {
        assertThat(mockAiClient.providerName()).isEqualTo("mock");
    }

    @Test
    void consultShouldReturnHighRiskForSpecialPopulationOrSevereSymptom() {
        AiConsultResponse response = mockAiClient.consult(
                TestFixtures.aiConsultRequest(1L, "I am pregnant and have breathing problems"));

        assertThat(response.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
        assertThat(response.getNeedHumanPharmacist()).isTrue();
        assertThat(response.getAnswer()).isNotBlank();
    }

    @Test
    void consultShouldReturnMediumRiskForFeverQuestion() {
        AiConsultResponse response = mockAiClient.consult(
                TestFixtures.aiConsultRequest(1L, "I have a fever and cough"));

        assertThat(response.getRiskLevel()).isEqualTo(RiskLevel.MEDIUM);
        assertThat(response.getNeedHumanPharmacist()).isFalse();
    }

    @Test
    void consultShouldReturnLowRiskForGeneralQuestion() {
        AiConsultResponse response = mockAiClient.consult(
                TestFixtures.aiConsultRequest(1L, "Can I buy a common bandage?"));

        assertThat(response.getRiskLevel()).isEqualTo(RiskLevel.LOW);
        assertThat(response.getNeedHumanPharmacist()).isFalse();
    }
}
