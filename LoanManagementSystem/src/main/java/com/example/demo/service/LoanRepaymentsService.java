package com.example.demo.service;
 
import java.util.Date;
import java.util.List;
import java.util.Optional;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.example.demo.model.LoanRepayments;
import com.example.demo.model.LoanSanction;
import com.example.demo.model.LoanApplication;
import com.example.demo.exception.AccountNotFoundException;
import com.example.demo.exception.EmailSendingException;
import com.example.demo.exception.InsufficientBalanceException;
import com.example.demo.exception.LoanApplicationNotFoundException;
import com.example.demo.exception.LoanRepaymentNotFoundException;
import com.example.demo.exception.PreclosureProcessingException;
import com.example.demo.model.AccountDetails;
import com.example.demo.model.BankTransaction;
import com.example.demo.model.Constants;
import com.example.demo.repo.LoanRepaymentsRepo;
import com.example.demo.repo.LoanSanctionRepo;
import com.example.demo.repo.LoanApplicationRepo;
import com.example.demo.repo.AccountDetailsRepo;
import com.example.demo.repo.BankTransactionRepo;
import jakarta.transaction.Transactional;
 
@Service
@Transactional
@EnableScheduling
public class LoanRepaymentsService {
 
    private static final Logger logger = LoggerFactory.getLogger(LoanRepaymentsService.class);
 
    private final LoanRepaymentsRepo loanRepaymentsRepo;
    private final LoanApplicationRepo loanApplicationRepo;
    private final BankTransactionRepo bankTransactionRepo;
    private final AccountDetailsRepo accountDetailsRepo;
    private final EmailService emailService;
    private final LoanSanctionRepo loanSanctionRepo;
 
   
    public LoanRepaymentsService(LoanRepaymentsRepo loanRepaymentsRepo, LoanApplicationRepo loanApplicationRepo,
                                 BankTransactionRepo bankTransactionRepo,
                                 AccountDetailsRepo accountDetailsRepo, EmailService emailService,
                                 LoanSanctionRepo loanSanctionRepo) {
        this.loanRepaymentsRepo = loanRepaymentsRepo;
        this.loanApplicationRepo = loanApplicationRepo;
        this.bankTransactionRepo = bankTransactionRepo;
        this.accountDetailsRepo = accountDetailsRepo;
        this.emailService = emailService;
        this.loanSanctionRepo = loanSanctionRepo;
    }
 
    public List<LoanRepayments> show() {
        return loanRepaymentsRepo.findAll();
    }
 
    public LoanRepayments searchByPaymentId(int paymentId) {
        return loanRepaymentsRepo.findById(paymentId).orElse(null);
    }
 
    public List<LoanRepayments> searchByLoanId(int loanId) {
        return loanRepaymentsRepo.findByLoanId(loanId);
    }
 
    public LoanRepayments addRepayment(LoanRepayments repayment) {
        logger.info("Adding repayment: {}", repayment);
        return loanRepaymentsRepo.save(repayment);
    }
 
    public LoanRepayments getLastRepayment(int loanId) {
        return loanRepaymentsRepo.findTopByLoanIdOrderByPaymentDateDesc(loanId)
                .orElse(null);
    }
 
    public List<LoanRepayments> searchByPaymentStatus(String paymentStatus) {
        return loanRepaymentsRepo.findByPaymentStatus(paymentStatus);
    }
 
