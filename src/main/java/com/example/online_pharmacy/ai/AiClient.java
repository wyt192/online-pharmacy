package com.example.online_pharmacy.ai;

import com.example.online_pharmacy.ai.dto.AiConsultRequest;
import com.example.online_pharmacy.ai.dto.AiConsultResponse;

public interface AiClient {

    AiConsultResponse consult(AiConsultRequest request);

    String providerName();
}
