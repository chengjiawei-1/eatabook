package com.example.springboot.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class RagSearchService {

    private final VectorStore vectorStore;

    @Value("${app.rag.search.default-top-k:5}")
    private int defaultTopK;

    @Value("${app.rag.search.default-similarity-threshold:0.0}")
    private double defaultSimilarityThreshold;

    public RagSearchService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Document> search(String query, Integer topK, Double similarityThreshold) {
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("query is required");
        }

        SearchRequest request = SearchRequest.builder()
                .query(query.trim())
                .topK(resolveTopK(topK))
                .similarityThreshold(resolveSimilarityThreshold(similarityThreshold))
                .build();

        return vectorStore.similaritySearch(request);
    }

    private int resolveTopK(Integer topK) {
        int resolved = topK == null ? defaultTopK : topK;
        return Math.max(1, Math.min(resolved, 20));
    }

    private double resolveSimilarityThreshold(Double similarityThreshold) {
        double resolved = similarityThreshold == null ? defaultSimilarityThreshold : similarityThreshold;
        return Math.max(0.0, Math.min(resolved, 1.0));
    }
}
