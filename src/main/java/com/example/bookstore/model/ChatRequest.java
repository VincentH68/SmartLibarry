package com.example.bookstore.model;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private Integer userId;
}

