package com.example.demo.service;
 
import org.apache.log4j.Logger;
 

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo.exception.EmailSendingException;
import com.example.demo.exception.LoanApplicationNotFoundException;
import com.example.demo.exception.LoanRejectionException;
import com.example.demo.model.AccountDetails;
import com.example.demo.model.Constants;
import com.example.demo.model.LoanApplication;
import com.example.demo.model.LoanRepayments;
import com.example.demo.model.LoanSanction;
import com.example.demo.repo.AccountDetailsRepo;
import com.example.demo.repo.LoanApplicationRepo;
import com.example.demo.repo.LoanRepaymentsRepo;
import com.example.demo.repo.LoanSanctionRepo;
import jakarta.transaction.Transactional;
 
@Service
@Transactional
public class LoanApplicationService {
 
    private static Logger logger = Logger.getLogger(LoanApplicationService.class);
 
    private final LoanApplicationRepo loanApplicationRepo;
    private final LoanSanctionRepo loanSanctionRepo;
    private final BankTransactionService bankTransactionService;
    private final LoanRepaymentsRepo loanRepaymentsRepo;
    private final AccountDetailsRepo accountDetailsRepo;
    private final EmailService emailService;
 
 
    // Constructor injection
    public LoanApplicationService(LoanApplicationRepo loanApplicationRepo,
                                   LoanSanctionRepo loanSanctionRepo,
                                   AccountDetailsRepo accountDetailsRepo,
                                   LoanRepaymentsRepo loanRepaymentsRepo,
                                   EmailService emailService,
                                   BankTransactionService bankTransactionService) {
        this.loanApplicationRepo = loanApplicationRepo;
        this.loanSanctionRepo = loanSanctionRepo;
        this.loanRepaymentsRepo = loanRepaymentsRepo;
        this.accountDetailsRepo = accountDetailsRepo;
        this.bankTransactionService = bankTransactionService;
        this.emailService = emailService;
        
    }
 
    /**
     * Retrieves all loan applications.
     *
     * @return List of all loan applications.
     */
    public List<LoanApplication> showLoanapplication() {
        logger.info("Fetching all loan applications");
        return loanApplicationRepo.findAll();
    }
 
    /**
     * Searches for a loan application by its ID.
     *
     * @param loanId ID of the loan application.
     * @return The loan application.
     */
    public LoanApplication searchLoanapplicationById(int loanId) {
        logger.info("Searching for loan application with ID: " + loanId);
        return loanApplicationRepo.findById(loanId).orElseThrow(
                () -> new LoanApplicationNotFoundException("Loan application with ID " + loanId + " not found"));
    }
 
    /**
     * Adds a new loan application after validating the inputs and restrictions.
     *
     * @param loanApplication The loan application to be added.
     * @return Message about the result of the loan application submission.
     */
    public ResponseEntity<String> addLoanApplication(LoanApplication loanApplication) {
        logger.info("Attempting to add loan application for account number: " + loanApplication.getAccountNumber());

        // Check if the loan application violates any loan type restrictions
        String loanTypeCheck = checkLoanTypeRestrictions(loanApplication);
        if (loanTypeCheck != null) {
            return new ResponseEntity<>(loanTypeCheck, HttpStatus.BAD_REQUEST); // Return 400 with the message
        }

        // Validate employment type for the loan type
        String employmentCheck = checkEmploymentTypeForLoan(loanApplication);
        if (employmentCheck != null) {
            return new ResponseEntity<>(employmentCheck, HttpStatus.BAD_REQUEST); // Return 400 with the message
        }

        // Check for duplicate loan applications
        String duplicateCheck = checkForDuplicateLoanApplication(loanApplication);
        if (duplicateCheck != null) {
            return new ResponseEntity<>(duplicateCheck, HttpStatus.BAD_REQUEST); // Return 400 with the message
        }

        // Save the loan application
        loanApplicationRepo.save(loanApplication);
        logger.info("Loan application added successfully for account number: " + loanApplication.getAccountNumber());
        AccountDetails accountDetails = accountDetailsRepo.findByAccountNumber(loanApplication.getAccountNumber());
        try {
			emailService.sendLoanApplicationEmail(accountDetails.getEmail(),
			        accountDetails.getFirstName(),
			        loanApplication.getLoanType(),
			        loanApplication.getLoanAmount());
		} catch (EmailSendingException e) {

			e.printStackTrace();
		}

        return new ResponseEntity<>("Loan application submitted successfully.", HttpStatus.OK); // Return 200 OK
    }

