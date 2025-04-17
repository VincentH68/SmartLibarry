package com.example.bookstore.dao;

import com.example.bookstore.entity.Category;
import com.example.bookstore.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatDao extends JpaRepository<Chat, Integer>{

}
