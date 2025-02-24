package com.example.demo.service;

import org.apache.log4j.Logger;
import java.util.List;
import org.springframework.stereotype.Service;
import com.example.demo.model.LoanApplication;
import com.example.demo.model.LoanSanction;
import com.example.demo.repo.LoanSanctionRepo;
import com.example.demo.repo.LoanApplicationRepo;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class LoanSanctionService {
    
    private static Logger logger = Logger.getLogger(LoanSanctionService.class);

    private final LoanSanctionRepo loanSanctionRepo;
    private final LoanApplicationRepo loanApplicationRepo;

    // Constructor Injection
    public LoanSanctionService(LoanSanctionRepo loanSanctionRepo, LoanApplicationRepo loanApplicationRepo) {
        this.loanSanctionRepo = loanSanctionRepo;
        this.loanApplicationRepo = loanApplicationRepo;
    }

    /**
     * Fetches all loan sanctions and calculates their monthly installment amount.
     * @return List of all LoanSanction objects with calculated monthly installments.
     */
    public List<LoanSanction> show() {
        logger.info("Fetching all loan sanctions and calculating monthly installments.");
        List<LoanSanction> sanctions = loanSanctionRepo.findAll();
        sanctions.forEach(this::calculateAndSetMonthlyInstallment);
        return sanctions;
    }

    /**
     * Searches for a loan sanction by its ID and calculates the monthly installment.
     * @param sanctionId The ID of the loan sanction.
     * @return LoanSanction object with calculated monthly installments.
     */
    public LoanSanction searchById(int sanctionId) {
        logger.info("Searching for loan sanction with ID " + sanctionId);
        LoanSanction loanSanction = loanSanctionRepo.findById(sanctionId).orElse(null);
        if (loanSanction != null) {
            calculateAndSetMonthlyInstallment(loanSanction);
            logger.info("Found and calculated monthly installment for loan sanction ID " + sanctionId);
        } else {
            logger.warn("Loan sanction with ID:  not found " + sanctionId);
        }
        return loanSanction;
    }

    /**
     * Searches for a loan sanction by the loan ID and calculates the monthly installment.
     * @param loanId The ID of the loan associated with the sanction.
     * @return LoanSanction object with calculated monthly installments.
     */
    public LoanSanction searchByLoanId(int loanId) {
        logger.info("Searching for loan sanction with Loan ID " + loanId);
        LoanSanction loanSanction = loanSanctionRepo.findByLoanId(loanId);
        if (loanSanction != null) {
            calculateAndSetMonthlyInstallment(loanSanction);
            logger.info("Found and calculated monthly installment for loan sanction with Loan ID " + loanId);
        } else {
            logger.warn("Loan sanction with Loan ID: {} not found " + loanId);
        }
        return loanSanction;
    }

    /**
     * Searches for loan sanctions by their sanction status and calculates the monthly installment for each.
     * @param sanctionStatus The sanction status (e.g., "APPROVED", "REJECTED").
     * @return List of loan sanctions with calculated monthly installments.
     */
    public List<LoanSanction> searchBySanctionStatus(String sanctionStatus) {
        logger.info("Searching for loan sanctions with Sanction Status " + sanctionStatus);
        List<LoanSanction> sanctions = loanSanctionRepo.findBySanctionStatus(sanctionStatus);
        sanctions.forEach(this::calculateAndSetMonthlyInstallment);
        return sanctions;
    }

    /**
     * Calculates and sets the monthly installment for a given loan sanction.
     * The monthly installment is calculated based on the loan amount, interest rate, and loan term.
     * @param loanSanction The LoanSanction object to calculate the monthly installment for.
     */
     public void calculateAndSetMonthlyInstallment(LoanSanction loanSanction) {
        logger.debug("Calculating monthly installment for LoanSanction ID " + loanSanction.getSanctionId());
        LoanApplication loanApplication = loanApplicationRepo.findById(loanSanction.getLoanId()).orElse(null);
        if (loanApplication == null) {
            logger.warn("Loan application with Loan ID  not found, setting monthly installment to 0.0" +  loanSanction.getLoanId());
            loanSanction.setMonthlyInstallmentsAmount(0.0);
            return;
        }

        double loanAmount = loanApplication.getLoanAmount();
        double annualInterestRate = loanSanction.getInterestRate();
        int loanTerm = loanApplication.getTenure();

        
        if (annualInterestRate == 0 || loanAmount == 0 || loanTerm == 0) {
            logger.warn("Invalid loan data: Loan Amount: + Interest Rate: + Loan Term: + setting monthly installment to 0.0 " + loanAmount + annualInterestRate + loanTerm);
            loanSanction.setMonthlyInstallmentsAmount(0.0);
            return;
        }

        double monthlyInterestRate = annualInterestRate / 100 / 12;
        int totalPayments = loanTerm * 12;
        double factor = Math.pow(1 + monthlyInterestRate, totalPayments);
        double monthlyPayment = (loanAmount * monthlyInterestRate * factor) / (factor - 1);

        logger.info("Calculated monthly installment for LoanSanction ID " +  loanSanction.getSanctionId() + monthlyPayment);

        loanSanction.setMonthlyInstallmentsAmount(monthlyPayment);
    }
}
