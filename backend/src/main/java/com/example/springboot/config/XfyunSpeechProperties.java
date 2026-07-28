package com.example.springboot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "xfyun.speech")
public class XfyunSpeechProperties {

    private String appId = "";
    private String apiKey = "";
    private String apiSecret = "";
    private String endpoint = "wss://iat.xf-yun.com/v1";
    private String domain = "slm";
    private String language = "zh_cn";
    private String accent = "mandarin";
    private String format = "audio/L16;rate=16000";
    private int sampleRate = 16000;
    private int maxAudioBytes = 1920000;
    private int chunkSize = 1280;
    private int frameIntervalMillis = 40;
    private int timeoutSeconds = 90;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getAccent() {
        return accent;
    }

    public void setAccent(String accent) {
        this.accent = accent;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getMaxAudioBytes() {
        return maxAudioBytes;
    }

    public void setMaxAudioBytes(int maxAudioBytes) {
        this.maxAudioBytes = maxAudioBytes;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getFrameIntervalMillis() {
        return frameIntervalMillis;
    }

    public void setFrameIntervalMillis(int frameIntervalMillis) {
        this.frameIntervalMillis = frameIntervalMillis;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isConfigured() {
        return hasText(appId) && hasText(apiKey) && hasText(apiSecret) && hasText(endpoint);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
