package com.example.demo.test;

	import com.example.demo.model.SignUp;
import com.example.demo.service.SignUpDataDetails;

import org.junit.jupiter.api.BeforeEach;
	import org.junit.jupiter.api.Test;
	import static org.junit.jupiter.api.Assertions.*;
	import java.util.List;
	import org.springframework.security.core.GrantedAuthority;
	
	 class SignUpDataDetailsServiceTest {

	    private SignUp signUp;
	    private SignUpDataDetails signUpDataDetails;

	    @BeforeEach
	    void setUp() {
	        // Create a sample SignUp object with roles
	        signUp = new SignUp();
	        signUp.setUsername("testUser");
	        signUp.setPassword("password123");
	        signUp.setRole("USER,ADMIN"); // Multiple roles to test authority mapping

	        // Initialize SignUpDataDetails with the SignUp object
	        signUpDataDetails = new SignUpDataDetails(signUp);
	    }

	    @Test
	    void testConstructor() {
	        assertNotNull(signUpDataDetails, "SignUpDataDetails should be correctly initialized.");
	        assertEquals("testUser", signUpDataDetails.getUsername(), "Username should match.");
	        assertEquals("password123", signUpDataDetails.getPassword(), "Password should match.");
	    }

	    @Test
	    void testGetAuthorities() {
	        List<GrantedAuthority> authorities = (List<GrantedAuthority>) signUpDataDetails.getAuthorities();

	        assertNotNull(authorities, "Authorities should not be null.");
	        assertEquals(2, authorities.size(), "There should be two authorities.");
	        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("USER")), "Authorities should contain 'USER'.");
	        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ADMIN")), "Authorities should contain 'ADMIN'.");
	    }

	    @Test
	    void testGetUsername() {
	        assertEquals("testUser", signUpDataDetails.getUsername(), "Username should be 'testUser'.");
	    }

	    @Test
	    void testGetPassword() {
	        assertEquals("password123", signUpDataDetails.getPassword(), "Password should be 'password123'.");
	    }

	    @Test
	    void testIsAccountNonExpired() {
	        assertTrue(signUpDataDetails.isAccountNonExpired(), "Account should not be expired.");
	    }

	    @Test
	    void testIsAccountNonLocked() {
	        assertTrue(signUpDataDetails.isAccountNonLocked(), "Account should not be locked.");
	    }

	    @Test
	    void testIsCredentialsNonExpired() {
	        assertTrue(signUpDataDetails.isCredentialsNonExpired(), "Credentials should not be expired.");
	    }

	    @Test
	    void testIsEnabled() {
	        assertTrue(signUpDataDetails.isEnabled(), "Account should be enabled.");
	    }

	    @Test
	    void testMultipleRoles() {
	        SignUp signUpWithMultipleRoles = new SignUp();
	        signUpWithMultipleRoles.setUsername("multiRoleUser");
	        signUpWithMultipleRoles.setPassword("password123");
	        signUpWithMultipleRoles.setRole("USER,ADMIN,MODERATOR");

	        SignUpDataDetails signUpDataDetailsMultipleRoles = new SignUpDataDetails(signUpWithMultipleRoles);
	        List<GrantedAuthority> authorities = (List<GrantedAuthority>) signUpDataDetailsMultipleRoles.getAuthorities();

	        assertNotNull(authorities, "Authorities should not be null.");
	        assertEquals(3, authorities.size(), "There should be three authorities.");
	        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("USER")), "Authorities should contain 'USER'.");
	        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ADMIN")), "Authorities should contain 'ADMIN'.");
	        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("MODERATOR")), "Authorities should contain 'MODERATOR'.");
	    }
	}


