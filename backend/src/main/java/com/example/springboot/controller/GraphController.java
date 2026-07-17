package com.example.springboot.controller;

import com.example.springboot.entity.NodeEntity;
import com.example.springboot.service.GraphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/graph")
@CrossOrigin
@Tag(name = "GraphController", description = "知识图谱相关接口")
public class GraphController {

    private final GraphService graphService;
    private final String neo4jUri;

    public GraphController(GraphService graphService, @Value("${neo4j.uri}") String neo4jUri) {
        this.graphService = graphService;
        this.neo4jUri = neo4jUri;
    }

    //统一返回图数据
    @Operation(summary = "获取知识图谱")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getGraph() {
        try {
            List<NodeEntity> nodes = graphService.getAllNodes();
            List<Map<String, Object>> links = graphService.getAllEdges();

            Map<String, Object> result = new HashMap<>();
            result.put("nodes", nodes);
            result.put("links", links);

            return ResponseEntity.ok(result);
        } catch (ServiceUnavailableException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Neo4j is not available. Please start Neo4j and check neo4j.uri.");
            result.put("neo4jUri", neo4jUri);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
        }
    }
}
