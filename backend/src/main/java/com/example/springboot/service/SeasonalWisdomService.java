package com.example.springboot.service;

import com.example.springboot.entity.SeasonalWisdom;

import java.util.List;

public interface SeasonalWisdomService {
    List<SeasonalWisdom> selectAllById(String tagName);
}
