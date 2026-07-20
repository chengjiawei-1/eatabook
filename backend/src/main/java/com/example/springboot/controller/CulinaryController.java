package com.example.springboot.controller;

import com.example.springboot.dto.CulinaryRequest;
import com.example.springboot.dto.CulinaryResponse;
import com.example.springboot.service.CulinaryImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/culinary")
@CrossOrigin(origins = "*")
@Tag(name = "Culinary images", description = "Culinary image generation APIs")
public class CulinaryController {

    private final CulinaryImageService culinaryImageService;

    public CulinaryController(CulinaryImageService culinaryImageService) {
        this.culinaryImageService = culinaryImageService;
    }

    @Operation(
            summary = "Generate culinary step images",
            description = "Use full translated context to plan image prompts first, then generate step images."
    )
    @PostMapping("/step-images")
    public CulinaryResponse generateImages(@RequestBody CulinaryRequest request) {
        CulinaryResponse response = new CulinaryResponse();

        try {
            if (request == null) {
                response.setSuccess(false);
                response.setMessage("request body is required");
                return response;
            }

            if (!hasText(request.getSourceText())
                    && (request.getStepsText() == null || request.getStepsText().isEmpty())) {
                response.setSuccess(false);
                response.setMessage("sourceText or stepsText is required");
                return response;
            }

            List<String> urls = culinaryImageService.processAndGenerate(
                    request.getDishName(),
                    request.getSourceText(),
                    request.getStepsText(),
                    request.getMaxImages()
            );

            response.setSuccess(true);
            response.setImageUrls(urls);
            response.setMessage("成功生成 " + urls.size() + " 张步骤图");
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("生图过程出现异常: " + e.getMessage());
        }

        return response;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
