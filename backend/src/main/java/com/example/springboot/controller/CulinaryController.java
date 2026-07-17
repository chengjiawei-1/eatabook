package com.example.springboot.controller;

import com.example.springboot.dto.CulinaryRequest;
import com.example.springboot.dto.CulinaryResponse;
import com.example.springboot.service.CulinaryImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/culinary")
@CrossOrigin(origins = "*")
@Tag(name = "Culinary images", description = "图像生成相关接口")
public class CulinaryController {

    private final CulinaryImageService culinaryImageService;
    public CulinaryController(CulinaryImageService culinaryImageService) {
        this.culinaryImageService = culinaryImageService;
    }

    @Operation(summary = "根据步骤生成图片", description = "根据步骤依次将文本给到生图api")
    @PostMapping("/step-images")
    public CulinaryResponse generateImages(@RequestBody CulinaryRequest request) {
        CulinaryResponse response = new CulinaryResponse();

        try {
            // 校验：检查 List 是否为空
            if (request.getStepsText() == null || request.getStepsText().isEmpty()) {
                response.setSuccess(false);
                response.setMessage("错误：步骤列表为空");
                return response;
            }

            // 直接传递 List
            List<String> urls = culinaryImageService.processAndGenerate(
                    request.getDishName(),
                    request.getStepsText() // 传入数组
            );

            response.setSuccess(true);
            response.setImageUrls(urls);
            response.setMessage("成功生成 " + urls.size() + " 张步骤图");

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("生图过程中出现异常: " + e.getMessage());
        }

        return response;
    }
}
