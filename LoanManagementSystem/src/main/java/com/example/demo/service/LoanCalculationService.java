package com.example.demo.service;
 
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
 
@Service
public class LoanCalculationService {
 
    
    private static  Logger logger = Logger.getLogger(LoanCalculationService.class);
 
    /**
     * Method to calculate loan payment details including monthly payment, total payment, and total interest.
     *
     * @param loanAmount The amount of the loan.
     * @param annualInterestRate The annual interest rate in percentage.
     * @param loanTerm The term of the loan in years.
     * @param loanType The type of loan (used for potential future extensions, though not used in this logic).
     * @return A LoanCalculationResult object containing the calculated monthly payment, total payment, and total interest.
     */
    public LoanCalculationResult calculateLoan(double loanAmount, double annualInterestRate, int loanTerm, String loanType) {
        logger.info("Calculating loan with amount: {}, annual interest rate: + loan term:  years + loan type: " +
                loanAmount + annualInterestRate + loanTerm + loanType);
 
        
        double calculatedInterest = annualInterestRate / 100 / 12;
        
        
        int payments = loanTerm * 12;
        
        
        double x = Math.pow(1 + calculatedInterest, payments);
        double monthlyPayment = (loanAmount * x * calculatedInterest) / (x - 1);
        
        
        if (!Double.isFinite(monthlyPayment)) {
            logger.error("The calculated monthly payment is not a finite number. Loan calculation failed.");
            return null;  
        }
 
        
        double totalPayment = monthlyPayment * payments;
        double totalInterest = totalPayment - loanAmount;
        
        logger.info("Loan calculation completed. Monthly Payment: + Total Payment: + Total Interest: " +
                monthlyPayment + totalPayment + totalInterest);
 
        //
        return new LoanCalculationResult(monthlyPayment, totalPayment, totalInterest);
    }
 
    /**
     * Result object to hold the loan calculation results: monthly payment, total payment, and total interest.
     */
    public static class LoanCalculationResult {
        private double monthlyPayment;
        private double totalPayment;
        private double totalInterest;
 
        
        public LoanCalculationResult(double monthlyPayment, double totalPayment, double totalInterest) {
            this.monthlyPayment = monthlyPayment;
            this.totalPayment = totalPayment;
            this.totalInterest = totalInterest;
        }
 
       
        public double getMonthlyPayment() {
            return monthlyPayment;
        }
 
        
        public double getTotalPayment() {
            return totalPayment;
        }
 
        
        public double getTotalInterest() {
            return totalInterest;
        }
    }
}
 