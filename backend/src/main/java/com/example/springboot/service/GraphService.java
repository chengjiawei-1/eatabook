package com.example.springboot.service;

import com.example.springboot.entity.NodeEntity;

import java.util.List;
import java.util.Map;

public interface GraphService {
    List<NodeEntity> getAllNodes();
    List<Map<String, Object>> getAllEdges();
}
