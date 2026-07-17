package com.example.springboot.controller;

import lombok.Data;

@Data
public class ChatPayload {
    private String agent;   // 智能体标识
    private String prompt;  // 输入内容
}
