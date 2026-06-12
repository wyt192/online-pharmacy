package com.example.online_pharmacy.ai.dto;

import com.example.online_pharmacy.consultation.entity.RiskLevel;

public class AiConsultResponse {

    private String answer;
    private RiskLevel riskLevel;
    private Boolean needHumanPharmacist;
    private String disclaimer;

    public AiConsultResponse() {
    }

    public AiConsultResponse(String answer,
                             RiskLevel riskLevel,
                             Boolean needHumanPharmacist,
                             String disclaimer) {
        this.answer = answer;
        this.riskLevel = riskLevel;
        this.needHumanPharmacist = needHumanPharmacist;
        this.disclaimer = disclaimer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Boolean getNeedHumanPharmacist() {
        return needHumanPharmacist;
    }

    public void setNeedHumanPharmacist(Boolean needHumanPharmacist) {
        this.needHumanPharmacist = needHumanPharmacist;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
