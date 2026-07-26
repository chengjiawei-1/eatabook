package com.example.springboot.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class RagIndexService {

    private static final Logger log = LoggerFactory.getLogger(RagIndexService.class);
    private static final String KNOWLEDGE_BASE_METADATA_KEY = "knowledge_base";
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(".doc", ".docx", ".pdf", ".txt", ".md", ".csv");

    private final VectorStore vectorStore;
    private final ResourcePatternResolver resourcePatternResolver;
    private final ParagraphDocumentSplitter paragraphDocumentSplitter;

    @Value("${app.rag.knowledge-location:classpath*:knowledge/**/*}")
    private String knowledgeLocation;

    @Value("${app.rag.clear-before-index:false}")
    private boolean clearBeforeIndex;

    @Value("${app.rag.knowledge-base-id:default}")
    private String knowledgeBaseId;

    @Value("${app.rag.index-batch-size:25}")
    private int indexBatchSize;

    public RagIndexService(VectorStore vectorStore,
                           ResourcePatternResolver resourcePatternResolver,
                           ParagraphDocumentSplitter paragraphDocumentSplitter) {
        this.vectorStore = vectorStore;
        this.resourcePatternResolver = resourcePatternResolver;
        this.paragraphDocumentSplitter = paragraphDocumentSplitter;
    }

    public RagIndexResult index() {
        LoadedDocuments loaded = loadDocuments();
        List<Document> chunks = paragraphDocumentSplitter.split(loaded.documents());

        if (!chunks.isEmpty()) {
            if (clearBeforeIndex) {
                clearKnowledgeBase();
            }
            addChunksInBatches(chunks);
        }

        return new RagIndexResult(
                loaded.resourceCount(),
                loaded.documents().size(),
                chunks.size(),
                loaded.sources()
        );
    }

    public void clearKnowledgeBase() {
        vectorStore.delete(new FilterExpressionBuilder()
                .eq(KNOWLEDGE_BASE_METADATA_KEY, knowledgeBaseId)
                .build());
    }

    private void addChunksInBatches(List<Document> chunks) {
        int batchSize = Math.max(1, indexBatchSize);
        int totalBatches = (chunks.size() + batchSize - 1) / batchSize;

        for (int start = 0, batchNumber = 1; start < chunks.size(); start += batchSize, batchNumber++) {
            int end = Math.min(start + batchSize, chunks.size());
            List<Document> batch = chunks.subList(start, end);
            log.info("RAG upserting batch {}/{}: chunks {}-{} of {}",
                    batchNumber,
                    totalBatches,
                    start + 1,
                    end,
                    chunks.size());
            vectorStore.add(batch);
        }
    }

    public LoadedDocuments loadDocuments() {
        try {
            Resource[] resources = resourcePatternResolver.getResources(knowledgeLocation);
            List<Document> documents = new ArrayList<>();
            LinkedHashSet<String> sources = new LinkedHashSet<>();
            int resourceCount = 0;

            for (Resource resource : resources) {
                if (!isSupportedResource(resource)) {
                    continue;
                }

                resourceCount++;
                String source = resolveSource(resource);
                List<Document> loaded = loadResource(resource, source);
                for (Document document : loaded) {
                    if (!StringUtils.hasText(document.getText())) {
                        continue;
                    }
                    documents.add(document);
                }
                sources.add(source);
            }

            return new LoadedDocuments(resourceCount, documents, List.copyOf(sources));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load RAG knowledge resources", e);
        }
    }

    private List<Document> loadResource(Resource resource, String source) {
        String filename = resource.getFilename();
        String normalized = filename == null ? "" : filename.toLowerCase(Locale.ROOT);

        try {
            if (normalized.endsWith(".docx")) {
                return loadDocx(resource, source);
            }

            if (normalized.endsWith(".txt") || normalized.endsWith(".md")) {
                return List.of(new Document(readText(resource), sourceMetadata(source)));
            }

            if (normalized.endsWith(".csv")) {
                return loadCsv(resource, source);
            }

            return new TikaDocumentReader(resource).get().stream()
                    .map(document -> withSourceMetadata(document, source))
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load RAG knowledge resource: " + source, e);
        }
    }

    private List<Document> loadCsv(Resource resource, String source) throws IOException {
        List<String> lines = readLines(resource);
        if (lines.isEmpty()) {
            return List.of();
        }

        List<String> headers = parseCsvLine(lines.get(0));
        List<Document> documents = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            List<String> values = parseCsvLine(lines.get(i));
            String row = csvRowToText(headers, values);
            if (StringUtils.hasText(row)) {
                Map<String, Object> metadata = sourceMetadata(source);
                metadata.put("csv_row_index", i);
                documents.add(new Document(row, metadata));
            }
        }

        if (documents.isEmpty()) {
            return List.of(new Document(String.join("\n\n", lines), sourceMetadata(source)));
        }

        return documents;
    }

    private List<Document> loadDocx(Resource resource, String source) throws IOException {
        try (InputStream inputStream = resource.getInputStream();
             XWPFDocument document = new XWPFDocument(inputStream)) {
            List<String> paragraphs = document.getParagraphs().stream()
                    .map(XWPFParagraph::getText)
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .toList();

            if (paragraphs.isEmpty()) {
                return List.of();
            }

            return List.of(new Document(String.join("\n\n", paragraphs), sourceMetadata(source)));
        }
    }

    private String readText(Resource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private List<String> readLines(Resource resource) throws IOException {
        String text = readText(resource)
                .replace("\uFEFF", "")
                .replace("\r\n", "\n")
                .replace('\r', '\n');
        return text.lines()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        values.add(current.toString().trim());
        return values;
    }

    private String csvRowToText(List<String> headers, List<String> values) {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            if (!StringUtils.hasText(value)) {
                continue;
            }

            String header = i < headers.size() ? headers.get(i) : "column_" + (i + 1);
            if (StringUtils.hasText(header)) {
                parts.add(header + ": " + value);
            } else {
                parts.add(value);
            }
        }
        return String.join("; ", parts);
    }

    private boolean isSupportedResource(Resource resource) {
        if (!resource.isReadable()) {
            return false;
        }

        String filename = resource.getFilename();
        if (!StringUtils.hasText(filename)) {
            return false;
        }

        String normalized = filename.toLowerCase(Locale.ROOT);
        return SUPPORTED_EXTENSIONS.stream().anyMatch(normalized::endsWith);
    }

    private Document withSourceMetadata(Document document, String source) {
        Map<String, Object> metadata = new HashMap<>(document.getMetadata());
        metadata.put("source", source);
        metadata.put(KNOWLEDGE_BASE_METADATA_KEY, knowledgeBaseId);
        return document.mutate()
                .metadata(metadata)
                .build();
    }

    private Map<String, Object> sourceMetadata(String source) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", source);
        metadata.put(KNOWLEDGE_BASE_METADATA_KEY, knowledgeBaseId);
        return metadata;
    }

    private String resolveSource(Resource resource) {
        String filename = resource.getFilename();
        return StringUtils.hasText(filename) ? filename : resource.getDescription();
    }

    public record LoadedDocuments(int resourceCount, List<Document> documents, List<String> sources) {
    }

    public record RagIndexResult(int resourceCount, int documentCount, int chunkCount, List<String> sources) {
    }
}
