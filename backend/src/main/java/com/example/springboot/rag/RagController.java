package com.example.springboot.rag;

import org.springframework.ai.document.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagIndexService ragIndexService;
    private final RagSearchService ragSearchService;

    public RagController(RagIndexService ragIndexService, RagSearchService ragSearchService) {
        this.ragIndexService = ragIndexService;
        this.ragSearchService = ragSearchService;
    }

    @PostMapping("/index")
    public RagIndexService.RagIndexResult index() {
        return ragIndexService.index();
    }

    @PostMapping("/clear")
    public Map<String, Object> clear() {
        ragIndexService.clearKnowledgeBase();
        return Map.of("message", "RAG knowledge base cleared");
    }

    @PostMapping("/search")
    public List<Document> search(@RequestBody RagSearchRequest request) {
        return ragSearchService.search(request.query(), request.topK(), request.similarityThreshold());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage()));
    }

    public record RagSearchRequest(String query, Integer topK, Double similarityThreshold) {
    }
}