    // Method to check loan type restrictions
    public String checkLoanTypeRestrictions(LoanApplication loanApplication) {
        List<LoanApplication> existingLoans = loanApplicationRepo.findByAccountNumber(loanApplication.getAccountNumber());

        boolean hasPersonalLoan = false;
        boolean hasBusinessLoan = false;
        boolean hasEducationLoan = false;

        // Loop through existing loans and set flags accordingly
        for (LoanApplication existingLoan : existingLoans) {
            String existingLoanType = existingLoan.getLoanType();

            // Set flags based on the loan type of existing loans
            if (Constants.PERSONAL.equals(existingLoanType)) {
                hasPersonalLoan = true;
            } else if (Constants.BUSINESS.equals(existingLoanType)) {
                hasBusinessLoan = true;
            } else if (Constants.EDUCATION.equals(existingLoanType)) {
                hasEducationLoan = true;
            }
        }
        String newLoanType = loanApplication.getLoanType();

        // Education Loan restrictions: Can’t apply for another loan
        if (hasEducationLoan) {
            logger.warn("Loan type restriction triggered: Existing Education Loan prevents new loan applications");
            return "You cannot apply for any other loan if you already have an Education Loan.";
        }

        // Personal Loan and other loan type restrictions
        if (hasPersonalLoan && (Constants.EDUCATION.equals(newLoanType) || Constants.BUSINESS.equals(newLoanType))) {
            logger.warn("Loan type restriction triggered: Existing Personal Loan prevents new loan types");
            return "You cannot apply for an Education or Business Loan if you already have a Personal Loan.";
        }

        // Business Loan and Personal Loan restrictions
        if (hasBusinessLoan && Constants.PERSONAL.equals(newLoanType)) {
            logger.warn("Loan type restriction triggered: Existing Business Loan prevents new Personal Loan");
            return "You cannot apply for a Personal Loan if you already have a Business Loan.";
        }

        // New validation: Cannot apply for Education Loan if both Personal and Business Loans exist
        if (hasBusinessLoan && hasPersonalLoan && Constants.EDUCATION.equals(newLoanType)) { 
            logger.warn("Loan type restriction triggered: Cannot apply for Education Loan with both Personal and Business Loans approved");
            return "You cannot apply for an Education Loan if you already have both a Personal and Business Loan approved.";
        }

        return null;  // No restrictions, loan application can proceed
    }


 
    // Method to check employment type for the loan type
    public String checkEmploymentTypeForLoan(LoanApplication loanApplication) {
        String loanType = loanApplication.getLoanType();
        String employmentType = loanApplication.getEmployType();
 
        if (loanType.equals("PERSONAL") && !employmentType.equals("SALARIED")) {
            logger.warn("Personal Loan application rejected: Only salaried individuals can apply");
            return "Personal Loan can only be applied by salaried individuals.";
        }
 
        if (loanType.equals(Constants.BUSINESS) && !employmentType.equals(Constants.SELF_EMPLOYED)) {
            logger.warn("Business Loan application rejected: Only self-employed individuals can apply");
            return "Business Loan can only be applied by self-employed individuals.";
        }
 
        if (loanType.equals("EDUCATION") && !employmentType.equals("STUDENT")) {
            logger.warn("Education Loan application rejected: Only students can apply");
            return "Educational Loan can only be applied by students.";
        }
        return null;
    }
 
