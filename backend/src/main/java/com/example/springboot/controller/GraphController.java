package com.example.springboot.controller;

import com.example.springboot.entity.NodeEntity;
import com.example.springboot.service.GraphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/graph")
@CrossOrigin
@Tag(name = "GraphController", description = "知识图谱相关接口")
public class GraphController {

    private final GraphService graphService;
    private final String neo4jUri;
    private final JdbcTemplate jdbcTemplate;

    public GraphController(GraphService graphService, @Value("${neo4j.uri}") String neo4jUri, JdbcTemplate jdbcTemplate) {
        this.graphService = graphService;
        this.neo4jUri = neo4jUri;
        this.jdbcTemplate = jdbcTemplate;
    }

    //统一返回图数据
    @Operation(summary = "获取知识图谱")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getGraph() {
        try {
            List<NodeEntity> nodes = graphService.getAllNodes();
            List<Map<String, Object>> links = graphService.getAllEdges();

            if (nodes.isEmpty()) {
                return ResponseEntity.ok(buildMysqlGraph());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("nodes", nodes);
            result.put("links", links);

            return ResponseEntity.ok(result);
        } catch (ServiceUnavailableException e) {
            Map<String, Object> result = buildMysqlGraph();
            result.put("message", "Neo4j is not available, returned graph data generated from MySQL.");
            result.put("neo4jUri", neo4jUri);
            return ResponseEntity.ok(result);
        }
    }

    private Map<String, Object> buildMysqlGraph() {
        Map<String, NodeEntity> nodesByKey = new LinkedHashMap<>();
        List<Map<String, Object>> links = new ArrayList<>();

        jdbcTemplate.queryForList("""
                SELECT book_id, book_name, dynasty, author, book_category, intro
                FROM ancient_books
                """).forEach(row -> addNode(nodesByKey, "Book", bookId(row.get("book_id")), row));

        jdbcTemplate.queryForList("""
                SELECT dish_id, dish_name, dish_source, suitable_constitution, unsuitable_constitution,
                       ancient_citation, effect_tag1, effect_tag2, effect_tag3, effect_tag4, effect_tag5, kind
                FROM dishes
                """).forEach(row -> addNode(nodesByKey, "Dish", dishId(row.get("dish_id")), row));

        jdbcTemplate.queryForList("""
                SELECT ingredient_id, ingredient_name, alias, property, category, ancient_effect
                FROM food_ingredients
                """).forEach(row -> addNode(nodesByKey, "Ingredient", ingredientId(row.get("ingredient_id")), row));

        jdbcTemplate.queryForList("""
                SELECT constitution_id, constitution_name, intro, characteristics, health_risk
                FROM tcm_constitution
                """).forEach(row -> addNode(nodesByKey, "Constitution", constitutionId(row.get("constitution_id")), row));

        jdbcTemplate.queryForList("""
                SELECT dish_id, book_id
                FROM dish_book_rel
                """).forEach(row -> addLink(links, dishId(row.get("dish_id")), bookId(row.get("book_id")), "RECORDED_IN"));

        jdbcTemplate.queryForList("""
                SELECT dish_id, ingredient_id, usage_detail
                FROM dish_ingredient_rel
                """).forEach(row -> addLink(links, dishId(row.get("dish_id")), ingredientId(row.get("ingredient_id")), "USES", row));

        jdbcTemplate.queryForList("""
                SELECT ingredient_id, book_id
                FROM ingredient_book_rel
                """).forEach(row -> addLink(links, ingredientId(row.get("ingredient_id")), bookId(row.get("book_id")), "MENTIONED_IN"));

        addConstitutionAndEffectLinks(nodesByKey, links);

        return Map.of(
                "nodes", new ArrayList<>(nodesByKey.values()),
                "links", links
        );
    }

    private void addConstitutionAndEffectLinks(Map<String, NodeEntity> nodesByKey, List<Map<String, Object>> links) {
        Map<String, Long> constitutionIdsByName = nodesByKey.values().stream()
                .filter(node -> "Constitution".equals(node.getLabel()))
                .filter(node -> node.getProperties().get("constitution_name") != null)
                .collect(Collectors.toMap(
                        node -> normalizeName(node.getProperties().get("constitution_name")),
                        NodeEntity::getId,
                        (left, right) -> left
                ));

        Map<String, Long> effectIdsByName = new LinkedHashMap<>();

        jdbcTemplate.queryForList("""
                SELECT dish_id, suitable_constitution, unsuitable_constitution,
                       effect_tag1, effect_tag2, effect_tag3, effect_tag4, effect_tag5
                FROM dishes
                """).forEach(row -> {
            Long source = dishId(row.get("dish_id"));

            splitTerms(row.get("suitable_constitution")).forEach(name -> {
                Long target = constitutionIdsByName.get(normalizeName(name));
                if (target != null) {
                    addLink(links, source, target, "SUITABLE_FOR");
                }
            });

            splitTerms(row.get("unsuitable_constitution")).forEach(name -> {
                Long target = constitutionIdsByName.get(normalizeName(name));
                if (target != null) {
                    addLink(links, source, target, "UNSUITABLE_FOR");
                }
            });

            for (String key : List.of("effect_tag1", "effect_tag2", "effect_tag3", "effect_tag4", "effect_tag5")) {
                String effectName = Objects.toString(row.get(key), "").trim();
                if (effectName.isEmpty()) {
                    continue;
                }
                Long effectId = effectIdsByName.computeIfAbsent(effectName, value -> 400000L + effectIdsByName.size() + 1);
                nodesByKey.computeIfAbsent("Effect:" + effectId, ignored -> createNode("Effect", effectId, Map.of("effect_name", effectName)));
                addLink(links, source, effectId, "HAS_EFFECT");
            }
        });
    }

    private void addNode(Map<String, NodeEntity> nodesByKey, String label, Long id, Map<String, Object> properties) {
        nodesByKey.put(label + ":" + id, createNode(label, id, cleanProperties(properties)));
    }

    private NodeEntity createNode(String label, Long id, Map<String, Object> properties) {
        NodeEntity node = new NodeEntity();
        node.setId(id);
        node.setLabel(label);
        node.setProperties(properties);
        return node;
    }

    private Map<String, Object> cleanProperties(Map<String, Object> row) {
        return row.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> left, LinkedHashMap::new));
    }

    private void addLink(List<Map<String, Object>> links, Long source, Long target, String type) {
        addLink(links, source, target, type, Map.of());
    }

    private void addLink(List<Map<String, Object>> links, Long source, Long target, String type, Map<String, Object> properties) {
        if (source == null || target == null) {
            return;
        }
        Map<String, Object> link = new LinkedHashMap<>();
        link.put("source", source);
        link.put("target", target);
        link.put("type", type);
        link.putAll(cleanProperties(properties));
        links.add(link);
    }

    private List<String> splitTerms(Object value) {
        if (value == null) {
            return List.of();
        }
        return Pattern.compile("[,，、/；;\\s]+")
                .splitAsStream(value.toString())
                .map(String::trim)
                .filter(term -> !term.isEmpty())
                .toList();
    }

    private String normalizeName(Object value) {
        return Objects.toString(value, "").replaceAll("[\\s\"'`]", "");
    }

    private Long bookId(Object id) {
        return asLong(id);
    }

    private Long dishId(Object id) {
        Long value = asLong(id);
        return value == null ? null : 100000L + value;
    }

    private Long ingredientId(Object id) {
        Long value = asLong(id);
        return value == null ? null : 200000L + value;
    }

    private Long constitutionId(Object id) {
        Long value = asLong(id);
        return value == null ? null : 300000L + value;
    }

    private Long asLong(Object id) {
        return id == null ? null : ((Number) id).longValue();
    }
}
