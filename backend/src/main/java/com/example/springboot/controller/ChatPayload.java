package com.example.springboot.controller;

import lombok.Data;

import java.util.Map;

@Data
public class ChatPayload {
    private String agent;
    private String scene;
    private String prompt;
    private Map<String, Object> meta;
}