    // Method to check for duplicate loan applications
    public String checkForDuplicateLoanApplication(LoanApplication loanApplication) {
        List<LoanApplication> existingLoans = loanApplicationRepo.findByAccountNumber(loanApplication.getAccountNumber());
 
        for (LoanApplication existingLoan : existingLoans) {
            if (existingLoan.getLoanType().equals(loanApplication.getLoanType())
                    && existingLoan.getStatus().equals(Constants.APPROVED)) {
                logger.warn("Duplicate loan application rejected: Already approved loan exists");
                return "You already have an approved " + loanApplication.getLoanType() + ". You cannot apply again.";
            }
        }
        return null;
    }
 
    /**
     * Updates the details of an existing loan application.
     *
     * @param loanapplication The updated loan application.
     */
    public void updateLoanapplication(LoanApplication loanapplication) {
        logger.info("Updating loan application with ID: " + loanapplication.getLoanId());
        loanApplicationRepo.save(loanapplication);
    }
 
    /**
     * Deletes a loan application by its ID.
     *
     * @param loanId The ID of the loan application to be deleted.
     */
    public void deleteLoanapplication(int loanId) {
        logger.info("Deleting loan application with ID: " + loanId);
        loanApplicationRepo.deleteById(loanId);
    }
 
    /**
     * Searches for loan applications by the account number.
     *
     * @param accountNumber The account number associated with loan applications.
     * @return List of loan applications for the given account number.
     */
    public List<LoanApplication> searchloanApplicationByaccountNumber(int accountNumber) {
        logger.info("Searching for loan applications with account number: " + accountNumber);
        return loanApplicationRepo.findByAccountNumber(accountNumber);
    }
 
    /**
     * Searches for loan applications by approval status.
     *
     * @param status The approval status of the loan applications (e.g., "APPROVED", "REJECTED").
     * @return List of loan applications with the specified status.
     */
    public List<LoanApplication> searchLoanApplicationByApprovalStatus(String status) {
        logger.info("Searching for loan applications with status: " + status);
        return loanApplicationRepo.findByStatus(status);
    }
 
    /**
     * Searches for loan applications by account number and loan type.
     *
     * @param accountNumber The account number associated with loan applications.
     * @param loanType The type of loan (e.g., "PERSONAL LOAN").
     * @return List of loan applications matching the given account number and loan type.
     */
    public List<LoanApplication> searchLoanApplicationsByAccountNumberAndLoanType(int accountNumber, String loanType) {
        logger.info("Searching for loan applications with account number: " + accountNumber + " and loan type: " + loanType);
        return loanApplicationRepo.findByAccountNumberAndLoanType(accountNumber, loanType);
    }
 
