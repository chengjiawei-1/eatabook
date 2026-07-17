package com.example.springboot.entity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_behavior_log")
public class UserBehaviorLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String behaviorType;
    private Long dishId;
    private String sessionId;
    private String refererSessionId;
    private String ipAddress;
    private String userAgent;
    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
    public enum BehaviorType {
        CLICK_VIEW("点击查看详情"),
        FAVORITE_DISH("收藏菜品");

        private final String description;

        BehaviorType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
