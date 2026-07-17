package com.example.springboot.service.impl;

import com.example.springboot.entity.SeasonalWisdom;
import com.example.springboot.mapper.SeasonalWisdomMapper;
import com.example.springboot.service.SeasonalWisdomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeasonalWisdomServicelmpl implements SeasonalWisdomService {
    @Autowired
    private SeasonalWisdomMapper seasonalWisdomMapper;

    @Override
    public List<SeasonalWisdom> selectAllById(String tagName) {
        return seasonalWisdomMapper.selectAllById(tagName);
    }
}
