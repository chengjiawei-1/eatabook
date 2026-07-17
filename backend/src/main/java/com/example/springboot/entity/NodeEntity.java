package com.example.springboot.entity;

import lombok.Data;
import java.util.Map;

@Data
public class NodeEntity {
    private Long id;
    private String label;
    private Map<String, Object> properties;
}
