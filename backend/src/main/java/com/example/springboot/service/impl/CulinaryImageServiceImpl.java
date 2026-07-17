package com.example.springboot.service.impl;

import com.example.springboot.service.CulinaryImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CulinaryImageServiceImpl implements CulinaryImageService {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.image.options.model}")
    private String modelId;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<String> processAndGenerate(String dishName, List<String> stepsText) {
        List<String> imageUrls = new ArrayList<>();

        // 1. 基础校验：如果步骤列表为空，直接返回空结果
        if (stepsText == null || stepsText.isEmpty()) {
            System.out.println("接收到的步骤列表为空");
            return imageUrls;
        }

        System.out.println("===== 开始处理步骤列表，共 " + stepsText.size() + " 条 =====");

        String url = "https://ark.cn-beijing.volces.com/api/v3/images/generations";

        // 2. 直接遍历前端传来的数组
        for (String step : stepsText) {
            String cleanStep = step.trim();
            if (cleanStep.length() < 3) continue;
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", modelId); // 这里就是你的 ep-xxxxxx
                String prompt =
                        "你是一位精通中国古代饮食文化、传统烹饪技法以及现代美食摄影的专业厨师。\n" +

                                "【任务】\n" +
                                "根据给定的‘单个烹饪步骤’，生成一个真实、合理、可食用的中国菜制作画面。\n\n" +

                                "【输入信息】\n" +
                                "菜名：" + dishName + "\n" +
                                "当前步骤：" + cleanStep + "\n\n" +

                                "【关键理解（必须执行）】\n" +
                                "1. 当前仅为一个步骤，需要推断完整菜品。\n" +
                                "2. 若菜名为古文或模糊描述，必须转化为现代合理菜品。\n" +
                                "3. 判断该步骤属于：准备 / 切配 / 加热 / 成品阶段。\n" +
                                "4. 只呈现当前步骤，不得提前生成成品。\n\n" +

                                "【语义纠偏规则（非常重要）】\n" +
                                "必须将模糊或古文表达转化为“合理食材形态”：\n" +
                                "1. “一个蛋” → 应理解为打散或用于烹饪的鸡蛋（如蛋液或蒸蛋），而不是完整生鸡蛋摆盘。\n" +
                                "2. “粉团” → 指面粉/糯米粉揉成的面团，而不是粉色团子。\n" +
                                "3. “青团” → 应为艾草糯米团（绿色来自植物），不是人工染色或奇怪颜色。\n" +
                                "4. 所有颜色必须来源合理（食材本身或烹饪过程）。\n" +
                                "5. 不允许出现“字面误解”的食物形态。\n\n" +

                                "【烹饪合理性约束】\n" +
                                "1. 食材必须真实、常见、可食用。\n" +
                                "2. 状态必须符合步骤（生/熟/加热中）。\n" +
                                "3. 烹饪方式必须符合中国传统（炒、蒸、煮等）。\n" +
                                "4. 禁止出现抽象、变形、不可食用内容。\n\n" +

                                "【画面要求】\n" +
                                "1. 聚焦当前步骤（例如切菜、翻炒、蒸制）。\n" +
                                "2. 使用中式厨具（铁锅、蒸笼、案板等）。\n" +
                                "3. 场景为真实中国厨房。\n" +
                                "4. 食物外观必须正常、干净、有食欲。\n\n" +

                                "【视觉风格】\n" +
                                "写实风格，美食摄影，自然光，高细节，真实质感，4K。\n\n" +

                                "【兜底机制】\n" +
                                "如果语义存在歧义：\n" +
                                "→ 优先选择“最常见、最合理、最接近真实中国菜”的解释\n" +
                                "→ 宁可普通，也必须正确\n\n" +

                                "【最终目标】\n" +
                                "生成一个符合真实烹饪逻辑、没有歧义、看起来正常且美味的中国菜制作画面。";
                requestBody.put("prompt", prompt);
                requestBody.put("size", "2K");
                requestBody.put("n", 1);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
                    String imageUrl = (String) data.get(0).get("url");
                    imageUrls.add(imageUrl);
                } else {
                    imageUrls.add("IMAGE_GEN_FAILED");
                }

                Thread.sleep(1000);

            } catch (Exception e) {
                System.err.println("步骤 [" + cleanStep + "] 生成失败: " + e.getMessage());
                imageUrls.add("IMAGE_GEN_FAILED");
            }
        }
        return imageUrls;
    }
}