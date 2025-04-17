package com.example.bookstore.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "Message",
        catalog = "")
@Data
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    protected Integer id;

    @Basic
    @Column(name = "message", nullable = false, length = 255, columnDefinition = "nvarchar(255)")
    private String message;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @EqualsAndHashCode.Exclude
    private User user;

    @CreatedDate
    @Column(name = "created_date_time" , updatable = false)
    protected LocalDateTime createdAt;

    @Basic
    @Column(name = "outgoing")
    private Boolean outgoingFromUser;
}
