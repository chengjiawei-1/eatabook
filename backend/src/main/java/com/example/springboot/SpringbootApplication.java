package com.example.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(excludeName = "org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration")
@EnableTransactionManagement
public class SpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);
    }

    public static class AncientFoodHealthApplication {
        public static void main(String[] args) {
            SpringApplication.run(AncientFoodHealthApplication.class, args);
        }
    }
}