    public LoanRepayments updatePaymentStatus(int paymentId, String status, double dueLoanAmount) {
        LoanRepayments repayment = loanRepaymentsRepo.findById(paymentId).orElse(null);
        if (repayment != null) {
            repayment.setPaymentStatus(status);
            repayment.setDueLoanAmount(dueLoanAmount);
            return loanRepaymentsRepo.save(repayment);
        }
        return null;
    }
    public void processNormalRepayments(LoanRepayments repayments) {
        logger.info("Processing normal repayment: {}", repayments);

        LoanRepayments repaymentsDb = loanRepaymentsRepo.findById(repayments.getPaymentId())
                .orElseThrow(() -> new LoanRepaymentNotFoundException("Repayment not found for Payment ID: " + repayments.getPaymentId()));
        repaymentsDb.setPaymentStatus(repayments.getPaymentStatus());
        repaymentsDb.setPaymentMode(repayments.getPaymentMode());

        Optional<LoanApplication> loanApplicationOpt = loanApplicationRepo.findById(repayments.getLoanId());
        
        if (loanApplicationOpt.isPresent()) {
            LoanApplication loanApplication = loanApplicationOpt.get();
            int accountNumber = loanApplication.getAccountNumber();
            AccountDetails accountDetails = accountDetailsRepo.findByAccountNumber(accountNumber);

            if (accountDetails != null) {
                // Update balance after repayment
                accountDetails.setBalance(accountDetails.getBalance() - repayments.getPaymentAmount());

                // Save updated repayment and account details
                loanRepaymentsRepo.save(repaymentsDb);
                accountDetailsRepo.save(accountDetails);
            }

            // Check if all repayments for the loan are completed
            boolean completed = getRepaymentsForLoan(repayments.getLoanId()).stream()
                    .noneMatch(r -> r.getPaymentStatus().equals(Constants.PENDING));

            // If all repayments are completed, close the loan sanction
            if (completed) {
                LoanSanction loanSanction = loanSanctionRepo.findByLoanId(repayments.getLoanId());
                if (loanSanction != null) {
                    loanSanction.setSanctionStatus(Constants.CLOSED);
                    loanSanctionRepo.save(loanSanction);
                }
            }

            // Initiate the repayment process
            initiateRepayment(repayments);

        } else {
            logger.error("Loan Application not found for Loan ID: {}", repayments.getLoanId());
        }
    }

 
    public void initiateRepayment(LoanRepayments repayment) {
        try {
            LoanApplication loanApplication = loanApplicationRepo.findById(repayment.getLoanId()).orElse(null);

            if (loanApplication == null) {
                logger.error("Loan Application not found for Loan ID: {}", repayment.getLoanId());
                updatePaymentStatus(repayment.getPaymentId(), Constants.FAILED, repayment.getDueLoanAmount());
                return;
            }

            int borrowerAccount = loanApplication.getAccountNumber();
            double amount = repayment.getPaymentAmount();

            // Extracted method to handle transaction and repayment logic
            processRepaymentTransaction(repayment, borrowerAccount, amount);

        } catch (Exception e) {
            logger.error("Repayment process failed for Payment ID: {}", repayment.getPaymentId(), e);
            updatePaymentStatus(repayment.getPaymentId(), Constants.FAILED, repayment.getDueLoanAmount());
        }
    }

    public void processRepaymentTransaction(LoanRepayments repayment, int borrowerAccount, double amount) {
        try {
            int lenderAccount = 0;

            // Create and save bank transaction
            BankTransaction transaction = new BankTransaction();
            transaction.setAccountNumber(borrowerAccount);
            transaction.setToAccNo(lenderAccount);
            transaction.setTransactionAmount(amount);
            transaction.setBalanceAfterTransaction(accountDetailsRepo.findByAccountNumber(borrowerAccount).getBalance() - amount);
            transaction.setTransactionDate(new Date());
            transaction.setTransactionType(Constants.LOAN_REPAYMENT);
            transaction.setTransactionStatus(Constants.SUCCESS);

            bankTransactionRepo.save(transaction);

            // Update repayment status
            double remainingDue = Math.max(repayment.getDueLoanAmount() - amount, 0);
            updatePaymentStatus(repayment.getPaymentId(), Constants.COMPLETED, remainingDue);
            logger.info("Repayment successful for Payment ID: {}", repayment.getPaymentId());

            // Send repayment success email
            sendRepaymentSuccessEmail(borrowerAccount, repayment, amount, remainingDue);

        } catch (Exception e) {
            logger.error("Transaction failed for Payment ID: {}", repayment.getPaymentId(), e);
            updatePaymentStatus(repayment.getPaymentId(), Constants.FAILED, repayment.getDueLoanAmount());
        }
    }
    public void sendRepaymentSuccessEmail(int borrowerAccount, LoanRepayments repayment, double amount, double remainingDue) {
        AccountDetails borrowerDetails = accountDetailsRepo.findByAccountNumber(borrowerAccount);
        
        if (borrowerDetails != null) {
            try {
                emailService.sendLoanRepaymentEmail(
                        borrowerDetails.getEmail(),
                        borrowerDetails.getFirstName() + " " + borrowerDetails.getLastName(),
                        repayment.getLoanId(),
                        amount,
                        remainingDue,
                        new Date(),
                        repayment.getPaymentMode()
                );
                logger.info("Loan repayment success email sent to: {}", borrowerDetails.getEmail());
            } catch (EmailSendingException e) {
                logger.error("Failed to send loan repayment success email to account: {} - Error: {}", borrowerAccount, e.getMessage());
            }
        } else {
            logger.warn("Borrower account details not found for account number: {}", borrowerAccount);
        }
    }


 
    public List<LoanRepayments> getRepaymentsForLoan(int loanId) {
        LoanSanction loanSanction = loanSanctionRepo.findByLoanId(loanId);
 
        if (loanSanction != null) {
            Date loanStartDate = loanSanction.getLoanStartDate();
            Date loanEndDate = loanSanction.getLoanEndDate();
 
            // Fetch all repayments within the loan start and end date
            return loanRepaymentsRepo.findRepaymentsByLoanIdAndPaymentDateBetween(
                    loanId, loanStartDate, loanEndDate);
        }
 
        return null;
    }
 
