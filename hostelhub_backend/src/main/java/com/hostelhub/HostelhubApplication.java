package com.hostelhub;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class HostelhubApplication {

	public static void main(String[] args) {
		SpringApplication.run(HostelhubApplication.class, args);
	}
	@Bean
    CommandLineRunner generatePasswordHash(PasswordEncoder passwordEncoder) {
		return args -> System.out.println(
				"BCrypt hash: " + passwordEncoder.encode("Sandu@9864")
		);
	}
}
