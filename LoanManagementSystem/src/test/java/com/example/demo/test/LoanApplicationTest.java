package com.example.demo.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.model.LoanApplication;

class LoanApplicationTest {

	private LoanApplication loanApplication;

	@BeforeEach
	void setUp() {
		loanApplication = new LoanApplication();
	}

	@Test
	void testDefaultConstructor() {
		LoanApplication defaultLoanApplication = new LoanApplication();
		assertEquals(0, defaultLoanApplication.getLoanId());
		assertEquals(0, defaultLoanApplication.getAccountNumber());
		assertEquals(0.0, defaultLoanApplication.getLoanAmount());
		assertEquals(null, defaultLoanApplication.getLoanType());
		assertEquals(null, defaultLoanApplication.getEmployType());
		assertEquals(0.0, defaultLoanApplication.getAnnualIncome());
		assertEquals("PENDING", defaultLoanApplication.getStatus());
		assertEquals(0.0, defaultLoanApplication.getInterestRate());
		assertEquals(0, defaultLoanApplication.getTenure());
		assertEquals(0, defaultLoanApplication.getCreditScore());
	}

	@Test
	void testParameterizedConstructor() {
		LoanApplication paramLoanApplication = new LoanApplication(1, 123456, 10000.0, "Home", "Salaried", 75000.0,
				"APPROVED", 5.0, 20, 700);
		assertEquals(1, paramLoanApplication.getLoanId());
		assertEquals(123456, paramLoanApplication.getAccountNumber());
		assertEquals(10000.0, paramLoanApplication.getLoanAmount());
		assertEquals("Home", paramLoanApplication.getLoanType());
		assertEquals("Salaried", paramLoanApplication.getEmployType());
		assertEquals(75000.0, paramLoanApplication.getAnnualIncome());
		assertEquals("APPROVED", paramLoanApplication.getStatus());
		assertEquals(5.0, paramLoanApplication.getInterestRate());
		assertEquals(20, paramLoanApplication.getTenure());
		assertEquals(700, paramLoanApplication.getCreditScore());
	}

	@Test
	void testMutators() {
		loanApplication.setLoanId(2);
		loanApplication.setAccountNumber(654321);
		loanApplication.setLoanAmount(20000.0);
		loanApplication.setLoanType("Car");
		loanApplication.setEmployType("Self-Employed");
		loanApplication.setAnnualIncome(100000.0);
		loanApplication.setStatus("REJECTED");
		loanApplication.setInterestRate(6.5);
		loanApplication.setTenure(15);
		loanApplication.setCreditScore(650);

		assertEquals(2, loanApplication.getLoanId());
		assertEquals(654321, loanApplication.getAccountNumber());
		assertEquals(20000.0, loanApplication.getLoanAmount());
		assertEquals("Car", loanApplication.getLoanType());
		assertEquals("Self-Employed", loanApplication.getEmployType());
		assertEquals(100000.0, loanApplication.getAnnualIncome());
		assertEquals("REJECTED", loanApplication.getStatus());
		assertEquals(6.5, loanApplication.getInterestRate());
		assertEquals(15, loanApplication.getTenure());
		assertEquals(650, loanApplication.getCreditScore());
	}

	@Test
	void testStatusDefaultValue() {
		assertEquals("PENDING", loanApplication.getStatus());
	}

	@Test
	void testUniqueLoanId() {
		loanApplication.setLoanId(1);
		LoanApplication anotherLoanApplication = new LoanApplication();
		anotherLoanApplication.setLoanId(2);
		assertNotEquals(loanApplication.getLoanId(), anotherLoanApplication.getLoanId());
	}

	@Test
	void testNegativeLoanAmount() {
		loanApplication.setLoanAmount(-1000.0);
		assertEquals(-1000.0, loanApplication.getLoanAmount());
	}

	@Test
	void testZeroAndNegativeValues() {
		loanApplication.setTenure(0);
		assertEquals(0, loanApplication.getTenure());
		loanApplication.setTenure(-12);
		assertEquals(-12, loanApplication.getTenure());
		loanApplication.setCreditScore(0);
		assertEquals(0, loanApplication.getCreditScore());
	}

	@Test
	void testStringValues() {
		loanApplication.setLoanType("STUDENT");
		loanApplication.setEmployType("UNEMPLOYED");

		assertEquals("STUDENT", loanApplication.getLoanType());
		assertEquals("UNEMPLOYED", loanApplication.getEmployType());
	}
}
