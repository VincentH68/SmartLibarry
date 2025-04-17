package com.example.bookstore.model;

import lombok.Data;

@Data
public class MessageDto {
    private Long id;
    private Integer userId;
    private String message;
    private String createdDate;
    private String email;
    private String fullName;
    private Boolean outgoingFromUser;
}
