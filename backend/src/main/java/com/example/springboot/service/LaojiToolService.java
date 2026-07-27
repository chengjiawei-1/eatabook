package com.example.springboot.service;

import com.example.springboot.mapper.LaojiToolMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LaojiToolService {

    private static final Logger log = LoggerFactory.getLogger(LaojiToolService.class);

    private final LaojiToolMapper mapper;
    private final ObjectMapper objectMapper;
    private final ChatClient chatClient;
    private final AiRequestLimiter aiRequestLimiter;

    @Value("${app.laoji.tools.max-json-chars:5000}")
    private int maxJsonChars;

    @Value("${app.laoji.intent-model:deepseek-ai/DeepSeek-V4-Flash}")
    private String intentModel;

    @Value("${app.laoji.tools.intent-classifier-enabled:false}")
    private boolean intentClassifierEnabled;

    public LaojiToolService(LaojiToolMapper mapper,
                            ObjectMapper objectMapper,
                            ChatClient.Builder chatClientBuilder,
                            AiRequestLimiter aiRequestLimiter) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.chatClient = chatClientBuilder.build();
        this.aiRequestLimiter = aiRequestLimiter;
    }

    public LaojiDecision decide(String prompt) {
        ToolIntent intent = classifyIntent(prompt);
        return new LaojiDecision(intent.normalConversation(), intent.categories(), intent.source());
    }

    public String collectToolContext(Integer currentUserId, String prompt, Map<String, Object> meta) {
        return collectToolContext(currentUserId, prompt, meta, decide(prompt));
    }

    public String collectToolContext(Integer currentUserId, String prompt, Map<String, Object> meta, LaojiDecision decision) {
        long startedAt = System.currentTimeMillis();
        ToolIntent intent = new ToolIntent(decision.normalConversation(), decision.categories(), decision.source());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("security", Map.of(
                "personal_scope", currentUserId == null ? "anonymous_public_only" : "current_user_only",
                "rule", "个人数据工具只能查询当前登录用户，不能查询、推断或泄露其他用户数据"
        ));
        result.put("intent", Map.of(
                "source", intent.source(),
                "normal_conversation", intent.normalConversation(),
                "categories", intent.categories()
        ));

        if (intent.normalConversation() && !hasSaveMealPlan(meta)) {
            String json = truncate(toJson(result), Math.max(1000, maxJsonChars));
            log.info("Laoji tools skipped userId={} intentSource={} categories={} elapsedMs={}",
                    currentUserId,
                    intent.source(),
                    intent.categories(),
                    System.currentTimeMillis() - startedAt);
            return json;
        }

        if (currentUserId != null) {
            Map<String, Object> latest = null;
            if (needsConstitutionSnapshot(intent)) {
                latest = safe(() -> mapper.selectLatestConstitution(currentUserId));
                result.put("latest_constitution", latest);
            }
            if (needsUserPreference(intent)) {
                result.put("user_preference", safe(() -> mapper.selectUserPreference(currentUserId)));
            }

            if (fastShouldLoadTrend(prompt, intent)) {
                int historyLimit = fastExtractHistoryLimit(prompt);
                result.put("constitution_trend_request", Map.of("limit", historyLimit));
                result.put("constitution_trend", safe(() -> mapper.selectConstitutionTrend(currentUserId, historyLimit)));
            }
            if (fastShouldLoadCollections(prompt, intent)) {
                result.put("user_collections", safe(() -> mapper.selectUserCollections(currentUserId, 12)));
            }
            if (fastShouldLoadMealPlan(prompt, intent)) {
                result.put("current_meal_plan", safe(() -> mapper.selectUserMealPlan(currentUserId)));
            }
            if (fastShouldLoadCheckin(prompt, intent)) {
                result.put("latest_checkin", safe(() -> mapper.selectLatestCheckin(currentUserId)));
                result.put("recent_checkins", safe(() -> mapper.selectRecentCheckins(currentUserId, 7)));
            }
            maybeSaveMealPlan(currentUserId, meta, result);

            String constitution = firstText(
                    textValue(latest, "main_constitution", "mainConstitution"),
                    fastExtractConstitution(prompt)
            );
            if (hasText(constitution) && fastShouldLoadDishes(prompt, intent)) {
                result.put("suitable_dishes", safe(() -> mapper.selectSuitableDishes(constitution, fastExtractKeyword(prompt), 12)));
            }
        } else if (fastShouldLoadDishes(prompt, intent)) {
            result.put("suitable_dishes", safe(() -> mapper.selectSuitableDishes(fastExtractConstitution(prompt), fastExtractKeyword(prompt), 8)));
        }

        String ingredientName = fastExtractIngredientName(prompt);
        if (hasText(ingredientName) || intent.has("ingredient_lookup")) {
            result.put("ingredient_ancient_chapters",
                    safe(() -> mapper.selectIngredientAncientChapters(ingredientName, 8)));
        }

        DishRef dishRef = fastExtractDishRef(meta, prompt);
        if (dishRef.hasValue()) {
            result.put("dish_ingredients",
                    safe(() -> mapper.selectDishIngredients(dishRef.dishId(), dishRef.dishName(), 20)));
        }

        String json = truncate(toJson(result), Math.max(1000, maxJsonChars));
        log.info("Laoji tools collected userId={} intentSource={} categories={} elapsedMs={}",
                currentUserId,
                intent.source(),
                intent.categories(),
                System.currentTimeMillis() - startedAt);
        return json;
    }

    private void maybeSaveMealPlan(Integer currentUserId, Map<String, Object> meta, Map<String, Object> result) {
        if (meta == null || !(meta.get("saveMealPlan") instanceof Map<?, ?> saveMealPlan)) {
            return;
        }
        Object planJson = saveMealPlan.get("planJson");
        Object constitution = saveMealPlan.get("constitution");
        if (planJson == null || !hasText(String.valueOf(constitution))) {
            result.put("save_meal_plan", Map.of("status", "skipped", "reason", "missing planJson or constitution"));
            return;
        }
        String planJsonText = planJson instanceof String text ? text : toJson(planJson);
        if (!hasText(planJsonText) || "null".equalsIgnoreCase(planJsonText.trim())) {
            result.put("save_meal_plan", Map.of("status", "skipped", "reason", "missing planJson or constitution"));
            return;
        }
        int rows = mapper.saveUserMealPlan(currentUserId, planJsonText, String.valueOf(constitution));
        result.put("save_meal_plan", Map.of("status", rows > 0 ? "saved" : "failed"));
    }

    private boolean hasSaveMealPlan(Map<String, Object> meta) {
        return meta != null && meta.get("saveMealPlan") instanceof Map<?, ?>;
    }

    private boolean needsConstitutionSnapshot(ToolIntent intent) {
        return intent.has("meal_plan")
                || intent.has("suitable_dishes");
    }

    private boolean needsUserPreference(ToolIntent intent) {
        return intent.has("user_collections")
                || intent.has("meal_plan")
                || intent.has("suitable_dishes");
    }

    private ToolIntent fastKeywordIntent(String prompt) {
        Set<String> categories = new LinkedHashSet<>();
        if (containsAny(prompt, "趋势", "变化", "最近", "历史", "改善", "变差", "体质变化", "上次", "之前", "测试结果", "测试记录", "前五次", "前5次", "前几次")) {
            categories.add("constitution_trend");
        }
        if (containsAny(prompt, "收藏", "喜欢", "偏好", "爱吃", "忌口", "不吃", "过敏", "口味")) {
            categories.add("user_collections");
        }
        if (containsAny(prompt, "七日", "7日", "一周", "计划", "菜谱", "食谱", "今天吃", "明天吃", "保存方案")) {
            categories.add("meal_plan");
        }
        if (containsAny(prompt, "吃", "菜", "食疗", "食谱", "菜谱", "早餐", "午餐", "晚餐", "调理", "推荐", "方子")) {
            categories.add("suitable_dishes");
        }
        if (containsAny(prompt, "食材", "古籍", "出处", "章节", "本草")) {
            categories.add("ingredient_lookup");
        }
        if (containsAny(prompt, "配料", "需要什么", "有什么材料", "怎么做")) {
            categories.add("dish_ingredients");
        }
        if (containsAny(prompt, "打卡", "签到", "连续", "今天记录", "最近记录", "饮食记录", "坚持了几天")) {
            categories.add("checkin_status");
        }
        return new ToolIntent(categories.isEmpty(), List.copyOf(categories), "keyword_fast");
    }

    private boolean fastShouldLoadTrend(String prompt, ToolIntent intent) {
        return intent.has("constitution_trend") || containsAny(prompt, "趋势", "变化", "最近", "历史", "改善", "变差", "体质变化", "上次", "之前", "测试结果", "测试记录", "前五次", "前5次", "前几次");
    }

    private boolean fastShouldLoadCollections(String prompt, ToolIntent intent) {
        return intent.has("user_collections") || containsAny(prompt, "收藏", "喜欢", "偏好", "爱吃", "忌口", "不吃", "过敏", "口味");
    }

    private boolean fastShouldLoadMealPlan(String prompt, ToolIntent intent) {
        return intent.has("meal_plan") || containsAny(prompt, "七日", "7日", "一周", "计划", "菜谱", "食谱", "今天吃", "明天吃", "保存方案");
    }

    private boolean fastShouldLoadDishes(String prompt, ToolIntent intent) {
        return intent.has("suitable_dishes") || containsAny(prompt, "吃", "菜", "食疗", "食谱", "菜谱", "早餐", "午餐", "晚餐", "调理", "推荐", "方子");
    }

    private boolean fastShouldLoadCheckin(String prompt, ToolIntent intent) {
        return intent.has("checkin_status") || containsAny(prompt, "打卡", "签到", "连续", "今天记录", "最近记录", "饮食记录", "坚持了几天");
    }

    private int fastExtractHistoryLimit(String prompt) {
        if (!hasText(prompt)) {
            return 6;
        }
        Matcher matcher = Pattern.compile("(?:最近|近|前)\\s*([0-9一二三四五六七八九十两]+)\\s*次").matcher(prompt);
        if (!matcher.find()) {
            return 6;
        }
        int value = fastParseChineseNumber(matcher.group(1));
        return value <= 0 ? 6 : Math.max(1, Math.min(value, 20));
    }

    private int fastParseChineseNumber(String value) {
        if (!hasText(value)) {
            return -1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return switch (value) {
                case "一" -> 1;
                case "二", "两" -> 2;
                case "三" -> 3;
                case "四" -> 4;
                case "五" -> 5;
                case "六" -> 6;
                case "七" -> 7;
                case "八" -> 8;
                case "九" -> 9;
                case "十" -> 10;
                default -> -1;
            };
        }
    }

    private String fastExtractConstitution(String prompt) {
        for (String constitution : List.of(
                "平和质", "气虚质", "阳虚质", "阴虚质", "痰湿质", "湿热质", "血瘀质", "气郁质", "特禀质",
                "平和", "气虚", "阳虚", "阴虚", "痰湿", "湿热", "血瘀", "气郁", "特禀")) {
            if (hasText(prompt) && prompt.contains(constitution)) {
                return constitution;
            }
        }
        return "";
    }

    private String fastExtractKeyword(String prompt) {
        if (!hasText(prompt)) {
            return "";
        }
        for (String keyword : List.of("早餐", "午餐", "晚餐", "汤", "粥", "茶", "点心", "主食", "蔬菜", "肉", "清热", "温阳", "补气", "滋阴")) {
            if (prompt.contains(keyword)) {
                return keyword;
            }
        }
        return "";
    }

    private String fastExtractIngredientName(String prompt) {
        if (!hasText(prompt)) {
            return "";
        }
        for (String marker : List.of("食材", "古籍", "出处", "章节", "本草")) {
            int index = prompt.indexOf(marker);
            if (index >= 0) {
                String tail = prompt.substring(Math.min(prompt.length(), index + marker.length())).trim();
                String candidate = tail.replaceAll("^[：:，,。\\s]+", "").split("[，。？?\\s]")[0];
                if (candidate.length() >= 1 && candidate.length() <= 12) {
                    return candidate;
                }
            }
        }
        return "";
    }

    private DishRef fastExtractDishRef(Map<String, Object> meta, String prompt) {
        if (meta != null) {
            Object dishId = meta.get("dishId");
            if (dishId instanceof Number number) {
                return new DishRef(number.intValue(), null);
            }
            Object dishName = meta.get("dishName");
            if (dishName != null && hasText(String.valueOf(dishName))) {
                return new DishRef(null, String.valueOf(dishName));
            }
        }
        if (!containsAny(prompt, "食材", "配料", "需要什么", "有什么材料", "怎么做")) {
            return new DishRef(null, null);
        }
        String cleaned = prompt.replaceAll(".*?(?:菜品|菜|食疗方|食谱)[：: ]*", "");
        String candidate = cleaned.split("[，。？?\\s]")[0];
        return hasText(candidate) && candidate.length() <= 20
                ? new DishRef(null, candidate)
                : new DishRef(null, null);
    }

    private ToolIntent classifyIntent(String prompt) {
        if (!hasText(prompt)) {
            return ToolIntent.normal("empty");
        }
        ToolIntent localIntent = fastKeywordIntent(prompt);
        if (!intentClassifierEnabled || !localIntent.normalConversation()) {
            return localIntent;
        }
        try {
            String content = aiRequestLimiter.execute(() -> chatClient.prompt()
                    .options(OpenAiChatOptions.builder()
                            .model(intentModel)
                            .temperature(0.0)
                            .maxTokens(180)
                            .streamUsage(false)
                            .build())
                    .user(buildIntentPrompt(prompt))
                    .call()
                    .content());
            ToolIntent intent = parseIntent(content);
            return intent == null ? fastKeywordIntent(prompt) : intent;
        } catch (RuntimeException e) {
            return fastKeywordIntent(prompt);
        }
    }

    private String buildIntentPrompt(String prompt) {
        return """
                你是老己聊天的工具意图分类器，只做分类，不回答用户问题。
                判断用户输入属于 normal_conversation，还是需要加载系统业务工具。

                可选 categories：
                constitution_trend: 查询体质测试历史、最近/前几次测试结果、趋势、变化、上次、之前。
                user_collections: 查询收藏、喜欢、偏好、爱吃、忌口、过敏、口味。
                meal_plan: 查询或保存七日计划、一周计划、食谱、菜谱、今天吃、明天吃。
                suitable_dishes: 推荐吃什么、菜品、食疗、早餐、午餐、晚餐、调理方子。
                ingredient_lookup: 查询食材、古籍出处、本草、章节。
                dish_ingredients: 查询某道菜的配料、食材、怎么做、需要什么材料。

                规则：
                1. 只输出 JSON，不要 Markdown。
                2. 正常闲聊、情绪安抚、问候、自我表达输出 normal_conversation=true 且 categories=[]。
                3. 一句话可命中多个 categories。

                输出格式：
                {"normal_conversation":false,"categories":["constitution_trend"]}

                用户输入：
                %s
                """.formatted(prompt);
    }

    private ToolIntent parseIntent(String content) {
        if (!hasText(content)) {
            return null;
        }
        String text = content.trim()
                .replaceFirst("^```json\\s*", "")
                .replaceFirst("^```\\s*", "")
                .replaceFirst("\\s*```$", "");
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            text = text.substring(start, end + 1);
        }
        try {
            JsonNode node = objectMapper.readTree(text);
            Set<String> categories = new LinkedHashSet<>();
            JsonNode array = node.get("categories");
            if (array != null && array.isArray()) {
                for (JsonNode item : array) {
                    if (hasText(item.asText())) {
                        categories.add(item.asText().trim());
                    }
                }
            }
            boolean normal = node.path("normal_conversation").asBoolean(categories.isEmpty());
            return new ToolIntent(normal, List.copyOf(categories), "deepseek");
        } catch (Exception e) {
            return null;
        }
    }

    private ToolIntent keywordIntent(String prompt) {
        Set<String> categories = new LinkedHashSet<>();
        if (containsAny(prompt, "趋势", "变化", "最近", "历史", "改善", "变差", "体质变化", "上次", "之前", "测试结果", "测试记录", "前五次", "前5次", "前几次")) {
            categories.add("constitution_trend");
        }
        if (containsAny(prompt, "收藏", "喜欢", "偏好", "爱吃", "忌口", "不吃", "过敏", "口味")) {
            categories.add("user_collections");
        }
        if (containsAny(prompt, "七日", "7日", "一周", "计划", "菜谱", "食谱", "今天吃", "明天吃", "保存方案")) {
            categories.add("meal_plan");
        }
        if (containsAny(prompt, "吃", "菜", "食疗", "食谱", "菜谱", "早餐", "午餐", "晚餐", "调理", "推荐", "方子")) {
            categories.add("suitable_dishes");
        }
        if (containsAny(prompt, "食材", "古籍", "出处", "章节", "本草")) {
            categories.add("ingredient_lookup");
        }
        if (containsAny(prompt, "配料", "需要什么", "有什么材料", "怎么做")) {
            categories.add("dish_ingredients");
        }
        return new ToolIntent(categories.isEmpty(), List.copyOf(categories), "keyword_fallback");
    }

    private boolean shouldLoadTrend(String prompt, ToolIntent intent) {
        return intent.has("constitution_trend") || containsAny(prompt, "趋势", "变化", "最近", "历史", "改善", "变差", "体质变化", "上次", "之前", "测试结果", "测试记录", "前五次", "前5次", "前几次");
    }

    private boolean shouldLoadCollections(String prompt, ToolIntent intent) {
        return intent.has("user_collections") || containsAny(prompt, "收藏", "喜欢", "偏好", "爱吃", "忌口", "不吃", "过敏", "口味");
    }

    private boolean shouldLoadMealPlan(String prompt, ToolIntent intent) {
        return intent.has("meal_plan") || containsAny(prompt, "七日", "7日", "一周", "计划", "菜谱", "食谱", "今天吃", "明天吃", "保存方案");
    }

    private boolean shouldLoadDishes(String prompt, ToolIntent intent) {
        return intent.has("suitable_dishes") || containsAny(prompt, "吃", "菜", "食疗", "食谱", "菜谱", "早餐", "午餐", "晚餐", "调理", "推荐", "方子");
    }

    private int extractHistoryLimit(String prompt) {
        if (!hasText(prompt)) {
            return 6;
        }
        Matcher matcher = Pattern.compile("(?:最近|近|前)\\s*([0-9一二三四五六七八九十两]+)\\s*次").matcher(prompt);
        if (!matcher.find()) {
            return 6;
        }
        int value = parseChineseNumber(matcher.group(1));
        return value <= 0 ? 6 : Math.max(1, Math.min(value, 20));
    }

    private int parseChineseNumber(String value) {
        if (!hasText(value)) {
            return -1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return switch (value) {
                case "一" -> 1;
                case "二", "两" -> 2;
                case "三" -> 3;
                case "四" -> 4;
                case "五" -> 5;
                case "六" -> 6;
                case "七" -> 7;
                case "八" -> 8;
                case "九" -> 9;
                case "十" -> 10;
                default -> -1;
            };
        }
    }

    private boolean containsAny(String text, String... keywords) {
        if (!hasText(text)) {
            return false;
        }
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String extractConstitution(String prompt) {
        for (String constitution : List.of(
                "平和质", "气虚质", "阳虚质", "阴虚质", "痰湿质", "湿热质", "血瘀质", "气郁质", "特禀质",
                "平和", "气虚", "阳虚", "阴虚", "痰湿", "湿热", "血瘀", "气郁", "特禀")) {
            if (hasText(prompt) && prompt.contains(constitution)) {
                return constitution;
            }
        }
        return "";
    }

    private String extractKeyword(String prompt) {
        if (!hasText(prompt)) {
            return "";
        }
        for (String keyword : List.of("早餐", "午餐", "晚餐", "汤", "粥", "茶", "点心", "主食", "蔬菜", "肉", "清热", "温阳", "补气", "滋阴")) {
            if (prompt.contains(keyword)) {
                return keyword;
            }
        }
        return "";
    }

    private String extractIngredientName(String prompt) {
        if (!hasText(prompt)) {
            return "";
        }
        for (String marker : List.of("食材", "古籍", "出处", "章节", "本草")) {
            int index = prompt.indexOf(marker);
            if (index >= 0) {
                String tail = prompt.substring(Math.min(prompt.length(), index + marker.length())).trim();
                String candidate = tail.replaceAll("^[：:，,。\\s]+", "").split("[，,。？?\\s]")[0];
                if (candidate.length() >= 1 && candidate.length() <= 12) {
                    return candidate;
                }
            }
        }
        return "";
    }

    private DishRef extractDishRef(Map<String, Object> meta, String prompt) {
        if (meta != null) {
            Object dishId = meta.get("dishId");
            if (dishId instanceof Number number) {
                return new DishRef(number.intValue(), null);
            }
            Object dishName = meta.get("dishName");
            if (hasText(String.valueOf(dishName))) {
                return new DishRef(null, String.valueOf(dishName));
            }
        }
        if (!containsAny(prompt, "食材", "配料", "需要什么", "有什么材料", "怎么做")) {
            return new DishRef(null, null);
        }
        String cleaned = prompt.replaceAll(".*?(?:菜品|菜|食疗方|食谱)[：: ]*", "");
        String candidate = cleaned.split("[，,。？?\\s]")[0];
        return hasText(candidate) && candidate.length() <= 20
                ? new DishRef(null, candidate)
                : new DishRef(null, null);
    }

    private String textValue(Map<String, Object> map, String... keys) {
        if (map == null) {
            return "";
        }
        for (String key : keys) {
            if (map.get(key) != null) {
                return String.valueOf(map.get(key));
            }
        }
        return "";
    }

    private String firstText(String first, String second) {
        return hasText(first) ? first : (hasText(second) ? second : "");
    }

    private boolean hasText(String value) {
        return StringUtils.hasText(value);
    }

    private <T> T safe(ToolCall<T> call) {
        try {
            return call.call();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }

    private String truncate(String value, int maxChars) {
        if (value == null || value.length() <= maxChars) {
            return value;
        }
        return value.substring(0, maxChars) + "...";
    }

    @FunctionalInterface
    private interface ToolCall<T> {
        T call();
    }

    private record DishRef(Integer dishId, String dishName) {
        boolean hasValue() {
            return dishId != null || StringUtils.hasText(dishName);
        }
    }

    public record LaojiDecision(boolean normalConversation, List<String> categories, String source) {
        boolean has(String category) {
            return categories != null && categories.contains(category);
        }

        public boolean needsKnowledgeContext() {
            return has("constitution_trend")
                    || has("meal_plan")
                    || has("suitable_dishes")
                    || has("ingredient_lookup")
                    || has("dish_ingredients");
        }
    }

    private record ToolIntent(boolean normalConversation, List<String> categories, String source) {
        static ToolIntent normal(String source) {
            return new ToolIntent(true, List.of(), source);
        }

        boolean has(String category) {
            return categories != null && categories.contains(category);
        }
    }
}
