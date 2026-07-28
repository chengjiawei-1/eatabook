package com.example.springboot.config;

import com.example.springboot.interceptor.LoginInterceptor;
import com.example.springboot.utils.FileUploadUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Bean
    public LoginInterceptor loginInterceptor() {
        return new LoginInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/users/login",
                        "/api/users/register",
                        "/api/users/avatars",
                        "/api/speech/config/status",
                        "/uploads/**"
                );
    }

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String uploadLocation = FileUploadUtils.uploadRoot(uploadDir).toUri().toString();
        if (!uploadLocation.endsWith("/")) {
            uploadLocation += "/";
        }
        System.out.println("文件上传目录: " + uploadLocation);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation);
    }
}
