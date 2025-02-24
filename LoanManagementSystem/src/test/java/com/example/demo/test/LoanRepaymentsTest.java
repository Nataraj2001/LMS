package com.example.demo.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.model.LoanRepayments;
 
class LoanRepaymentsTest {
 
    private LoanRepayments loanRepayment;
 
    @BeforeEach void setUp() {
        loanRepayment = new LoanRepayments();
    }
 
    @Test
    void testDefaultConstructor() {
        LoanRepayments lr = new LoanRepayments();
        assertEquals(0, lr.getPaymentId());
        assertEquals(0, lr.getLoanId());
        assertNull(lr.getPaymentDate());
        assertEquals(0.0, lr.getPaymentAmount(), 0.01);
        assertNull(lr.getPaymentMode());
        assertNull(lr.getPaymentStatus());
        assertEquals(0.0, lr.getDueLoanAmount(), 0.01);
    }
 
    @Test
    void testAllArgsConstructor() {
        Date today = new Date();
        LoanRepayments lr = new LoanRepayments(1, 100, today, 500.0, "Credit Card", "Paid", 1500.0);
 
        assertEquals(1, lr.getPaymentId());
        assertEquals(100, lr.getLoanId());
        assertEquals(today, lr.getPaymentDate());
        assertEquals(500.0, lr.getPaymentAmount(), 0.01);
        assertEquals("Credit Card", lr.getPaymentMode());
        assertEquals("Paid", lr.getPaymentStatus());
        assertEquals(1500.0, lr.getDueLoanAmount(), 0.01);
    }
 
    @Test
    void testSetAndGetPaymentId() {
        loanRepayment.setPaymentId(10);
        assertEquals(10, loanRepayment.getPaymentId());
    }
 
    @Test void testSetAndGetLoanId() {
        loanRepayment.setLoanId(200);
        assertEquals(200, loanRepayment.getLoanId());
    }
 
    @Test
    void testSetAndGetPaymentDate() {
        Date paymentDate = new Date();
        loanRepayment.setPaymentDate(paymentDate);
        assertEquals(paymentDate, loanRepayment.getPaymentDate());
    }
 
    @Test void testSetAndGetPaymentAmount() {
        loanRepayment.setPaymentAmount(1000.75);
        assertEquals(1000.75, loanRepayment.getPaymentAmount(), 0.01);
    }
 
    @Test void testSetAndGetPaymentMode() {
        loanRepayment.setPaymentMode("Bank Transfer");
        assertEquals("Bank Transfer", loanRepayment.getPaymentMode());
    }
 
    @Test
    void testSetAndGetPaymentStatus() {
        loanRepayment.setPaymentStatus("Pending");
        assertEquals("Pending", loanRepayment.getPaymentStatus());
    }
 
    @Test void testSetAndGetDueLoanAmount() {
        loanRepayment.setDueLoanAmount(2500.00);
        assertEquals(2500.00, loanRepayment.getDueLoanAmount(), 0.01);
    }
 
    @Test
    void testNegativePaymentAmount() {
        loanRepayment.setPaymentAmount(-500.0);
        assertEquals(-500.0, loanRepayment.getPaymentAmount(), 0.01);
    }
 
    @Test
    void testNegativeDueLoanAmount() {
        loanRepayment.setDueLoanAmount(-1500.0);
        assertEquals(-1500.0, loanRepayment.getDueLoanAmount(), 0.01);
    }
 
    @Test void testNullPaymentMode() {
        loanRepayment.setPaymentMode(null);
        assertNull(loanRepayment.getPaymentMode());
    }
 
    @Test void testNullPaymentStatus() {
        loanRepayment.setPaymentStatus(null);
        assertNull(loanRepayment.getPaymentStatus());
    }
}