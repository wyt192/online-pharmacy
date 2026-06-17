package com.example.online_pharmacy.ai;

import com.example.online_pharmacy.TestFixtures;
import com.example.online_pharmacy.ai.dto.AiConsultRequest;
import com.example.online_pharmacy.ai.dto.AiConsultResponse;
import com.example.online_pharmacy.consultation.entity.ConsultationRecord;
import com.example.online_pharmacy.consultation.entity.RiskLevel;
import com.example.online_pharmacy.consultation.repository.ConsultationRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiPharmacistServiceTest {

    @Mock
    private AiClient aiClient;

    @Mock
    private ConsultationRecordRepository consultationRecordRepository;

    @InjectMocks
    private AiPharmacistService aiPharmacistService;

    @BeforeEach
    void setUp() {
        when(aiClient.providerName()).thenReturn("test-ai");
    }

    @Test
    void consultShouldTrimQuestionCallAiClientAndSaveRecord() {
        AiConsultRequest request = TestFixtures.aiConsultRequest(1L, "  general medicine question  ");
        when(aiClient.consult(request))
                .thenReturn(new AiConsultResponse("General answer", RiskLevel.LOW, false, null));

        AiConsultResponse response = aiPharmacistService.consult(request);

        assertThat(request.getQuestion()).isEqualTo("general medicine question");
        assertThat(response.getRiskLevel()).isEqualTo(RiskLevel.LOW);
        assertThat(response.getNeedHumanPharmacist()).isFalse();

        ArgumentCaptor<ConsultationRecord> captor = ArgumentCaptor.forClass(ConsultationRecord.class);
        verify(consultationRecordRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(1L);
        assertThat(captor.getValue().getQuestion()).isEqualTo("general medicine question");
        assertThat(captor.getValue().getAnswer()).contains("General answer");
        assertThat(captor.getValue().getAiProvider()).isEqualTo("test-ai");
    }

    @Test
    void consultShouldEscalateToHighRiskWhenLocalRuleDetectsHighRisk() {
        AiConsultRequest request = TestFixtures.aiConsultRequest(1L, "pregnant patient asks about medicine");
        when(aiClient.consult(any(AiConsultRequest.class)))
                .thenReturn(new AiConsultResponse("General answer", RiskLevel.LOW, false, null));

        AiConsultResponse response = aiPharmacistService.consult(request);

        assertThat(response.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
        assertThat(response.getNeedHumanPharmacist()).isTrue();
        assertThat(response.getAnswer()).isNotBlank();
    }

    @Test
    void consultShouldUseFallbackAnswerWhenAiAnswerIsBlank() {
        AiConsultRequest request = TestFixtures.aiConsultRequest(1L, "general question");
        when(aiClient.consult(any(AiConsultRequest.class)))
                .thenReturn(new AiConsultResponse(" ", RiskLevel.LOW, false, null));

        AiConsultResponse response = aiPharmacistService.consult(request);

        assertThat(response.getAnswer()).isNotBlank();
        assertThat(response.getDisclaimer()).isNotBlank();
    }
}
