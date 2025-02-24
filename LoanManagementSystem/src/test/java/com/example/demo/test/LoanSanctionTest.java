package com.example.demo.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.model.LoanSanction;

 class LoanSanctionTest {

    private LoanSanction loanSanction;

    @BeforeEach
     void setUp() {
        loanSanction = new LoanSanction();
    }

    @Test
     void testLoanSanctionDefaultConstructor() {
        assertNotNull(loanSanction);
        assertEquals(0, loanSanction.getSanctionId());
        assertEquals(0, loanSanction.getLoanId());
        assertNull(loanSanction.getSanctionDate());
        assertEquals(0.0, loanSanction.getSanctionAmount(), 0.001);
        assertNull(loanSanction.getSanctionedBy());
        assertNull(loanSanction.getSanctionStatus());
        assertNull(loanSanction.getLoanStartDate());
        assertNull(loanSanction.getLoanEndDate());
        assertEquals(0.0, loanSanction.getInterestRate(), 0.001);
        assertEquals(0.0, loanSanction.getMonthlyInstallmentsAmount(), 0.001);
    }

    @Test
     void testLoanSanctionParameterizedConstructor() {
        Date now = new Date();
        LoanSanction loanSanctionObject = new LoanSanction(1, 2, now, 10000, "Alexa", "Approved", now, now, 5.5, 850);

        assertEquals(1, loanSanctionObject.getSanctionId());
        assertEquals(2, loanSanctionObject.getLoanId());
        assertEquals(now, loanSanctionObject.getSanctionDate());
        assertEquals(10000, loanSanctionObject.getSanctionAmount(), 0.001);
        assertEquals("Alexa", loanSanctionObject.getSanctionedBy());
        assertEquals("Approved", loanSanctionObject.getSanctionStatus());
        assertEquals(now, loanSanctionObject.getLoanStartDate());
        assertEquals(now, loanSanctionObject.getLoanEndDate());
        assertEquals(5.5, loanSanctionObject.getInterestRate(), 0.001);
        assertEquals(850, loanSanctionObject.getMonthlyInstallmentsAmount(), 0.001);
    }


    @Test
     void testSetAndGetSanctionId() {
        loanSanction.setSanctionId(10);
        assertEquals(10, loanSanction.getSanctionId());
    }

    @Test
     void testSetAndGetLoanId() {
        loanSanction.setLoanId(20);
        assertEquals(20, loanSanction.getLoanId());
    }

    @Test
     void testSetAndGetSanctionDate() {
        Date date = new Date();
        loanSanction.setSanctionDate(date);
        assertEquals(date, loanSanction.getSanctionDate());
    }

    @Test
     void testSetAndGetSanctionAmount() {
        loanSanction.setSanctionAmount(9999.99);
        assertEquals(9999.99, loanSanction.getSanctionAmount(), 0.001);
    }

    @Test
     void testSetAndGetSanctionedBy() {
        loanSanction.setSanctionedBy("Jordan");
        assertEquals("Jordan", loanSanction.getSanctionedBy());
    }

    @Test
     void testSetAndGetSanctionStatus() {
        loanSanction.setSanctionStatus("Pending");
        assertEquals("Pending", loanSanction.getSanctionStatus());
    }

    @Test
     void testSetAndGetLoanStartDate() {
        Date date = new Date();
        loanSanction.setLoanStartDate(date);
        assertEquals(date, loanSanction.getLoanStartDate());
    }

    @Test
     void testSetAndGetLoanEndDate() {
        Date date = new Date();
        loanSanction.setLoanEndDate(date);
        assertEquals(date, loanSanction.getLoanEndDate());
    }

    @Test
     void testSetAndGetInterestRate() {
        loanSanction.setInterestRate(3.75);
        assertEquals(3.75, loanSanction.getInterestRate(), 0.001);
    }

    @Test
     void testSetAndGetMonthlyInstallmentsAmount() {
        loanSanction.setMonthlyInstallmentsAmount(1250);
        assertEquals(1250, loanSanction.getMonthlyInstallmentsAmount(), 0.001);
    }
}
