package com.example.online_pharmacy.ai;

import com.example.online_pharmacy.ai.dto.AiConsultRequest;
import com.example.online_pharmacy.ai.dto.AiConsultResponse;
import com.example.online_pharmacy.common.BusinessException;
import com.example.online_pharmacy.common.Result;
import com.example.online_pharmacy.consultation.entity.RiskLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "deepseek")
public class DeepSeekAiClient implements AiClient {

    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";
    private static final double DEFAULT_TEMPERATURE = 0.3;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${deepseek.base-url}")
    private String baseUrl;

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.model:deepseek-v4-flash}")
    private String model;

    @Value("${deepseek.thinking-enabled:false}")
    private boolean thinkingEnabled;

    @Value("${deepseek.reasoning-effort:high}")
    private String reasoningEffort;

    @Override
    public AiConsultResponse consult(AiConsultRequest request) {
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException("DeepSeek API key is not configured");
        }

        try {
            String requestBody = objectMapper.writeValueAsString(buildRequestBody(request));
            HttpRequest httpRequest = HttpRequest.newBuilder(buildRequestUri())
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300) {
                throw new BusinessException("DeepSeek API call failed, status=" + httpResponse.statusCode());
            }
            return parseResponse(httpResponse.body());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(Result.SYSTEM_ERROR_CODE, "DeepSeek API call was interrupted", ex);
        } catch (IOException ex) {
            throw new BusinessException(Result.SYSTEM_ERROR_CODE, "Failed to call DeepSeek API", ex);
        } catch (JacksonException ex) {
            throw new BusinessException(Result.SYSTEM_ERROR_CODE, "Failed to parse DeepSeek API response", ex);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(Result.SYSTEM_ERROR_CODE, "Invalid DeepSeek API configuration", ex);
        }
    }

    @Override
    public String providerName() {
        return "deepseek";
    }

    private URI buildRequestUri() {
        if (!StringUtils.hasText(baseUrl)) {
            throw new BusinessException("DeepSeek base URL is not configured");
        }
        String normalizedBaseUrl = baseUrl;
        while (normalizedBaseUrl.endsWith("/")) {
            normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
        }
        return URI.create(normalizedBaseUrl + CHAT_COMPLETIONS_PATH);
    }

    private Map<String, Object> buildRequestBody(AiConsultRequest request) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("stream", false);
        requestBody.put("response_format", Map.of("type", "json_object"));
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", buildSystemPrompt()),
                Map.of("role", "user", "content", request.getQuestion())));
        requestBody.put("temperature", DEFAULT_TEMPERATURE);

        if (!thinkingEnabled) {
            requestBody.put("thinking", Map.of("type", "disabled"));
        } else if (StringUtils.hasText(reasoningEffort)) {
            requestBody.put("reasoning_effort", reasoningEffort);
        }

        return requestBody;
    }

    private String buildSystemPrompt() {
        return "你是线上购药系统中的 AI 药师助手。\n"
                + "你不能诊断疾病，不能替代医生或执业药师。\n"
                + "你只能提供一般性用药信息和风险提示。\n\n"
                + "如果用户问题涉及儿童、老人、孕妇、处方药、过敏史、严重疼痛、呼吸困难、药物相互作用、剂量不明等情况，"
                + "必须建议转人工药师或线下就医。\n\n"
                + "你必须输出 json 对象，不要输出 Markdown，不要输出多余文字。\n\n"
                + "JSON 格式如下：\n"
                + "{\n"
                + "  \"answer\": \"给用户的简短建议\",\n"
                + "  \"riskLevel\": \"LOW/MEDIUM/HIGH\",\n"
                + "  \"needHumanPharmacist\": true,\n"
                + "  \"disclaimer\": \"AI 建议仅供参考，不能替代医生诊断或执业药师判断。\"\n"
                + "}";
    }

    private AiConsultResponse parseResponse(String responseBody) throws JacksonException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        String content = contentNode.stringValue();
        if (contentNode.isMissingNode() || !StringUtils.hasText(content)) {
            throw new BusinessException("Invalid response from DeepSeek API");
        }

        AiConsultResponse response = objectMapper.readValue(content, AiConsultResponse.class);
        if (!StringUtils.hasText(response.getAnswer())) {
            throw new BusinessException("DeepSeek response answer is empty");
        }
        if (response.getRiskLevel() == null) {
            response.setRiskLevel(RiskLevel.MEDIUM);
        }
        if (response.getNeedHumanPharmacist() == null) {
            response.setNeedHumanPharmacist(false);
        }
        return response;
    }
}
