package com.example.springboot.service;

import com.example.springboot.entity.User;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.rag.RagSearchService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConstitutionAdviceGenerationService {

    private final ChatClient chatClient;
    private final UserMapper userMapper;
    private final RagSearchService ragSearchService;
    private final AiRequestLimiter aiRequestLimiter;

    @Value("${app.constitution-advice.model:Qwen/Qwen3-14B}")
    private String chatModel;

    @Value("${app.constitution-advice.rag-top-k:8}")
    private int ragTopK;

    public ConstitutionAdviceGenerationService(ChatClient.Builder chatClientBuilder,
                                               UserMapper userMapper,
                                               RagSearchService ragSearchService,
                                               AiRequestLimiter aiRequestLimiter) {
        this.chatClient = chatClientBuilder.build();
        this.userMapper = userMapper;
        this.ragSearchService = ragSearchService;
        this.aiRequestLimiter = aiRequestLimiter;
    }

    public ConstitutionAdviceResult generate(ConstitutionAdviceRequest request) {
        if (request == null || request.userId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (!hasText(request.primary())) {
            throw new IllegalArgumentException("primary is required");
        }

        User user = userMapper.selectById(request.userId());
        if (user == null) {
            throw new IllegalArgumentException("user not found: " + request.userId());
        }

        String age = resolveAge(user, request.age());
        String location = firstText(request.location(), joinLocation(user));
        String secondary = request.secondary() == null ? "" : request.secondary();
        String season = request.season() == null ? "" : request.season();

        Map<String, String> contexts = retrieveContexts(request.primary(), secondary, age, location, season);
        String advice = callModel(buildPrompt(request.primary(), secondary, age, location, season, contexts));

        return new ConstitutionAdviceResult(advice, contexts);
    }

    private Map<String, String> retrieveContexts(String primary, String secondary, String age, String location, String season) {
        Map<String, String> contexts = new LinkedHashMap<>();
        contexts.put("constitution", searchContext("中医测试九大体质特点 " + primary + " " + secondary));
        contexts.put("region", searchContext("地域饮食信息 " + location + " " + primary));
        contexts.put("staple", searchContext("食材 谷类 主食 " + primary + " " + age + " " + location + " " + season));
        contexts.put("vegetableFruit", searchContext("食材 蔬菜 水果 菌藻 " + primary + " " + age + " " + location + " " + season));
        contexts.put("meat", searchContext("食材 肉类 禽蛋 水产 " + primary + " " + age + " " + location + " " + season));
        return contexts;
    }

    private String searchContext(String query) {
        return ragSearchService.search(query, Math.max(3, ragTopK), 0.0).stream()
                .map(this::formatDocument)
                .filter(this::hasText)
                .collect(Collectors.joining("\n---\n"));
    }

    private String formatDocument(Document document) {
        String source = String.valueOf(document.getMetadata().getOrDefault("source", ""));
        String text = document.getText();
        if (!hasText(text)) {
            return "";
        }
        return "source=" + source + "\n" + text;
    }

    private String buildPrompt(String primary,
                               String secondary,
                               String age,
                               String location,
                               String season,
                               Map<String, String> contexts) {
        return """
                Role: 中医体质调理专家（三因制宜丰盈版）
                一、角色定位
                你是一位遵循中医经典理论的体质调理专家。通过年龄、地域、体质三位一体逻辑，仅利用知识库中古籍食材输出精准调理建议。

                二、核心决策逻辑
                年龄维度：婴幼儿护脾胃；青少年助发育；成年平阴阳；中老年护脏腑。
                地域维度：根据地域气候偏性（湿、干、寒、热）筛选对冲食材。
                配伍逻辑：遵循君臣佐使，每类食材提供有层次的组合方案。

                三、唯一合法知识来源
                体质判定：只参考 context.constitution。
                食材库：只参考 context.staple、context.vegetableFruit、context.meat。
                地域参考：只参考 context.region。

                四、核心禁令
                严禁生成菜品，只能输出单一食材名。
                严禁输出菜名、菜谱名、制作步骤。
                严禁科普说明、逻辑推导、礼貌废话。
                严禁编造知识库外食材。
                严禁输出过少：主食、蔬果、肉类都必须达到 3-4 项。

                五、输出模板
                必须严格按下述标签格式输出，不得添加额外文字，食材之间用、分隔：
                [体质]{体质名称}
                [地域]{简述地域气候对体质的影响，控制在20-25字}
                [重点]{结合年龄段，详述调理核心生理靶点，控制在20-25字}
                [忌口]{列举至少4类基于年龄与体质需规避的食物属性或具体类目}
                [方案]{核心调理方案，需包含调、补、泄完整逻辑，30-50字}
                [主食]{3-4项食材}
                [蔬果]{3-4项食材}
                [肉类]{3-4项食材}
                [注意]{结合季节与时辰，给出2句具体起居建议}

                用户信息：
                primary=%s
                secondary=%s
                age=%s
                location=%s
                season=%s

                context.constitution:
                %s

                context.region:
                %s

                context.staple:
                %s

                context.vegetableFruit:
                %s

                context.meat:
                %s
                """.formatted(
                primary,
                nullToEmpty(secondary),
                nullToEmpty(age),
                nullToEmpty(location),
                nullToEmpty(season),
                contexts.getOrDefault("constitution", ""),
                contexts.getOrDefault("region", ""),
                contexts.getOrDefault("staple", ""),
                contexts.getOrDefault("vegetableFruit", ""),
                contexts.getOrDefault("meat", "")
        );
    }

    private String callModel(String prompt) {
        OpenAiChatOptions.Builder options = OpenAiChatOptions.builder()
                .temperature(0.2)
                .maxTokens(1200)
                .streamUsage(false);
        if (hasText(chatModel)) {
            options.model(chatModel.trim());
        }
        String content = aiRequestLimiter.execute(() -> chatClient.prompt()
                .options(options.build())
                .user(prompt)
                .call()
                .content());
        return normalizeAdvice(content);
    }

    private String normalizeAdvice(String content) {
        if (content == null) {
            return "";
        }
        return content.trim()
                .replaceFirst("^```[a-zA-Z]*\\s*", "")
                .replaceFirst("\\s*```$", "");
    }

    private String resolveAge(User user, Integer requestAge) {
        if (requestAge != null && requestAge > 0) {
            return String.valueOf(requestAge);
        }
        if (user.getBirthday() == null) {
            return "";
        }
        return String.valueOf(Period.between(user.getBirthday(), LocalDate.now()).getYears());
    }

    private String joinLocation(User user) {
        return List.of(user.getNativePlace(), user.getCity()).stream()
                .filter(this::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.joining(" "));
    }

    private String firstText(String first, String fallback) {
        return hasText(first) ? first.trim() : nullToEmpty(fallback);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private boolean hasText(String value) {
        return StringUtils.hasText(value);
    }

    public record ConstitutionAdviceRequest(Integer userId,
                                            String primary,
                                            String secondary,
                                            Integer age,
                                            String location,
                                            String season) {
    }

    public record ConstitutionAdviceResult(String advice, Map<String, String> contexts) {
    }
}
