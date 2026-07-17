package com.example.springboot.interceptor;

import com.example.springboot.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //放行OPTIONS请求
        if("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        //获取请求路径
        String requestURI = request.getRequestURI();
        System.out.println(">>> 正在拦截请求: " + requestURI);

        //获取 token
        String token = request.getHeader("Authorization");
        System.out.println(">>> 获取到的 Token: " + token);

        //判断token是否为空
        if (!StringUtils.hasLength(token)) {
            System.out.println(">>> 错误: Token 为空");
            response.setStatus(401);
            return false;
        }

        //解析 token
        try {
            Integer userId = JwtUtils.parseToken(token);

            if (userId == null) {
                System.out.println(">>> 错误: Token 解析失败，用户ID为空");
                response.setStatus(401);
                return false;
            }

            System.out.println(">>> Token 解析成功，UserId: " + userId);
            request.setAttribute("userId", userId);
            return true;

        } catch (Exception e) {
            System.out.println(">>> 错误: Token 解析失败 - " + e.getMessage());
            response.setStatus(401);
            return false;
        }
    }
}