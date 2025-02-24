package com.example.demo.test;

import org.junit.jupiter.api.Test;
import com.example.demo.model.AuthRequest;
import static org.junit.jupiter.api.Assertions.*;

	 class AuthRequestTest {

	    // Test 1: Test the no-args constructor and setters/getters
	    @Test
	    void testNoArgsConstructorAndGettersSetters() {
	        // Create an instance using no-args constructor
	        AuthRequest authRequest = new AuthRequest();

	        // Set values using setters
	        authRequest.setUsername("testUser");
	        authRequest.setPassword("password123");
	        authRequest.setRole("admin");

	        
	        // Validate the values using getters
	        assertEquals("testUser", authRequest.getUsername());
	        assertEquals("password123", authRequest.getPassword());
	        assertEquals("admin", authRequest.getRole());
	    }

	    // Test 2: Test the all-args constructor
	    @Test
	    void testAllArgsConstructor() {
	        // Create an instance using all-args constructor
	        AuthRequest authRequest = new AuthRequest("testUser", "password123", "admin");

	        // Validate the values using getters
	        assertEquals("testUser", authRequest.getUsername());
	        assertEquals("password123", authRequest.getPassword());
	        assertEquals("admin", authRequest.getRole());
	    }

	    // Test 3: Test the setters and getters for individual fields
	    @Test
	    void testSettersAndGetters() {
	        AuthRequest authRequest = new AuthRequest();

	        // Set values individually
	        authRequest.setUsername("testUser");
	        authRequest.setPassword("password123");
	        authRequest.setRole("admin");

	        // Validate values
	        assertAll(
	            () -> assertEquals("testUser", authRequest.getUsername()),
	            () -> assertEquals("password123", authRequest.getPassword()),
	            () -> assertEquals("admin", authRequest.getRole())
	        );
	    }

	    // Test 4: Test the no-args constructor to ensure default values are null
	    @Test
	    void testNoArgsConstructorDefaults() {
	        AuthRequest authRequest = new AuthRequest();

	        assertNull(authRequest.getUsername());
	        assertNull(authRequest.getPassword());
	        assertNull(authRequest.getRole());
	    }
	    
	}
