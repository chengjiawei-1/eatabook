package com.example.springboot.config;

import org.springframework.amqp.core.*;
        import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    //定义交换机和队列名称
    public static final String DELAY_EXCHANGE_NAME = "reminder.delay.exchange";
    public static final String DELAY_QUEUE_NAME = "reminder.delay.queue";
    public static final String DEAD_LETTER_EXCHANGE = "reminder.process.exchange";
    public static final String DEAD_LETTER_QUEUE = "reminder.process.queue";
    public static final String ROUTING_KEY = "reminder.key";

    //定义死信交换机
    @Bean
    public DirectExchange processExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    //定义死信队列
    @Bean
    public Queue processQueue() {
        return new Queue(DEAD_LETTER_QUEUE);
    }

    //绑定死信队列到死信交换机
    @Bean
    public Binding processBinding() {
        return BindingBuilder.bind(processQueue()).to(processExchange()).with(ROUTING_KEY);
    }

    //定义延迟交换机
    @Bean
    public CustomExchange delayExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(DELAY_EXCHANGE_NAME, "direct", true, false);
    }

    //定义延迟队列
    @Bean
    public Queue delayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", ROUTING_KEY);
        return new Queue(DELAY_QUEUE_NAME, true, false, false, args);
    }

    //绑定延迟队列
    @Bean
    public Binding delayBinding() {
        return BindingBuilder.bind(delayQueue())
                .to(delayExchange())
                .with(ROUTING_KEY)
                .noargs();
    }
}