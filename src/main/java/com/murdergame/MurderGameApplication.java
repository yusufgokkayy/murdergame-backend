package com.murdergame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class MurderGameApplication {

	public static void main(String[] args) {
		SpringApplication.run(MurderGameApplication.class, args);
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String rawPassword = "123456";
		String encodedPassword = encoder.encode(rawPassword);

		System.out.println("--- SENİN PC'NDE ÜRETİLEN HASH ---");
		System.out.println(encodedPassword);
		System.out.println("---------------------------------");
	}

}