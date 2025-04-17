package com.example.bookstore.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.bookstore.common.Constants;

@Controller
public class AdminThongKeController {
	
	@GetMapping("/admin/statistical/product/day")
	public String product(Model model) {
		return Constants.USER_DISPLAY_ADMIN_STATISTICAL_PRODUCT_DAY;
	}
	
	@GetMapping("/admin/statistical/product/warehouse")
	public String warehouse(Model model) {
		return Constants.USER_DISPLAY_ADMIN_STATISTICAL_WAREHOUSE_PRODUCT;
	}
	
	@GetMapping("/admin/statistical/revenue")
	public String revenue(Model model) {
		return Constants.USER_DISPLAY_ADMIN_STATISTICAL_REVENUE;
	}
	
	@GetMapping("/admin/statistical/order")
	public String order(Model model) {
		return Constants.USER_DISPLAY_ADMIN_STATISTICAL_ORDER;
	}
	
}
