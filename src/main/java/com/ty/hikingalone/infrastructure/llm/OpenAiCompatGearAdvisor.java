package com.ty.hikingalone.infrastructure.llm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ty.hikingalone.domain.gear.service.GearAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * OpenAI 兼容对话接口适配器（DeepSeek / 通义千问 / 智谱 / 自建 vLLM 均兼容）。
 * <p>凭证未配置时 available()=false，由应用服务降级规则引擎；
 * 网络失败/输出不可解析一律收敛为 empty，不向应用层抛出网络异常</p>
 */
@Slf4j
@Component
public class OpenAiCompatGearAdvisor implements GearAdvisor {

    private static final String SYSTEM_PROMPT = """
            你是资深徒步装备顾问，熟悉从一日轻装到多日重装的全场景。根据用户提供的线路与行程信息，\
            输出一份「需要补充的装备建议」。硬性要求：
            1. 只输出 JSON 数组，不要输出任何其他文字或 markdown 围栏；
            2. 每个元素形如 {"name":"装备名","category":品类码,"weightGrams":克重或null,"priority":1或2或3,"reason":"一句话中文理由"}；
            3. category 品类码：1背包 2帐篷天幕 3睡袋防潮垫 4鞋袜 5服装 6炊具饮水 7照明 8导航 9安全急救 10电子设备 11其他；
            4. priority：1 必带 2 建议 3 可选，单项用途决定，不超过 20 条；
            5. 用户已有装备列表中的物品不要重复推荐；weightGrams 估不准就给 null，不要编造。""";

    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final int timeoutSeconds;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiCompatGearAdvisor(
            @Value("${llm.base-url:https://api.deepseek.com}") String baseUrl,
            @Value("${llm.api-key:}") String apiKey,
            @Value("${llm.model:deepseek-chat}") String model,
            @Value("${llm.timeout-seconds:30}") int timeoutSeconds) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.replaceAll("/+$", "");
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        this.timeoutSeconds = timeoutSeconds;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public boolean available() {
        return !apiKey.isEmpty();
    }

    @Override
    public Optional<GearAdvice> advise(GearAdviceContext context) {
        if (!available()) {
            return Optional.empty();
        }
        try {
            String content = chatCompletion(buildUserPrompt(context));
            List<GearAdviceItem> items = parseItems(content);
            if (items.isEmpty()) {
                log.warn("LLM 装备建议输出为空，model={}", model);
                return Optional.empty();
            }
            return Optional.of(new GearAdvice("LLM", model, items, List.of(
                    "最重的装备贴近背部中上部，行走重心最稳",
                    "睡袋、备用衣物压包底，到营才取",
                    "急救包和头灯放顶袋，伸手可及")));
        } catch (Exception e) {
            log.warn("LLM 装备建议失败，降级规则引擎：{}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 用户提示词：线路上下文 + 行程参数 + 现有装备去重清单 + 自由补充
     */
    private String buildUserPrompt(GearAdviceContext context) {
        StringBuilder sb = new StringBuilder("行程信息：\n");
        sb.append("- 行程天数：").append(context.days()).append(" 天\n");
        sb.append("- 出行月份：").append(context.month()).append(" 月\n");
        if (context.routeName() != null) {
            sb.append("- 线路：").append(context.routeName()).append("\n");
        }
        if (context.distanceKm() != null) {
            sb.append("- 计划里程：").append(context.distanceKm()).append(" km\n");
        }
        if (context.elevationGainM() != null) {
            sb.append("- 累计爬升：").append(context.elevationGainM()).append(" m\n");
        }
        if (context.durationMin() != null) {
            sb.append("- 历史参考时长：").append(context.durationMin()).append(" 分钟\n");
        }
        if (context.extra() != null && !context.extra().isBlank()) {
            sb.append("- 特别说明：").append(context.extra()).append("\n");
        }
        sb.append("- 用户已有装备：");
        sb.append(context.ownedItemNames().isEmpty() ? "（暂无记录）" : String.join("、", context.ownedItemNames()));
        sb.append("\n请只推荐清单里没有的、这次行程需要带的装备。");
        return sb.toString();
    }

    /**
     * 调用 OpenAI 兼容 /chat/completions，取首个 choice 的 message.content
     */
    private String chatCompletion(String userPrompt) throws Exception {
        String requestBody = objectMapper.writeValueAsString(java.util.Map.of(
                "model", model,
                "temperature", 0.4,
                "messages", List.of(
                        java.util.Map.of("role", "system", "content", SYSTEM_PROMPT),
                        java.util.Map.of("role", "user", "content", userPrompt))));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("LLM HTTP " + response.statusCode() + ": "
                    + response.body().substring(0, Math.min(200, response.body().length())));
        }
        JsonNode node = objectMapper.readTree(response.body());
        JsonNode content = node.path("choices").path(0).path("message").path("content");
        if (content.isMissingNode() || content.asText().isBlank()) {
            throw new IllegalStateException("LLM 响应缺少 choices[0].message.content");
        }
        return content.asText();
    }

    /**
     * 健壮解析：剥掉 markdown 围栏/前后杂文，取首个 '[' 到最后一个 ']'，逐条校验钳制
     */
    private List<GearAdviceItem> parseItems(String content) {
        int start = content.indexOf('[');
        int end = content.lastIndexOf(']');
        if (start < 0 || end <= start) {
            throw new IllegalStateException("LLM 输出中找不到 JSON 数组");
        }
        List<JsonNode> nodes;
        try {
            nodes = objectMapper.readValue(content.substring(start, end + 1),
                    new TypeReference<List<JsonNode>>() {
                    });
        } catch (Exception e) {
            throw new IllegalStateException("LLM 输出 JSON 解析失败", e);
        }
        List<GearAdviceItem> items = new ArrayList<>();
        for (JsonNode node : nodes) {
            String name = node.path("name").asText("").trim();
            if (name.isEmpty() || name.length() > 100) {
                continue;
            }
            int category = node.path("category").asInt(11);
            if (category < 1 || category > 11) {
                category = 11;
            }
            Integer weight = node.path("weightGrams").isNumber()
                    ? Math.max(0, Math.min(30000, node.path("weightGrams").asInt()))
                    : null;
            int priority = Math.max(1, Math.min(3, node.path("priority").asInt(2)));
            String reason = node.path("reason").asText("").trim();
            if (reason.length() > 200) {
                reason = reason.substring(0, 200);
            }
            items.add(new GearAdviceItem(name, category, weight, priority, reason));
            if (items.size() >= 20) {
                break;
            }
        }
        return items;
    }
}
