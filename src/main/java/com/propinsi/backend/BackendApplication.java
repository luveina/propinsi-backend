package com.propinsi.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	// make sure there is at least one admin (superadmin) in the system
	@Bean
	public org.springframework.boot.CommandLineRunner dataInitializer(
	        com.propinsi.backend.repository.UserRepository userRepo,
	        org.springframework.security.crypto.password.PasswordEncoder encoder) {
	    return args -> {
	        String username = "antonelli12";
	        if (!userRepo.existsByUsername(username)) {
	            com.propinsi.backend.model.User admin = com.propinsi.backend.model.User.builder()
	                    .username(username)
	                    .fullName("Kimi Antonelli")
	                    .phoneNumber("081234567890")
	                    .password(encoder.encode("Silobur123!"))
	                    .role(com.propinsi.backend.model.Role.ADMIN)
	                    .status("Active")
	                    .isFirstLogin(true)
	                    .createdBy("SYSTEM")
	                    .updatedBy("SYSTEM")
	                    .build();
	            userRepo.save(admin);
	            System.out.println("[Init] Superadmin '" + username + "' created");
	        }
	    };
	}

}
