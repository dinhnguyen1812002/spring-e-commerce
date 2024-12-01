package com.app.e_commerce;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;

import org.springframework.util.unit.DataSize;

@SpringBootApplication

public class ECommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceApplication.class, args);
	}
	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setMaxFileSize(DataSize.ofMegabytes(10)); // Giới hạn kích thước file tối đa
		factory.setMaxRequestSize(DataSize.ofMegabytes(20)); // Giới hạn kích thước request tối đa
		return factory.createMultipartConfig();
	}
}
