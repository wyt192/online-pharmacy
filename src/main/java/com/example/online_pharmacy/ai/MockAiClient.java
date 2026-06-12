package com.example.online_pharmacy.ai;

import com.example.online_pharmacy.ai.dto.AiConsultRequest;
import com.example.online_pharmacy.ai.dto.AiConsultResponse;
import com.example.online_pharmacy.consultation.entity.RiskLevel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockAiClient implements AiClient {

    private static final String HIGH_RISK_ANSWER = "该问题可能涉及高风险症状或特殊人群用药，请先联系人工药师确认；如果症状严重、持续加重或出现呼吸困难等情况，请及时线下就医。";
    private static final String MEDIUM_RISK_ANSWER = "请结合症状持续时间、体温变化、过敏史和当前用药情况综合判断。非处方药也应严格按说明书使用；如果症状持续或加重，请咨询药师或医生。";
    private static final String LOW_RISK_ANSWER = "请补充年龄、主要症状、持续时间、过敏史、基础疾病和当前用药情况。购药前请仔细核对适应症、禁忌症和用法用量。";

    @Override
    public AiConsultResponse consult(AiConsultRequest request) {
        RiskLevel riskLevel = assessRisk(request.getQuestion());
        boolean needHumanPharmacist = riskLevel == RiskLevel.HIGH;
        String answer = switch (riskLevel) {
            case HIGH -> HIGH_RISK_ANSWER;
            case MEDIUM -> MEDIUM_RISK_ANSWER;
            case LOW -> LOW_RISK_ANSWER;
        };
        return new AiConsultResponse(answer, riskLevel, needHumanPharmacist, null);
    }

    @Override
    public String providerName() {
        return "mock";
    }

    private RiskLevel assessRisk(String question) {
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

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
