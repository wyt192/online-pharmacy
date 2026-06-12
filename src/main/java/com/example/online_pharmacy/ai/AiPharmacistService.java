package com.example.online_pharmacy.ai;

import com.example.online_pharmacy.ai.dto.AiConsultRequest;
import com.example.online_pharmacy.ai.dto.AiConsultResponse;
import com.example.online_pharmacy.consultation.entity.ConsultationRecord;
import com.example.online_pharmacy.consultation.entity.RiskLevel;
import com.example.online_pharmacy.consultation.repository.ConsultationRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Service
public class AiPharmacistService {

    private static final String DISCLAIMER = "AI 建议仅供线上购药参考，不能替代医生诊断或执业药师判断。如果症状严重、持续加重，或涉及儿童、老人、孕妇等特殊人群，请咨询人工药师或线下就医。";

    private final AiClient aiClient;
    private final ConsultationRecordRepository consultationRecordRepository;

    public AiPharmacistService(AiClient aiClient, ConsultationRecordRepository consultationRecordRepository) {
        this.aiClient = aiClient;
        this.consultationRecordRepository = consultationRecordRepository;
    }

    /**
     * Generates a medication consultation answer through the configured AI client.
     *
     * @param request simulated user id and consultation question
     * @return answer, risk level, human-pharmacist flag, and disclaimer
     * @throws com.example.online_pharmacy.common.BusinessException if the configured AI client fails
     */
    @Transactional
    public AiConsultResponse consult(AiConsultRequest request) {
        String question = request.getQuestion().trim();
        request.setQuestion(question);

        AiConsultResponse aiResponse = aiClient.consult(request);
        RiskLevel localRiskLevel = assessLocalRisk(question);
        RiskLevel riskLevel = higherRisk(localRiskLevel, aiResponse.getRiskLevel());
        boolean needHumanPharmacist = Boolean.TRUE.equals(aiResponse.getNeedHumanPharmacist()) || riskLevel == RiskLevel.HIGH;

        AiConsultResponse response = new AiConsultResponse(
                normalizeAnswer(aiResponse.getAnswer(), needHumanPharmacist),
                riskLevel,
                needHumanPharmacist,
                DISCLAIMER
        );
        saveRecord(request, response);
        return response;
    }

    private RiskLevel assessLocalRisk(String question) {
        String text = question.toLowerCase(Locale.ROOT);
        if (containsAny(text, "chest pain", "breathing", "severe allergy", "pregnant", "infant", "blood", "unconscious",
                "胸痛", "呼吸困难", "严重过敏", "孕妇", "婴儿", "出血", "昏迷")) {
            return RiskLevel.HIGH;
        }
        if (containsAny(text, "fever", "high temperature", "cough", "asthma", "diabetes", "hypertension",
                "发烧", "发热", "高烧", "咳嗽", "哮喘", "糖尿病", "高血压")) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private RiskLevel higherRisk(RiskLevel localRiskLevel, RiskLevel aiRiskLevel) {
        RiskLevel safeAiRiskLevel = aiRiskLevel == null ? RiskLevel.LOW : aiRiskLevel;
        return localRiskLevel.ordinal() >= safeAiRiskLevel.ordinal() ? localRiskLevel : safeAiRiskLevel;
    }

    private String normalizeAnswer(String answer, boolean needHumanPharmacist) {
        String normalizedAnswer = StringUtils.hasText(answer)
                ? answer
                : "请补充年龄、症状、持续时间、过敏史、基础疾病和当前用药情况，以便给出更准确的用药风险提示。";
        String lowerAnswer = normalizedAnswer.toLowerCase(Locale.ROOT);
        if (needHumanPharmacist && !lowerAnswer.contains("human pharmacist") && !normalizedAnswer.contains("人工药师")) {
            return normalizedAnswer + " 请联系人工药师进一步确认。";
        }
        return normalizedAnswer;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private void saveRecord(AiConsultRequest request, AiConsultResponse response) {
        ConsultationRecord record = new ConsultationRecord();
        record.setUserId(request.getUserId());
        record.setQuestion(request.getQuestion());
        record.setAnswer(response.getAnswer());
        record.setRiskLevel(response.getRiskLevel());
        record.setNeedHumanPharmacist(response.getNeedHumanPharmacist());
        record.setDisclaimer(response.getDisclaimer());
        record.setAiProvider(aiClient.providerName());
        consultationRecordRepository.save(record);
    }
}
