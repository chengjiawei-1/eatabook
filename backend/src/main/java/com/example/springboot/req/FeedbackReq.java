package com.example.springboot.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackReq {
    private String name;
    private String contact;
    private String email;
    private String content;
}
