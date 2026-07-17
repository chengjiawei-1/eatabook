package com.example.springboot.mq;

import com.example.springboot.config.RabbitMQConfig;
import com.example.springboot.entity.Notification;
import com.example.springboot.entity.User;
import com.example.springboot.service.NotificationService;
import com.example.springboot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class ReminderConsumer {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserService userService;

    //监听死信队列
    @RabbitListener(queues = RabbitMQConfig.DEAD_LETTER_QUEUE)
    public void handleReminder(Integer userId) {
        log.info("接收到体质测试提醒消息，用户ID: {}", userId);

        try {
            //二次校验
            User user = userService.selectById(userId);
            if (user == null || user.getNextTestTime() == null) {
                log.info("用户或提醒时间已删除，忽略消息");
                return;
            }

            //检查数据库里的时间是否真的到了
            if (user.getNextTestTime().isAfter(LocalDateTime.now().plusMinutes(1))) {
                log.info("用户修改了时间，当前消息作废");
                return;
            }

            //发送站内信通知
            Notification notification = new Notification();
            notification.setUserId(Long.valueOf(userId));
            notification.setType("reminder");
            notification.setTitle("体质复测提醒");
            notification.setContent("您预约的体质测试时间已到，请及时进行测试。");
            notification.setLink("/physique-test");
            notification.setReadStatus(false);
            notification.setCreatedAt(LocalDateTime.now());

            notificationService.sendNotification(notification);

            //清空数据库的提醒时间
        } catch (Exception e) {
            log.error("处理提醒消息异常", e);
        }
    }
}