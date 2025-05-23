package com.example.bookstore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Entity
public class StatisticalProductDay {
	@Id
	private String code;
	private String name;
	private int price;
	private int quantity;
	private long selled;
}