    /**
     * Accepts a loan application, updates the status, and processes loan sanction.
     *
     * @param loanId The ID of the loan application to be accepted.
     * @return True if the loan is successfully accepted, false otherwise.
     */
    public boolean acceptLoan(int loanId) {
		Optional<LoanApplication> loanApplicationOpt = loanApplicationRepo.findById(loanId);
 
		if (loanApplicationOpt.isPresent()) {
			LoanApplication loanApplication = loanApplicationOpt.get();
			loanApplication.setStatus("APPROVED");
			loanApplicationRepo.save(loanApplication);
 
			// Create loan sanction
			LoanSanction loanSanction = new LoanSanction();
			loanSanction.setLoanId(loanApplication.getLoanId());
			loanSanction.setSanctionAmount(loanApplication.getLoanAmount());
			loanSanction.setSanctionStatus("APPROVED");
			loanSanction.setInterestRate(loanApplication.getInterestRate());
 
			// Set sanction date
			Date sanctionDate = new Date();
			loanSanction.setSanctionDate(sanctionDate);
 
			// Calculate loan start date (1 month after sanction)
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(sanctionDate);
			calendar.add(Calendar.MONTH, 1);
			Date loanStartDate = calendar.getTime();
			loanSanction.setLoanStartDate(loanStartDate);
 
			// Convert tenure from years to months
			int tenureInYears = loanApplication.getTenure();
			int totalMonths = tenureInYears * 12;
 
			// Calculate loan end date
			calendar.setTime(loanStartDate);
			calendar.add(Calendar.MONTH, totalMonths);
			Date loanEndDate = calendar.getTime();
			loanSanction.setLoanEndDate(loanEndDate);
 
			// Calculate fixed monthly installment using compound interest
			double principal = loanApplication.getLoanAmount();
			double annualInterestRate = loanApplication.getInterestRate();
			double monthlyInterestRate = annualInterestRate / (12 * 100);
 
			double monthlyInstallment = principal * monthlyInterestRate *
					Math.pow(1 + monthlyInterestRate, totalMonths) /
					(Math.pow(1 + monthlyInterestRate, totalMonths) - 1);
 
			monthlyInstallment = Math.round(monthlyInstallment * 100.0) / 100.0; // Round to 2 decimal places
			loanSanction.setMonthlyInstallmentsAmount(monthlyInstallment);
			loanSanction.setSanctionedBy("Admin");
 
			// Save loan sanction
			loanSanctionRepo.save(loanSanction);
 
			// Generate monthly repayment records
			// Generate monthly repayment records
			calendar.setTime(loanStartDate);
			double remainingLoanAmount = principal;

			for (int month = 1; month <= totalMonths; month++) {
				LoanRepayments repayment = new LoanRepayments();
				repayment.setLoanId(loanId);
				repayment.setPaymentDate(calendar.getTime());
				repayment.setPaymentAmount(monthlyInstallment);
				repayment.setPaymentMode("LOAN_REPAYMENT");
				repayment.setPaymentStatus(Constants.PENDING);
				repayment.setDueLoanAmount(remainingLoanAmount);

				// Round dueLoanAmount to 2 decimal places
				repayment.setDueLoanAmount(Math.round(remainingLoanAmount * 100.0) / 100.0);

				loanRepaymentsRepo.save(repayment);

				// Update remaining loan amount and date for next month
				remainingLoanAmount = remainingLoanAmount - monthlyInstallment;
				calendar.add(Calendar.MONTH, 1);
			}

			// Credit loan amount to user
			bankTransactionService.creditLoanAmount(
					loanApplication.getAccountNumber(),
					loanApplication.getLoanAmount()
			);

			AccountDetails accountDetails = accountDetailsRepo.findByAccountNumber(loanApplication.getAccountNumber());
			emailService.sendLoanStatusEmail(accountDetails.getEmail(),
			        accountDetails.getFirstName() + " " + accountDetails.getLastName(),
			        loanApplication.getLoanType(),
			        loanApplication.getLoanAmount(),
			        true);

            logger.info("Loan with ID {} successfully approved");
			return true;
		}else {
            logger.error("Failed to approve loan with ID " + loanId);
            throw new LoanRejectionException("Failed to approve loan with ID " + loanId);
		}
	}

    /**
     * Rejects a loan application and updates its status.
     *
     * @param loanId The ID of the loan application to be rejected.
     * @return True if the loan is successfully rejected, false otherwise.
     */
    public boolean rejectLoan(int loanId) {
        logger.info("Attempting to reject loan with ID: " + loanId);
        Optional<LoanApplication> loanApplicationOpt = loanApplicationRepo.findById(loanId);
        
        if (loanApplicationOpt.isPresent()) {
            LoanApplication loanApplication = loanApplicationOpt.get();
            
            // Update loan status
            loanApplication.setStatus(Constants.REJECTED);
            loanApplicationRepo.save(loanApplication);

            AccountDetails accountDetails = accountDetailsRepo.findByAccountNumber(loanApplication.getAccountNumber());
            emailService.sendLoanStatusEmail(accountDetails.getEmail(),
                    accountDetails.getFirstName() + " " + accountDetails.getLastName(),
                    loanApplication.getLoanType(),
                    loanApplication.getLoanAmount(),
                    false); // Loan is rejected

            logger.info("Loan with ID {} successfully rejected");
            return true;
        } else {
            logger.error("Failed to reject loan with ID " + loanId);
            throw new LoanRejectionException("Failed to reject loan with ID " + loanId);
        }
    }

}
 