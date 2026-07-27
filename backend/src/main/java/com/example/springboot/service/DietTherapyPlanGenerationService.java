package com.example.springboot.service;

import com.example.springboot.entity.Dishes;
import com.example.springboot.entity.User;
import com.example.springboot.entity.UserConstitutionTests;
import com.example.springboot.mapper.DishesMapper;
import com.example.springboot.mapper.UserConstitutionTestsMapper;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.rag.RagSearchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DietTherapyPlanGenerationService {

    private static final List<String> BREAKFAST_KINDS = List.of("主食面点类", "粥类", "糊类", "茶水类", "点心类");
    private static final List<String> LUNCH_KINDS = List.of("主菜类", "羹类", "汤类", "其他");
    private static final List<String> DINNER_KINDS = List.of("蔬菜类", "点心类", "小菜类");

    private static final Pattern DISH_ID_PATTERN = Pattern.compile("(?:dish_id|菜品唯一 ID|菜品ID|id)\\s*[:：]\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DISH_NAME_PATTERN = Pattern.compile("(?:dish_name|菜品名称|菜名)\\s*[:：]\\s*([^;\\n]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern KIND_PATTERN = Pattern.compile("(?:kind|分类|类别)\\s*[:：]\\s*([^;\\n]+)", Pattern.CASE_INSENSITIVE);

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    private final UserConstitutionTestsMapper constitutionTestsMapper;
    private final DishesMapper dishesMapper;
    private final RagSearchService ragSearchService;
    private final UserMealPlanService userMealPlanService;
    private final AiRequestLimiter aiRequestLimiter;
    private final OpenAiCompatibleChatService openAiCompatibleChatService;

    @Value("${app.diet-therapy-plan.extraction-model:Qwen/Qwen3-8B}")
    private String extractionModel;

    @Value("${app.diet-therapy-plan.plan-model:deepseek-ai/DeepSeek-V4-Flash}")
    private String planModel;

    public DietTherapyPlanGenerationService(ChatClient.Builder chatClientBuilder,
                                            ObjectMapper objectMapper,
                                            UserMapper userMapper,
                                            UserConstitutionTestsMapper constitutionTestsMapper,
                                            DishesMapper dishesMapper,
                                            RagSearchService ragSearchService,
                                            UserMealPlanService userMealPlanService,
                                            AiRequestLimiter aiRequestLimiter,
                                            OpenAiCompatibleChatService openAiCompatibleChatService) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.userMapper = userMapper;
        this.constitutionTestsMapper = constitutionTestsMapper;
        this.dishesMapper = dishesMapper;
        this.ragSearchService = ragSearchService;
        this.userMealPlanService = userMealPlanService;
        this.aiRequestLimiter = aiRequestLimiter;
        this.openAiCompatibleChatService = openAiCompatibleChatService;
    }

    public Map<String, Object> generate(Integer userId, String prompt, Boolean save, Integer topK) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("user not found: " + userId);
        }

        UserConstitutionTests latestTest = constitutionTestsMapper.selectLatestByUserId(userId);
        JsonNode extracted = extractProfile(user, latestTest, prompt);
        extracted = withFallbacks(extracted, user, latestTest);

        List<String> queries = buildRagQueries(extracted);
        List<DishCandidate> candidates = searchDishCandidates(queries, resolveTopK(topK));
        ObjectNode plan = generatePlanJson(extracted, candidates);
        ObjectNode normalizedPlan = normalizePlan(plan, candidates);

        List<Integer> allDishIds = collectPlanDishIds(normalizedPlan);
        if (allDishIds.size() < 21) {
            throw new IllegalStateException("failed to generate a complete seven-day meal plan");
        }
        Map<Integer, Dishes> dishDetails = loadDishDetails(allDishIds);

        if (Boolean.TRUE.equals(save)) {
            userMealPlanService.savePlan(
                    userId,
                    normalizedPlan.toString(),
                    textValue(extracted, "primary")
            );
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("extracted", objectMapper.convertValue(extracted, Map.class));
        result.put("queries", queries);
        result.put("candidateCount", candidates.size());
        result.put("plan", objectMapper.convertValue(normalizedPlan, Map.class));
        result.put("dishes", allDishIds.stream()
                .map(dishDetails::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        return result;
    }

    private JsonNode extractProfile(User user, UserConstitutionTests latestTest, String prompt) {
        JsonNode promptProfile = parseStructuredPrompt(prompt);
        if (promptProfile != null) {
            return promptProfile;
        }
        String response = callModel(buildExtractionPrompt(user, latestTest, prompt), 0.0, 800, extractionModel);
        return readJsonObject(response);
    }

    private JsonNode parseStructuredPrompt(String prompt) {
        if (!hasText(prompt)) {
            return null;
        }
        try {
            JsonNode input = objectMapper.readTree(prompt);
            if (!input.isObject()) {
                return null;
            }

            ObjectNode node = objectMapper.createObjectNode();
            copyText(input, node, "primary", "primary");
            copyText(input, node, "age", "age");
            copyText(input, node, "location", "location");
            copyText(input, node, "preferences", "preferences");

            JsonNode secondaryValue = input.get("secondary");
            ArrayNode secondary = objectMapper.createArrayNode();
            if (secondaryValue != null && secondaryValue.isArray()) {
                for (JsonNode item : secondaryValue) {
                    if (hasText(item.asText())) {
                        secondary.add(item.asText().trim());
                    }
                }
            } else if (secondaryValue != null && hasText(secondaryValue.asText())) {
                splitConstitutions(secondaryValue.asText()).forEach(secondary::add);
            }
            node.set("secondary", secondary);

            applyCurrentSeason(node);
            return node;
        } catch (Exception ignored) {
            return null;
        }
    }

    private void copyText(JsonNode source, ObjectNode target, String sourceField, String targetField) {
        JsonNode value = source.get(sourceField);
        if (value == null || value.isNull()) {
            return;
        }
        if (value.isNumber() && "age".equals(targetField)) {
            target.put(targetField, value.asInt());
            return;
        }
        String text = value.asText("");
        if (hasText(text)) {
            target.put(targetField, text.trim());
        }
    }

    private void applyCurrentSeason(ObjectNode node) {
        int month = LocalDate.now().getMonthValue();
        if (month >= 3 && month <= 5) {
            node.put("season_spring", 1);
        } else if (month >= 6 && month <= 8) {
            node.put("season_summer", 1);
        } else if (month >= 9 && month <= 11) {
            node.put("season_autumn", 1);
        } else {
            node.put("season_winter", 1);
        }
    }

    private String buildExtractionPrompt(User user, UserConstitutionTests latestTest, String prompt) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("userId", user.getUserId());
        profile.put("age", ageOf(user));
        profile.put("nativePlace", user.getNativePlace());
        profile.put("city", user.getCity());
        profile.put("preferences", user.getContent());
        if (latestTest != null) {
            profile.put("mainConstitution", latestTest.getMainConstitution());
            profile.put("subConstitution", latestTest.getSubConstitution());
            profile.put("region", latestTest.getRegion());
            profile.put("ageAvoid", latestTest.getAgeAvoid());
            profile.put("ageFocus", latestTest.getAgeFocus());
            profile.put("foodStaple", latestTest.getFoodStaple());
            profile.put("foodVeg", latestTest.getFoodVeg());
            profile.put("foodMeat", latestTest.getFoodMeat());
            profile.put("daily", latestTest.getDaily());
        }

        return """
                Role
                你是一名高精度结构化信息抽取专家，专门从中医膳食咨询或体质描述类文本中，抽取体质、用户信息与季节信息，并输出单层、可直接解析的 JSON。
                你不进行解释、分析或建议，只输出结构化结果。

                Task
                从输入中抽取：
                primary, secondary, age, location, preferences, season_spring/season_summer/season_autumn/season_winter。

                Rules
                primary 最多一个；secondary 是数组，不重复 primary。
                age 未知输出 null；location 未知输出 null；preferences 未明确输出 null。
                季节字段只输出值为 1 的字段，最多一个。
                禁止编造，禁止自然语言，必须严格单层 JSON。

                输出格式：
                {"primary":"主体质名","secondary":["次体质A"],"age":21,"location":"地区名","preferences":"偏好","season_summer":1}

                数据库用户资料：
                %s

                用户补充输入：
                %s
                """.formatted(toJson(profile), prompt == null ? "" : prompt);
    }

    private JsonNode withFallbacks(JsonNode extracted, User user, UserConstitutionTests latestTest) {
        ObjectNode node = extracted == null || !extracted.isObject()
                ? objectMapper.createObjectNode()
                : ((ObjectNode) extracted).deepCopy();

        if (!hasText(textValue(node, "primary")) && latestTest != null && hasText(latestTest.getMainConstitution())) {
            node.put("primary", latestTest.getMainConstitution().trim());
        }
        if (!node.has("secondary") || !node.get("secondary").isArray()) {
            ArrayNode secondary = objectMapper.createArrayNode();
            if (latestTest != null && hasText(latestTest.getSubConstitution())) {
                splitConstitutions(latestTest.getSubConstitution()).forEach(secondary::add);
            }
            node.set("secondary", secondary);
        }
        if (!node.has("age") || node.get("age").isNull()) {
            Integer age = ageOf(user);
            if (age == null) {
                node.putNull("age");
            } else {
                node.put("age", age);
            }
        }
        if (!hasText(textValue(node, "location"))) {
            String location = joinNonBlank(user.getNativePlace(), user.getCity());
            if (hasText(location)) {
                node.put("location", location);
            } else {
                node.putNull("location");
            }
        }
        if (!node.has("preferences")) {
            if (hasText(user.getContent())) {
                node.put("preferences", user.getContent().trim());
            } else {
                node.putNull("preferences");
            }
        }
        return node;
    }

    private List<String> buildRagQueries(JsonNode extracted) {
        String primary = textValue(extracted, "primary");
        String preferences = textValue(extracted, "preferences");
        String season = seasonText(extracted);
        String secondary = secondaryText(extracted);

        List<String> queries = new ArrayList<>();
        addMealQueries(queries, primary, preferences, season);
        if (hasText(secondary)) {
            addMealQueries(queries, secondary, preferences, season);
        }
        return queries.stream().filter(this::hasText).toList();
    }

    private void addMealQueries(List<String> queries, String constitution, String preferences, String season) {
        if (!hasText(constitution)) {
            return;
        }
        queries.add(String.join("，", BREAKFAST_KINDS) + "，" + constitution + nullToEmpty(preferences) + " " + season);
        queries.add(String.join("，", LUNCH_KINDS) + "，" + constitution + nullToEmpty(preferences) + " " + season);
        queries.add(String.join("，", DINNER_KINDS) + "，" + constitution + nullToEmpty(preferences) + " " + season);
    }

    private List<DishCandidate> searchDishCandidates(List<String> queries, int topK) {
        Map<Integer, DishCandidate> byId = new LinkedHashMap<>();
        Map<String, DishCandidate> byName = new LinkedHashMap<>();

        for (String query : queries) {
            List<Document> documents = ragSearchService.search(query, topK, 0.0);
            for (Document document : documents) {
                DishCandidate candidate = toDishCandidate(document);
                if (candidate == null) {
                    continue;
                }
                if (candidate.dishId() != null) {
                    byId.putIfAbsent(candidate.dishId(), candidate);
                } else if (hasText(candidate.dishName())) {
                    byName.putIfAbsent(candidate.dishName(), candidate);
                }
            }
        }

        List<DishCandidate> candidates = new ArrayList<>(byId.values());
        candidates.addAll(byName.values());
        return candidates;
    }

    private DishCandidate toDishCandidate(Document document) {
        String content = document.getText();
        if (!hasText(content)) {
            return null;
        }

        Integer dishId = matchInt(DISH_ID_PATTERN, content);
        String dishName = matchText(DISH_NAME_PATTERN, content);
        String kind = matchText(KIND_PATTERN, content);

        if (dishId == null && !hasText(dishName)) {
            return null;
        }

        return new DishCandidate(
                dishId,
                dishName,
                kind,
                content,
                document.getMetadata(),
                document.getScore()
        );
    }

    private ObjectNode generatePlanJson(JsonNode extracted, List<DishCandidate> candidates) {
        if (candidates.isEmpty()) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("error", "No valid content found");
            return error;
        }

        String response = callModel(buildPlanPrompt(extracted, candidates), 0.1, 1200, planModel);
        JsonNode json = readJsonObject(response);
        if (json != null && json.isObject()) {
            return (ObjectNode) json;
        }
        ObjectNode error = objectMapper.createObjectNode();
        error.put("error", "No valid content found");
        return error;
    }

    private String buildPlanPrompt(JsonNode extracted, List<DishCandidate> candidates) {
        List<Map<String, Object>> candidatePayload = candidates.stream()
                .limit(140)
                .map(candidate -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("content", candidate.content());
                    item.put("metadata", candidate.metadata());
                    item.put("score", candidate.score());
                    return item;
                })
                .toList();

        return """
                你是古法膳食逻辑分发引擎。
                任务：从候选数据的 content 字段中提取 dish_id、dish_name、kind，并为用户生成 7 天食疗计划所需的早餐、午餐、晚餐菜品 ID。

                分类规则：
                breakfast: kind 包含 主食面点类、粥类、糊类、茶水类、点心类。
                lunch: kind 包含 主菜类、羹类、汤类、其他。
                dinner: kind 包含 蔬菜类、点心类、小菜类。
                相同 dish_id 或 dish_name 全天只保留一次。
                优先选择符合用户体质、偏好、季节、地区特征的菜品。
                每一餐必须返回 7 个 dish_id；候选不足时从分类相近候选中补齐。

                严禁输出解释、Markdown、代码块。只输出 JSON：
                {"breakfast":[1,2,3,4,5,6,7],"lunch":[8,9,10,11,12,13,14],"dinner":[15,16,17,18,19,20,21]}

                用户结构化信息：
                %s

                候选数据：
                %s
                """.formatted(extracted.toString(), toJson(candidatePayload));
    }

    private ObjectNode normalizePlan(ObjectNode modelPlan, List<DishCandidate> candidates) {
        if (modelPlan.has("error")) {
            return modelPlan;
        }

        ObjectNode result = objectMapper.createObjectNode();
        Set<Integer> used = new LinkedHashSet<>();
        Set<Integer> allowedIds = candidates.stream()
                .map(DishCandidate::dishId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        result.set("breakfast", normalizeMeal("breakfast", modelPlan, candidates, used, allowedIds));
        result.set("lunch", normalizeMeal("lunch", modelPlan, candidates, used, allowedIds));
        result.set("dinner", normalizeMeal("dinner", modelPlan, candidates, used, allowedIds));
        return result;
    }

    private ArrayNode normalizeMeal(String meal,
                                    ObjectNode modelPlan,
                                    List<DishCandidate> candidates,
                                    Set<Integer> used,
                                    Set<Integer> allowedIds) {
        ArrayNode result = objectMapper.createArrayNode();
        Set<Integer> mealIds = new LinkedHashSet<>();

        JsonNode array = modelPlan.get(meal);
        if (array != null && array.isArray()) {
            for (JsonNode item : array) {
                Integer id = item.canConvertToInt() ? item.asInt() : null;
                if (id != null && allowedIds.contains(id) && !used.contains(id) && mealIds.add(id)) {
                    result.add(id);
                    used.add(id);
                }
                if (result.size() >= 7) {
                    return result;
                }
            }
        }

        for (DishCandidate candidate : candidates) {
            if (candidate.dishId() == null || used.contains(candidate.dishId())) {
                continue;
            }
            if (matchesMeal(meal, candidate.kind()) && mealIds.add(candidate.dishId())) {
                result.add(candidate.dishId());
                used.add(candidate.dishId());
            }
            if (result.size() >= 7) {
                return result;
            }
        }

        for (DishCandidate candidate : candidates) {
            if (candidate.dishId() == null || used.contains(candidate.dishId())) {
                continue;
            }
            if (mealIds.add(candidate.dishId())) {
                result.add(candidate.dishId());
                used.add(candidate.dishId());
            }
            if (result.size() >= 7) {
                break;
            }
        }
        return result;
    }

    private boolean matchesMeal(String meal, String kind) {
        if (!hasText(kind)) {
            return "lunch".equals(meal);
        }
        List<String> kinds = switch (meal) {
            case "breakfast" -> BREAKFAST_KINDS;
            case "dinner" -> DINNER_KINDS;
            default -> LUNCH_KINDS;
        };
        return kinds.stream().anyMatch(kind::contains);
    }

    private List<Integer> collectPlanDishIds(ObjectNode plan) {
        List<Integer> ids = new ArrayList<>();
        for (String meal : List.of("breakfast", "lunch", "dinner")) {
            JsonNode array = plan.get(meal);
            if (array == null || !array.isArray()) {
                continue;
            }
            for (JsonNode item : array) {
                if (item.canConvertToInt()) {
                    ids.add(item.asInt());
                }
            }
        }
        return ids;
    }

    private Map<Integer, Dishes> loadDishDetails(List<Integer> dishIds) {
        if (dishIds.isEmpty()) {
            return Map.of();
        }
        return dishesMapper.selectBatchIds(dishIds).stream()
                .collect(Collectors.toMap(Dishes::getDishId, dish -> dish, (left, right) -> left, LinkedHashMap::new));
    }

    private String callModel(String prompt, double temperature, int maxTokens, String model) {
        OpenAiChatOptions.Builder options = OpenAiChatOptions.builder()
                .temperature(temperature)
                .maxTokens(maxTokens)
                .streamUsage(false);
        if (hasText(model)) {
            options.model(model.trim());
        }
        String content = aiRequestLimiter.execute(() ->
                openAiCompatibleChatService.callProvider("deepseek", model, temperature, null, maxTokens, prompt));
        return content == null ? "" : content.trim();
    }

    private JsonNode readJsonObject(String content) {
        if (!hasText(content)) {
            return objectMapper.createObjectNode();
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
            return objectMapper.readTree(text);
        } catch (JsonProcessingException e) {
            return objectMapper.createObjectNode();
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }

    private Integer matchInt(Pattern pattern, String content) {
        String text = matchText(pattern, content);
        if (!hasText(text)) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String matchText(Pattern pattern, String content) {
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private Integer ageOf(User user) {
        if (user == null || user.getBirthday() == null) {
            return null;
        }
        return Period.between(user.getBirthday(), LocalDate.now()).getYears();
    }

    private int resolveTopK(Integer topK) {
        return topK == null ? 20 : Math.max(1, Math.min(topK, 20));
    }

    private String textValue(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? "" : value.asText("");
    }

    private String secondaryText(JsonNode extracted) {
        JsonNode secondary = extracted == null ? null : extracted.get("secondary");
        if (secondary == null || !secondary.isArray()) {
            return "";
        }
        List<String> values = new ArrayList<>();
        for (JsonNode item : secondary) {
            if (hasText(item.asText())) {
                values.add(item.asText().trim());
            }
        }
        return String.join("，", values);
    }

    private List<String> splitConstitutions(String value) {
        if (!hasText(value)) {
            return List.of();
        }
        return Arrays.stream(value.split("[、,，/;；\\s]+"))
                .map(String::trim)
                .filter(this::hasText)
                .distinct()
                .toList();
    }

    private String seasonText(JsonNode extracted) {
        if (intValue(extracted, "season_spring") == 1) {
            return "season_spring:1;";
        }
        if (intValue(extracted, "season_summer") == 1) {
            return "season_summer:1;";
        }
        if (intValue(extracted, "season_autumn") == 1) {
            return "season_autumn:1;";
        }
        if (intValue(extracted, "season_winter") == 1) {
            return "season_winter:1;";
        }
        return "";
    }

    private int intValue(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || !value.canConvertToInt() ? 0 : value.asInt();
    }

    private String joinNonBlank(String... values) {
        return Arrays.stream(values)
                .filter(this::hasText)
                .map(String::trim)
                .collect(Collectors.joining(" "));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private boolean hasText(String value) {
        return StringUtils.hasText(value);
    }

    private record DishCandidate(Integer dishId,
                                 String dishName,
                                 String kind,
                                 String content,
                                 Map<String, Object> metadata,
                                 Double score) {
    }
}
