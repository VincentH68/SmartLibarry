package com.example.bookstore.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Định nghĩa MessageSource để lấy các chuỗi từ các file properties
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("messages");  // Tên file .properties mà bạn đã tạo
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    // Cấu hình Interceptor để thay đổi ngôn ngữ khi thay đổi tham số URL
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang"); // Tham số để thay đổi ngôn ngữ (ví dụ: ?lang=vi)
        registry.addInterceptor(localeChangeInterceptor);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
