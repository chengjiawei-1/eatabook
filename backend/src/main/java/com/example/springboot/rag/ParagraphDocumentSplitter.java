package com.example.springboot.rag;

import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class ParagraphDocumentSplitter {

    private static final Pattern LINE_SEPARATOR = Pattern.compile("\\R+");
    private static final Pattern SENTENCE_BOUNDARY = Pattern.compile("(?<=[。！？.!?；;])");

    private final int targetChunkSizeChars;
    private final int maxChunkSizeChars;
    private final int minChunkSizeChars;

    public ParagraphDocumentSplitter(
            @Value("${app.rag.chunk.default-chunk-size:500}") int targetChunkSizeChars,
            @Value("${app.rag.chunk.max-chunk-size-chars:1200}") int maxChunkSizeChars,
            @Value("${app.rag.chunk.min-chunk-size-chars:100}") int minChunkSizeChars) {
        this.targetChunkSizeChars = Math.max(100, targetChunkSizeChars);
        this.maxChunkSizeChars = Math.max(this.targetChunkSizeChars, maxChunkSizeChars);
        this.minChunkSizeChars = Math.max(1, minChunkSizeChars);
    }

    public List<Document> split(List<Document> documents) {
        List<Document> chunks = new ArrayList<>();
        for (Document document : documents) {
            if (document.getMetadata().containsKey("csv_row_index")) {
                String text = normalize(document.getText());
                if (!text.isEmpty()) {
                    chunks.add(toChunk(document, text, chunks.size()));
                }
                continue;
            }

            List<String> paragraphs = splitParagraphs(document.getText());
            List<String> texts = mergeParagraphs(paragraphs);

            for (String text : texts) {
                if (text.length() < minChunkSizeChars && paragraphs.size() > 1) {
                    continue;
                }

                chunks.add(toChunk(document, text, chunks.size()));
            }
        }
        return chunks;
    }

    private List<String> splitParagraphs(String text) {
        List<String> paragraphs = new ArrayList<>();
        if (!StringUtils.hasText(text)) {
            return paragraphs;
        }

        for (String line : LINE_SEPARATOR.split(text)) {
            String paragraph = normalize(line);
            if (!paragraph.isEmpty()) {
                paragraphs.add(paragraph);
            }
        }
        return paragraphs;
    }

    private List<String> mergeParagraphs(List<String> paragraphs) {
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String paragraph : paragraphs) {
            if (paragraph.length() > maxChunkSizeChars) {
                flush(current, chunks);
                chunks.addAll(splitLongParagraph(paragraph));
                continue;
            }

            if (!current.isEmpty() && current.length() + 2 + paragraph.length() > targetChunkSizeChars) {
                flush(current, chunks);
            }

            if (!current.isEmpty()) {
                current.append("\n\n");
            }
            current.append(paragraph);
        }

        flush(current, chunks);
        return chunks;
    }

    private List<String> splitLongParagraph(String paragraph) {
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String sentence : SENTENCE_BOUNDARY.split(paragraph)) {
            String normalized = normalize(sentence);
            if (normalized.isEmpty()) {
                continue;
            }

            if (!current.isEmpty() && current.length() + normalized.length() > maxChunkSizeChars) {
                flush(current, chunks);
            }

            if (normalized.length() > maxChunkSizeChars) {
                flush(current, chunks);
                splitByLength(normalized, chunks);
            } else {
                current.append(normalized);
            }
        }

        flush(current, chunks);
        return chunks;
    }

    private void splitByLength(String text, List<String> chunks) {
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxChunkSizeChars, text.length());
            chunks.add(text.substring(start, end).trim());
            start = end;
        }
    }

    private void flush(StringBuilder current, List<String> chunks) {
        if (current.isEmpty()) {
            return;
        }

        String text = current.toString().trim();
        if (!text.isEmpty()) {
            chunks.add(text);
        }
        current.setLength(0);
    }

    private Document toChunk(Document document, String text, int chunkIndex) {
        Map<String, Object> metadata = new HashMap<>(document.getMetadata());
        metadata.put("chunk_index", chunkIndex);
        metadata.put("chunk_type", metadata.containsKey("csv_row_index") ? "csv_row" : "paragraph");
        String source = String.valueOf(metadata.getOrDefault("source", document.getId()));

        return document.mutate()
                .id(UUID.nameUUIDFromBytes((source + ":" + chunkIndex + ":" + text).getBytes(StandardCharsets.UTF_8)).toString())
                .text(text)
                .metadata(metadata)
                .build();
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().replaceAll("[ \\t\\x0B\\f\\r]+", " ");
    }
}
