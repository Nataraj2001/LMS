package com.example.demo.test;


 
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

import com.example.demo.model.SignUp;

import static org.junit.jupiter.api.Assertions.*;
 
class SignUpTest {
 
    private SignUp signUp;
 
    @BeforeEach

    void setUp() {

        signUp = new SignUp();

    }
 
    @Test void testGetId() {

        signUp.setId(1);

        assertEquals(1, signUp.getId());

    }
 
    @Test void testSetId() {

        signUp.setId(10);

        assertEquals(10, signUp.getId());

    }
 
    @Test void testGetUsername() {

        signUp.setUsername("testUser");

        assertEquals("testUser", signUp.getUsername());

    }
 
    @Test void testSetUsername() {

        signUp.setUsername("newUser");

        assertEquals("newUser", signUp.getUsername());

    }
 
    @Test void testGetPassword() {

        signUp.setPassword("password123");

        assertEquals("password123", signUp.getPassword());

    }
 
    @Test void testSetPassword() {

        signUp.setPassword("newPass");

        assertEquals("newPass", signUp.getPassword());

        
    }
 
    @Test void testGetEmail() {

        signUp.setEmail("test@example.com");

        assertEquals("test@example.com", signUp.getEmail());

    }
 
    @Test void testSetEmail() {

        signUp.setEmail("new@example.com");

        assertEquals("new@example.com", signUp.getEmail());

    }
 
    @Test void testGetMobileNo() {

        signUp.setMobileNo("1234567890");

        assertEquals("1234567890", signUp.getMobileNo());

    }
 
    @Test

    void testSetMobileNo() {

        signUp.setMobileNo("0987654321");

        assertEquals("0987654321", signUp.getMobileNo());

    }
 
    @Test void testGetRole() {

        signUp.setRole("ADMIN");

        assertEquals("ADMIN", signUp.getRole());

    }
 
    @Test

    void testSetRole() {

        signUp.setRole("USER");

        assertEquals("USER", signUp.getRole());

    }
 
    @Test void testAllArgsConstructor() {

        signUp = new SignUp(1, "username", "password", "email@example.com", "1234567890", "USER");

        assertEquals(1, signUp.getId());

        assertEquals("username", signUp.getUsername());

        assertEquals("password", signUp.getPassword());

        assertEquals("email@example.com", signUp.getEmail());

        assertEquals("1234567890", signUp.getMobileNo());

        assertEquals("USER", signUp.getRole());

    }
 
    @Test

    void testNoArgsConstructor() {

        SignUp emptySignUp = new SignUp();

        assertNotNull(emptySignUp);

        assertEquals(0, emptySignUp.getId());

        assertNull(emptySignUp.getUsername());

        assertNull(emptySignUp.getPassword());

        assertNull(emptySignUp.getEmail());

        assertNull(emptySignUp.getMobileNo());

        assertNull(emptySignUp.getRole());

    }

}

 


