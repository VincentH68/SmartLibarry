package com.example.bookstore.dao;

import com.example.bookstore.entity.Chat;
import com.example.bookstore.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageDao extends JpaRepository<Message, Integer>{
    List<Message> findAllByUserId(Integer id);

}
