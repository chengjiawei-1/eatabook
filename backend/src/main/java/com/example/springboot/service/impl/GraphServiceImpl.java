package com.example.springboot.service.impl;

import com.example.springboot.entity.NodeEntity;
import com.example.springboot.service.GraphService;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GraphServiceImpl implements GraphService {

    private final Driver driver;

    public GraphServiceImpl(Driver driver) {
        this.driver = driver;
    }

    @Override
    public List<NodeEntity> getAllNodes() {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {

                List<NodeEntity> nodes = new ArrayList<>();

                Result result = tx.run(
                        "MATCH (n) " +
                                "RETURN id(n) AS id, labels(n) AS labels, n AS properties "
                );

                while (result.hasNext()) {

                    Record record = result.next();

                    NodeEntity node = new NodeEntity();

                    node.setId(record.get("id").asLong());

                    List<String> labels =
                            record.get("labels").asList(Object::toString);

                    node.setLabel(
                            labels.isEmpty() ? "Unknown" : labels.get(0)
                    );

                    node.setProperties(
                            record.get("properties").asMap()
                    );

                    nodes.add(node);
                }

                return nodes;
            });
        }
    }

    @Override
    public List<Map<String, Object>> getAllEdges() {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {

                List<Map<String, Object>> edges =
                        new ArrayList<>();

                Result result = tx.run(
                        "MATCH (a)-[r]->(b) " +
                                "RETURN id(a) AS source, " +
                                "id(b) AS target, " +
                                "type(r) AS type "
                );

                while (result.hasNext()) {

                    Record record = result.next();

                    Map<String, Object> edge =
                            new HashMap<>();

                    edge.put(
                            "source",
                            record.get("source").asLong()
                    );

                    edge.put(
                            "target",
                            record.get("target").asLong()
                    );

                    edge.put(
                            "type",
                            record.get("type").asString()
                    );

                    edges.add(edge);
                }

                return edges;
            });
        }
    }
}