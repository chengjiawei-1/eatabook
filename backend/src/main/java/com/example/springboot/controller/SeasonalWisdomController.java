package com.example.springboot.controller;

import com.example.springboot.entity.SeasonalWisdom;
import com.example.springboot.service.SeasonalWisdomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="SeasonalWisdomController",description="节气有关信息")
@RestController
@RequestMapping("/api/seasonal-wisdom")
@CrossOrigin
public class SeasonalWisdomController {
    @Autowired
    private SeasonalWisdomService seasonalWisdomService;

    @Operation(summary = "节气查询", description = "查询特定节气信息")
    @GetMapping("/{tagName}")
    public List<SeasonalWisdom> selectAllById(@PathVariable String tagName) {
        return seasonalWisdomService.selectAllById(tagName);
    }
}
