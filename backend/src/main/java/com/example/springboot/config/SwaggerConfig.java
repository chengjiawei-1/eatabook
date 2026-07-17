package com.example.springboot.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI springOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("接口文档")
                        .description("通用接口")
                        .version("v1")
                        .license(new License().name("Apache 2.0").url("http://paperbase.cn")))
                .components(components())
                .addSecurityItem(new SecurityRequirement().addList("tokenScheme"))
                .externalDocs(new ExternalDocumentation()
                        .description("外部文档")
                        .url("http://paperbase.cn"));
    }

    private Components components() {
        return new Components()
                .addSecuritySchemes("tokenScheme", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization"));
    }
}
