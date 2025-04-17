/**
 * @(#)SecurityConfig.java 2021/09/09.
 * 
 * Copyright(C) 2021 by PHOENIX FIVE.
 * 
 * Last_Update 2021/09/09.
 * Version 1.00.
 */
package com.example.bookstore.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.example.bookstore.service.UserService;
import com.example.bookstore.service.impl.UserDetailsServiceImpl;

import javax.sql.DataSource;

/**
 * Class dung de phan quyen cho project
 * 
 * @author khoa-ph
 * @version 1.00
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	// Thong tin User Service
	@Autowired
	UserService userService;

	// Phuong thuc ma hoa mat khau
	@Autowired
	BCryptPasswordEncoder pe;

	// Phuong thuc cap quyen
	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	// Thong tin datasource
	@Autowired
	DataSource dataSource;

	/**
	 * Cung cap quyen cho project
	 * 
	 * @param auth
	 * @throws Exception
	 */
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService);
	}


	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();

		// Cac trang yeu cau quyen su dung la Admin hoac Director
		http.authorizeRequests().antMatchers("/admin/**", "/chat/**").access("hasAnyRole('ROLE_ADMIN', 'ROLE_DIRECTOR')");

		http.authorizeRequests().antMatchers("/shop/profile/**","/shop/favorite/**" ,"/shop/cart/checkout", "/account", "/account/**", "/rest/favorite/add/**", "/chat/**")
		.access("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_DIRECTOR')");
		
		http.authorizeRequests().anyRequest().permitAll();

		http.authorizeRequests().and().exceptionHandling().accessDeniedPage("/403page");

		http.authorizeRequests().and().formLogin().loginPage("/login").usernameParameter("username")
				.passwordParameter("password").failureForwardUrl("/login").defaultSuccessUrl("/login/success", false);

		// Cau hinh dang xuat khoi chuong trinh
		http.authorizeRequests().and().logout().logoutUrl("/logout").logoutSuccessUrl("/index");

		// Cau hinh remember me
		http.authorizeRequests().and().rememberMe().tokenValiditySeconds(86400);
	}

	/**
	 * Cung cap phuong thuc ma hoa
	 * 
	 * @return phuong thuc ma hoa
	 */
	@Bean
	public BCryptPasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		// TODO Auto-generated method stub
		web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**");
	}
}
