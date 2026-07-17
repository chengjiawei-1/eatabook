package com.example.springboot.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

@Component
public class JwtUtils {

    // 过期时间常量
    public static final long EXPIRE_TIME_SECONDS = 720000;

    private static final String SECRET = "your-secret-key";

    // 生成 token
    public static String generateToken(Integer userId) {
        Date expiryDate = new Date(System.currentTimeMillis() + (EXPIRE_TIME_SECONDS * 1000));

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    // 解析 token
    public static Integer parseToken(String token) {
        try {
            if (!StringUtils.hasLength(token)) {
                throw new RuntimeException("Token为空");
            }
            token = token.trim();

            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();

            Object userIdObj = claims.get("userId");
            if (userIdObj != null) {
                return Integer.valueOf(userIdObj.toString());
            } else {
                return Integer.valueOf(claims.getSubject());
            }

        } catch (MalformedJwtException e) {
            System.err.println("JWT格式错误: " + e.getMessage());
            throw new RuntimeException("无效的令牌");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("令牌解析失败: " + e.getMessage());
        }
    }

    // 验证 token
    public static Long validateToken(String token) throws Exception {
        try {
            if (StringUtils.hasLength(token)) {
                token = token.trim();
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
            }

            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();

            Object userIdObj = claims.get("userId");
            if (userIdObj != null) {
                return Long.parseLong(userIdObj.toString());
            } else {
                return Long.parseLong(claims.getSubject());
            }
        } catch (Exception e) {
            throw new Exception("Token 无效或已过期", e);
        }
    }
}