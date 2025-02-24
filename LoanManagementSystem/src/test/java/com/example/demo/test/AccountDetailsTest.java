package com.example.demo.test;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.model.AccountDetails;

import java.util.Date;
 
class AccountDetailsTest {
    private AccountDetails accountDetails;

    @BeforeEach
    void setUp() {
        accountDetails = new AccountDetails();
    }

    @Test
    void testDefaultConstructor() {
        AccountDetails localAccountDetails = new AccountDetails(); // Renamed to avoid shadowing
        assertNotNull(localAccountDetails);
    }

    @Test
    void testAllArgsConstructor() {
        Date date = new Date();
        AccountDetails localAccountDetails = new AccountDetails(1, "Savings", "John", "Doe", "john.doe@example.com", "1234567890", "123 Main St", date, "Male", "Jane Doe", "Active", 1000.0, date, null); // Renamed to avoid shadowing

        assertEquals(1, localAccountDetails.getAccountNumber());
        assertEquals("Savings", localAccountDetails.getAccountType());
        assertEquals("John", localAccountDetails.getFirstName());
        assertEquals("Doe", localAccountDetails.getLastName());
        assertEquals("john.doe@example.com", localAccountDetails.getEmail());
        assertEquals("1234567890", localAccountDetails.getMobileNo());
        assertEquals("123 Main St", localAccountDetails.getAddress());
        assertEquals(date, localAccountDetails.getDateOfBirth());
        assertEquals("Male", localAccountDetails.getGender());
        assertEquals("Jane Doe", localAccountDetails.getNomineeName());
        assertEquals("Active", localAccountDetails.getAccountStatus());
        assertEquals(1000.0, localAccountDetails.getBalance(), 0.01);
        assertEquals(date, localAccountDetails.getAccountCreationDate());
        assertNull(localAccountDetails.getAccountClosedDate());
    }


    
    @Test
    void testSettersAndGetters() {
        Date date = new Date();
        accountDetails.setAccountNumber(1);
        accountDetails.setAccountType("Current");
        accountDetails.setFirstName("Alice");
        accountDetails.setLastName("Smith");
        accountDetails.setEmail("alice.smith@example.com");
        accountDetails.setMobileNo("0987654321");
        accountDetails.setAddress("456 Elm St");
        accountDetails.setDateOfBirth(date);
        accountDetails.setGender("Female");
        accountDetails.setNomineeName("Bob Smith");
        accountDetails.setAccountStatus("Inactive");
        accountDetails.setBalance(2000.5);
        accountDetails.setAccountCreationDate(date);
        accountDetails.setAccountClosedDate(null);
 
        assertEquals(1, accountDetails.getAccountNumber());
        assertEquals("Current", accountDetails.getAccountType());
        assertEquals("Alice", accountDetails.getFirstName());
        assertEquals("Smith", accountDetails.getLastName());
        assertEquals("alice.smith@example.com", accountDetails.getEmail());
        assertEquals("0987654321", accountDetails.getMobileNo());
        assertEquals("456 Elm St", accountDetails.getAddress());
        assertEquals(date, accountDetails.getDateOfBirth());
        assertEquals("Female", accountDetails.getGender());
        assertEquals("Bob Smith", accountDetails.getNomineeName());
        assertEquals("Inactive", accountDetails.getAccountStatus());
        assertEquals(2000.5, accountDetails.getBalance(), 0.01);
        assertEquals(date, accountDetails.getAccountCreationDate());
        assertNull(accountDetails.getAccountClosedDate());
    }
 
    @Test
    void testNegativeBalance() {
        accountDetails.setBalance(-500);
        assertEquals(-500, accountDetails.getBalance(), 0.01);
    }
 
    @Test void testBoundaryAccountNumber() {
        accountDetails.setAccountNumber(0);
        assertEquals(0, accountDetails.getAccountNumber());
        accountDetails.setAccountNumber(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, accountDetails.getAccountNumber());
accountDetails.setAccountNumber(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, accountDetails.getAccountNumber());
    }
 
    @Test
    void testEmptyStrings() {
        accountDetails.setFirstName("");
        assertEquals("", accountDetails.getFirstName());
accountDetails.setLastName("");
        assertEquals("", accountDetails.getLastName());
    }
}


