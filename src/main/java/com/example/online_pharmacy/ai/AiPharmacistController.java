package com.example.online_pharmacy.ai;

import com.example.online_pharmacy.ai.dto.AiConsultRequest;
import com.example.online_pharmacy.ai.dto.AiConsultResponse;
import com.example.online_pharmacy.common.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/pharmacist")
public class AiPharmacistController {

    private final AiPharmacistService aiPharmacistService;

    public AiPharmacistController(AiPharmacistService aiPharmacistService) {
        this.aiPharmacistService = aiPharmacistService;
    }

    /**
     * Receives a medication consultation question and returns an AI pharmacist
     * answer.
     *
     * @param request simulated user id and question
     * @return answer, risk level, human-pharmacist flag, and disclaimer
     * @throws org.springframework.web.bind.MethodArgumentNotValidException if
     *                                                                      request
     *                                                                      body
     *                                                                      validation
     *                                                                      fails
     * @throws com.example.online_pharmacy.common.BusinessException         if the
     *                                                                      configured
     *                                                                      AI
     *                                                                      client
     *                                                                      fails
     */
    @PostMapping("/consult")
    public Result<AiConsultResponse> consult(@Valid @RequestBody AiConsultRequest request) {
        return Result.success(aiPharmacistService.consult(request));
    }
}
