package com.example.demo.test;

import org.junit.jupiter.api.Test;
import com.example.demo.model.BankTransaction;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

	 class BankTransactionTest {
		 

	    // Test 1: Test No-Args Constructor and Setters/Getters
	    @Test
	    void testNoArgsConstructorAndGettersSetters() {
	        // Create an instance using the no-args constructor
	        BankTransaction bankTransaction = new BankTransaction();

	        // Set values using setters
	        bankTransaction.setTransactionId(1);
	        bankTransaction.setAccountNumber(1001);
	        bankTransaction.setToAccNo(2002);
	        bankTransaction.setTransactionType("Deposit");
	        bankTransaction.setTransactionAmount(500.00);
	        bankTransaction.setTransactionDate(new Date());
	        bankTransaction.setBalanceAfterTransaction(1500.00);
	        bankTransaction.setTransactionStatus("Successful");

	        // Validate the values using getters
	        assertEquals(1, bankTransaction.getTransactionId());
	        assertEquals(1001, bankTransaction.getAccountNumber());
	        assertEquals(2002, bankTransaction.getToAccNo());
	        assertEquals("Deposit", bankTransaction.getTransactionType());
	        assertEquals(500.00, bankTransaction.getTransactionAmount());
	        assertNotNull(bankTransaction.getTransactionDate());
	        assertEquals(1500.00, bankTransaction.getBalanceAfterTransaction());
	        assertEquals("Successful", bankTransaction.getTransactionStatus());
	    }

	    // Test 2: Test All-Args Constructor
	    @Test
	    void testAllArgsConstructor() {
	        // Create an instance using all-args constructor
	        Date date = new Date();
	        BankTransaction bankTransaction = new BankTransaction(1, 1001, 2002, "Deposit", 500.00, date, 1500.00, "Successful");

	        // Validate the values using getters
	        assertEquals(1, bankTransaction.getTransactionId());
	        assertEquals(1001, bankTransaction.getAccountNumber());
	        assertEquals(2002, bankTransaction.getToAccNo());
	        assertEquals("Deposit", bankTransaction.getTransactionType());
	        assertEquals(500.00, bankTransaction.getTransactionAmount());
	        assertEquals(date, bankTransaction.getTransactionDate());
	        assertEquals(1500.00, bankTransaction.getBalanceAfterTransaction());
	        assertEquals("Successful", bankTransaction.getTransactionStatus());
	    }

	    // Test 3: Test Setters and Getters Individually
	    @Test
	    void testSettersAndGetters() {
	        BankTransaction bankTransaction = new BankTransaction();

	        // Set values individually
	        bankTransaction.setTransactionId(1);
	        bankTransaction.setAccountNumber(1001);
	        bankTransaction.setToAccNo(2002);
	        bankTransaction.setTransactionType("Deposit");
	        bankTransaction.setTransactionAmount(500.00);
	        bankTransaction.setTransactionDate(new Date());
	        bankTransaction.setBalanceAfterTransaction(1500.00);
	        bankTransaction.setTransactionStatus("Successful");

	        // Validate values using assertions
	        assertAll(
	            () -> assertEquals(1, bankTransaction.getTransactionId()),
	            () -> assertEquals(1001, bankTransaction.getAccountNumber()),
	            () -> assertEquals(2002, bankTransaction.getToAccNo()),
	            () -> assertEquals("Deposit", bankTransaction.getTransactionType()),
	            () -> assertEquals(500.00, bankTransaction.getTransactionAmount()),
	            () -> assertNotNull(bankTransaction.getTransactionDate()),
	            () -> assertEquals(1500.00, bankTransaction.getBalanceAfterTransaction()),
	            () -> assertEquals("Successful", bankTransaction.getTransactionStatus())
	        );
	    }

	    // Test 4: Test No-Args Constructor Defaults
	    @Test
	    void testNoArgsConstructorDefaults() {
	        BankTransaction bankTransaction = new BankTransaction();

	        // Validate default values for all fields
	        assertEquals(0, bankTransaction.getTransactionId());
	        assertEquals(0, bankTransaction.getAccountNumber());
	        assertEquals(0, bankTransaction.getToAccNo());
	        assertNull(bankTransaction.getTransactionType());
	        assertEquals(0.0, bankTransaction.getTransactionAmount());
	        assertNull(bankTransaction.getTransactionDate());
	        assertEquals(0.0, bankTransaction.getBalanceAfterTransaction());
	        assertNull(bankTransaction.getTransactionStatus());
	    }

	    // Test 5: Test for non-default values (Edge case)
	    @Test
	    void testCustomTransactionValues() {
	        Date date = new Date();
	        BankTransaction bankTransaction = new BankTransaction(123, 12345, 67890, "Withdrawal", 1000.00, date, 5000.00, "Successful");

	        // Validate the values using getters
	        assertEquals(123, bankTransaction.getTransactionId());
	        assertEquals(12345, bankTransaction.getAccountNumber());
	        assertEquals(67890, bankTransaction.getToAccNo());
	        assertEquals("Withdrawal", bankTransaction.getTransactionType());
	        assertEquals(1000.00, bankTransaction.getTransactionAmount());
	        assertEquals(date, bankTransaction.getTransactionDate());
	        assertEquals(5000.00, bankTransaction.getBalanceAfterTransaction());
	        assertEquals("Successful", bankTransaction.getTransactionStatus());
	    }
	}
