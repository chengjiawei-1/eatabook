package com.example.springboot.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TcmConstitution {
    private Integer constitutionId;
    private String constitutionName;
    private String intro;
    private String characteristics;
    private String healthRisk;
    private LocalDateTime updateTime;
}