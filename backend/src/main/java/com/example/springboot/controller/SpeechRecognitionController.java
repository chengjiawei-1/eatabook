package com.example.springboot.controller;

import com.example.springboot.config.XfyunSpeechProperties;
import com.example.springboot.service.XfyunSpeechRecognitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Tag(name = "SpeechRecognitionController", description = "Speech recognition APIs")
@CrossOrigin
@RestController
@RequestMapping("/api/speech")
public class SpeechRecognitionController {

    private static final Logger log = LoggerFactory.getLogger(SpeechRecognitionController.class);

    private final XfyunSpeechRecognitionService recognitionService;
    private final XfyunSpeechProperties properties;

    public SpeechRecognitionController(XfyunSpeechRecognitionService recognitionService,
                                       XfyunSpeechProperties properties) {
        this.recognitionService = recognitionService;
        this.properties = properties;
    }

    @Operation(summary = "Recognize Chinese speech", description = "Recognize 16k 16bit mono PCM audio by Xfyun")
    @PostMapping(value = "/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> recognize(@RequestParam("audio") MultipartFile audio) throws Exception {
        if (audio == null || audio.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "audio is required");
        }
        log.info("Speech recognize request received, fileName={}, size={}", audio.getOriginalFilename(), audio.getSize());
        String text = recognitionService.recognize(audio.getBytes());
        log.info("Speech recognize request completed, textLength={}", text == null ? 0 : text.length());
        return Map.of(
                "text", text,
                "provider", "xfyun",
                "sampleRate", properties.getSampleRate()
        );
    }

    @GetMapping("/config/status")
    public Map<String, Object> status() {
        return Map.of(
                "provider", "xfyun",
                "configured", properties.isConfigured(),
                "endpoint", properties.getEndpoint(),
                "sampleRate", properties.getSampleRate()
        );
    }
}