    @Transactional
    public void processPreclosure(int loanId, int accountNumber, double preclosureAmount, String paymentMode) {
        try {
            logger.info("Processing pre-closure for Loan ID: {}", loanId);

            // Get the loan application
            Optional<LoanApplication> loanApplicationOpt = loanApplicationRepo.findById(loanId);
            if (!loanApplicationOpt.isPresent()) {
                throw new LoanApplicationNotFoundException("Loan not found: " + loanId);  // Custom exception for loan not found
            }
            LoanApplication loanApplication = loanApplicationOpt.get();

            // Get all pending repayments for this loan
            List<LoanRepayments> pendingRepayments = loanRepaymentsRepo.findByLoanIdAndPaymentStatus(loanId, Constants.PENDING);

            // Get account details
            AccountDetails accountDetails = accountDetailsRepo.findByAccountNumber(accountNumber);
            if (accountDetails == null) {
                throw new AccountNotFoundException("Account not found: " + accountNumber);  // Custom exception for account not found
            }

            // Check if account has sufficient balance
            if (accountDetails.getBalance() < preclosureAmount) {
                throw new InsufficientBalanceException("Insufficient balance for pre-closure");  // Custom exception for insufficient balance
            }

            // Create transaction for pre-closure payment
            BankTransaction transaction = new BankTransaction();
            transaction.setAccountNumber(accountNumber);
            transaction.setToAccNo(0); // Bank account
            transaction.setTransactionAmount(preclosureAmount);
            transaction.setBalanceAfterTransaction(accountDetails.getBalance() - preclosureAmount);
            transaction.setTransactionDate(new Date());
            transaction.setTransactionType(Constants.LOAN_PRECLOSURE);
            transaction.setTransactionStatus(Constants.SUCCESS);
            bankTransactionRepo.save(transaction);

            // Deduct amount from account
            accountDetails.setBalance(accountDetails.getBalance() - preclosureAmount);
            accountDetailsRepo.save(accountDetails);

            // Mark all pending repayments as COMPLETED
            for (LoanRepayments repayment : pendingRepayments) {
                repayment.setPaymentStatus(Constants.COMPLETED);
                repayment.setPaymentMode(paymentMode);
                repayment.setDueLoanAmount(0);
                loanRepaymentsRepo.save(repayment);
            }

            // Update loan status to CLOSED in LoanApplication
            loanApplication.setStatus(Constants.CLOSED);
            loanApplicationRepo.save(loanApplication);

            // Update loan sanction status
            LoanSanction loanSanction = loanSanctionRepo.findByLoanId(loanId);
            if (loanSanction != null) {
                loanSanction.setSanctionStatus(Constants.CLOSED);
                loanSanctionRepo.save(loanSanction);
            }

            // Send email notification
            AccountDetails borrowerDetails = accountDetailsRepo.findByAccountNumber(accountNumber);
            if (borrowerDetails != null) {
                emailService.sendLoanPreclosureEmail(
                        borrowerDetails.getEmail(),
                        borrowerDetails.getFirstName() + " " + borrowerDetails.getLastName(),
                        loanId,
                        preclosureAmount,
                        new Date(),
                        paymentMode
                );
            }

        } catch (LoanApplicationNotFoundException | AccountNotFoundException | InsufficientBalanceException e) {
            // Log and rethrow specific exceptions for higher-level handling
            logger.error("Pre-closure failed for Loan ID: {} - Reason: {}", loanId, e.getMessage());
            throw e;
        } catch (Exception e) {
            // Log unexpected errors and rethrow them with contextual information
            logger.error("Unexpected error occurred during pre-closure for Loan ID: {} - Error: {}", loanId, e.getMessage(), e);
            throw new PreclosureProcessingException(
                    String.format("Pre-closure failed for Loan ID: %d due to an unexpected error. Error details: %s", loanId, e.getMessage())
            );
        }
    }


 
    @Scheduled(cron = "0 0 10 * * ?")
    public void processLoanDueReminders() {
        logger.info("Processing loan due reminders...");

        Date today = new Date();
        List<LoanRepayments> overdueRepayments = loanRepaymentsRepo.findByPaymentDateBeforeAndPaymentStatus(today, "PENDING");

        for (LoanRepayments repayment : overdueRepayments) {
            // Fetch the loan details using the loanId
            LoanApplication loan = loanApplicationRepo.findById(repayment.getLoanId()).orElse(null);

            if (loan != null) {
                // Get the account number from the loan or user
                int accountNumber = loan.getAccountNumber();

                // Fetch the account details using the account number
                AccountDetails account = accountDetailsRepo.findByAccountNumber(accountNumber);

                if (account != null) {
                    try {
                        emailService.sendLoanDueReminder(
                            account.getEmail(),
                            account.getFirstName() + " " + account.getLastName(),
                            repayment.getLoanId(),
                            repayment.getDueLoanAmount(),
                            repayment.getPaymentDate()
                        );
                        logger.info("Reminder email sent to account: {}", accountNumber);
                    } catch (EmailSendingException e) {
                        logger.error("Failed to send email to account: {} - Error: {}", accountNumber, e.getMessage());
                    }
                } else {
                    logger.warn("Account not found for account number: {}", accountNumber);
                }
            } else {
                logger.warn("Loan details not found for loan ID: {}", repayment.getLoanId());
            }
        }
    }

}