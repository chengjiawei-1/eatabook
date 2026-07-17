package com.example.springboot.websocket;

import com.example.springboot.utils.JwtUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WebSocketInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        // 获取 URL 中的参数
        String query = request.getURI().getQuery();
        System.out.println(">>> WebSocket 握手请求参数: " + query);

        if (query != null && query.contains("token=")) {
            // 提取 token
            String token = query.split("token=")[1].split("&")[0];

            try {
                // 验证 token
                Integer userId = JwtUtils.parseToken(token);

                if (userId != null) {
                    System.out.println(">>> WebSocket 握手成功，用户ID: " + userId);
                    attributes.put("userId", userId);
                    return true;
                }
            } catch (Exception e) {
                System.out.println(">>> WebSocket 握手失败: Token 无效或过期 - " + e.getMessage());
                return false;
            }
        }

        System.out.println(">>> WebSocket 握手失败: 未找到 Token 参数");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}