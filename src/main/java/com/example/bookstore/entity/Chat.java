package com.example.bookstore.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "Chats")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Basic
    @Column(length = 255, name = "name", nullable = true, columnDefinition = "nvarchar(255)")
    private String name;
    @Basic
    @Column(name = "intent_name", nullable = true, length = 255, columnDefinition = "nvarchar(255)")
    private String intentName;
    @Basic
    @Column(name = "training_phrases", nullable = true, length = 255, columnDefinition = "nvarchar(255)")
    private String trainingPhrases;
    @Basic
    @Column(name = "responses", nullable = true)
    private String responses;
    private String description;
}
