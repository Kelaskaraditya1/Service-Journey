package com.starkIndustries.serviceJourney;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class BackendApplication {

	@GetMapping("/health")
	public ResponseEntity<?> health() {

		Map<String,Object> response = new LinkedHashMap<>();

		response.put("health", "healthy");
		response.put("status", "up");
		response.put("message","Service journey running on port: 8080");	

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
