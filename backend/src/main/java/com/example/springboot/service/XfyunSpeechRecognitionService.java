package com.example.springboot.service;

import com.example.springboot.config.XfyunSpeechProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

@Service
public class XfyunSpeechRecognitionService {

    private static final Logger log = LoggerFactory.getLogger(XfyunSpeechRecognitionService.class);

    private final XfyunSpeechProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public XfyunSpeechRecognitionService(XfyunSpeechProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String recognize(byte[] pcmAudio) {
        if (!properties.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Xfyun speech API is not configured");
        }
        if (pcmAudio == null || pcmAudio.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "audio is required");
        }
        if (pcmAudio.length > properties.getMaxAudioBytes()) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "audio is too large");
        }

        log.info("Xfyun speech recognition started, audioBytes={}, sampleRate={}", pcmAudio.length, properties.getSampleRate());
        XfyunListener listener = new XfyunListener(objectMapper);
        try {
            WebSocket webSocket = httpClient.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .buildAsync(URI.create(buildAuthenticatedUrl()), listener)
                    .get(15, TimeUnit.SECONDS);

            sendFrames(webSocket, pcmAudio);
            String text = listener.resultFuture()
                    .get(properties.getTimeoutSeconds(), TimeUnit.SECONDS)
                    .trim();
            log.info("Xfyun speech recognition finished, textLength={}", text.length());
            return text;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Xfyun speech recognition failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Xfyun speech recognition failed: " + e.getMessage(), e);
        }
    }

    private void sendFrames(WebSocket webSocket, byte[] pcmAudio) throws Exception {
        int offset = 0;
        int sequence = 1;
        boolean first = true;
        while (offset < pcmAudio.length) {
            int length = Math.min(properties.getChunkSize(), pcmAudio.length - offset);
            byte[] chunk = new byte[length];
            System.arraycopy(pcmAudio, offset, chunk, 0, length);
            offset += length;

            Map<String, Object> frame = new LinkedHashMap<>();
            if (first) {
                frame.put("header", Map.of(
                        "app_id", properties.getAppId(),
                        "status", 0
                ));
                frame.put("parameter", Map.of(
                        "iat", Map.of(
                                "domain", properties.getDomain(),
                                "language", properties.getLanguage(),
                                "accent", properties.getAccent(),
                                "eos", 6000,
                                "result", Map.of("encoding", "utf8", "compress", "raw", "format", "json")
                        )
                ));
            } else {
                frame.put("header", Map.of(
                        "app_id", properties.getAppId(),
                        "status", 1
                ));
            }
            frame.put("payload", Map.of(
                    "audio", Map.of(
                            "encoding", "raw",
                            "sample_rate", properties.getSampleRate(),
                            "channels", 1,
                            "bit_depth", 16,
                            "seq", sequence,
                            "status", first ? 0 : 1,
                            "audio", Base64.getEncoder().encodeToString(chunk)
                    )
            ));

            webSocket.sendText(objectMapper.writeValueAsString(frame), true).join();
            first = false;
            sequence += 1;
            Thread.sleep(properties.getFrameIntervalMillis());
        }

        Map<String, Object> endFrame = Map.of(
                "header", Map.of("app_id", properties.getAppId(), "status", 2),
                "payload", Map.of("audio", Map.of(
                        "encoding", "raw",
                        "sample_rate", properties.getSampleRate(),
                        "channels", 1,
                        "bit_depth", 16,
                        "seq", sequence,
                        "status", 2,
                        "audio", ""
                ))
        );
        webSocket.sendText(objectMapper.writeValueAsString(endFrame), true).join();
    }

    private String buildAuthenticatedUrl() throws Exception {
        URI endpoint = URI.create(properties.getEndpoint());
        String host = endpoint.getHost();
        String path = endpoint.getRawPath();
        if (path == null || path.isEmpty()) {
            path = "/";
        }

        String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(java.time.ZoneOffset.UTC));
        String signatureOrigin = "host: " + host + "\n"
                + "date: " + date + "\n"
                + "GET " + path + " HTTP/1.1";
        String signature = hmacSha256Base64(signatureOrigin, properties.getApiSecret());
        String authorizationOrigin = "api_key=\"" + properties.getApiKey() + "\", "
                + "algorithm=\"hmac-sha256\", "
                + "headers=\"host date request-line\", "
                + "signature=\"" + signature + "\"";
        String authorization = Base64.getEncoder().encodeToString(authorizationOrigin.getBytes(StandardCharsets.UTF_8));

        String query = "authorization=" + urlEncode(authorization)
                + "&date=" + urlEncode(date)
                + "&host=" + urlEncode(host);
        return properties.getEndpoint() + (properties.getEndpoint().contains("?") ? "&" : "?") + query;
    }

    private String hmacSha256Base64(String data, String secret) throws Exception {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new SignatureException("failed to sign Xfyun request", e);
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static class XfyunListener implements WebSocket.Listener {
        private static final Logger log = LoggerFactory.getLogger(XfyunListener.class);

        private final ObjectMapper objectMapper;
        private final CompletableFuture<String> result = new CompletableFuture<>();
        private final StringBuilder partialText = new StringBuilder();
        private final StringBuilder responseText = new StringBuilder();

        private XfyunListener(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        CompletableFuture<String> resultFuture() {
            return result;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            partialText.append(data);
            if (last) {
                handleMessage(partialText.toString());
                partialText.setLength(0);
            }
            webSocket.request(1);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            result.completeExceptionally(error);
        }

        private void handleMessage(String message) {
            try {
                JsonNode root = objectMapper.readTree(message);
                JsonNode header = root.path("header");
                int code = header.path("code").asInt(0);
                if (code != 0) {
                    String error = header.path("message").asText("Xfyun speech recognition failed");
                    log.warn("Xfyun returned error, code={}, message={}", code, error);
                    result.completeExceptionally(new IllegalStateException(error));
                    return;
                }

                JsonNode text = root.path("payload").path("result").path("text");
                if (!text.isMissingNode() && !text.asText("").isEmpty()) {
                    String decoded = new String(Base64.getDecoder().decode(text.asText()), StandardCharsets.UTF_8);
                    String transcript = parseTranscript(decoded);
                    if (transcript.isEmpty()) {
                        log.info("Xfyun decoded result had no transcript words: {}", decoded);
                    }
                    responseText.append(transcript);
                }

                if (header.path("status").asInt() == 2) {
                    log.info("Xfyun final frame received, accumulatedTextLength={}", responseText.length());
                    result.complete(responseText.toString());
                }
            } catch (Exception e) {
                result.completeExceptionally(e);
            }
        }

        private String parseTranscript(String decodedResult) throws Exception {
            JsonNode root = objectMapper.readTree(decodedResult);
            StringBuilder text = new StringBuilder();
            appendWords(root, text);
            if (text.length() == 0 && root.hasNonNull("text")) {
                text.append(root.path("text").asText(""));
            }
            return text.toString();
        }

        private void appendWords(JsonNode node, StringBuilder text) {
            if (node == null || node.isMissingNode() || node.isNull()) {
                return;
            }

            JsonNode words = node.path("ws");
            if (words.isArray()) {
                for (JsonNode word : words) {
                    JsonNode candidates = word.path("cw");
                    if (candidates.isArray() && !candidates.isEmpty()) {
                        text.append(candidates.get(0).path("w").asText(""));
                    }
                }
            }

            if (node.isObject() || node.isArray()) {
                node.elements().forEachRemaining(child -> appendWords(child, text));
            }
        }
    }
}
