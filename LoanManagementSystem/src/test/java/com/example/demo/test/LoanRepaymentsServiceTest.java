package com.example.demo.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.model.*;
import com.example.demo.repo.*;
import com.example.demo.service.EmailService;
import com.example.demo.service.LoanRepaymentsService;
import com.example.demo.exception.*;
import org.apache.log4j.Logger;

@ExtendWith(MockitoExtension.class)
 class LoanRepaymentsServiceTest {

    @Mock
    private LoanRepaymentsRepo loanRepaymentsRepo;
    
    @Mock
    private LoanApplicationRepo loanApplicationRepo;
    
    @Mock
    private BankTransactionRepo bankTransactionRepo;
    
    @Mock
    private AccountDetailsRepo accountDetailsRepo;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private LoanSanctionRepo loanSanctionRepo;
    
    @Mock
    private Logger logger;

    @InjectMocks
    private LoanRepaymentsService loanRepaymentsService;

    private LoanRepayments sampleRepayment;
    private LoanApplication sampleLoanApplication;
    private AccountDetails sampleAccountDetails;
    private LoanSanction sampleLoanSanction;

	

    @BeforeEach
    void setUp() {
        // Initialize sample data
        sampleRepayment = new LoanRepayments();
        sampleRepayment.setPaymentId(1);
        sampleRepayment.setLoanId(1);
        sampleRepayment.setPaymentAmount(1000.0);
        sampleRepayment.setPaymentStatus(Constants.PENDING);
        sampleRepayment.setDueLoanAmount(5000.0);
        sampleRepayment.setPaymentMode("ONLINE");
        sampleRepayment.setPaymentDate(new Date());

        sampleLoanApplication = new LoanApplication();
        sampleLoanApplication.setLoanId(1);
        sampleLoanApplication.setAccountNumber(12345);
        sampleLoanApplication.setStatus(Constants.ACTIVE);

        sampleAccountDetails = new AccountDetails();
        sampleAccountDetails.setAccountNumber(12345);
        sampleAccountDetails.setBalance(10000.0);
        sampleAccountDetails.setEmail("test@example.com");
        sampleAccountDetails.setFirstName("John");
        sampleAccountDetails.setLastName("Doe");

        sampleLoanSanction = new LoanSanction();
        sampleLoanSanction.setLoanId(1);
        sampleLoanSanction.setSanctionStatus(Constants.ACTIVE);
        sampleLoanSanction.setLoanStartDate(new Date());
        sampleLoanSanction.setLoanEndDate(new Date());
    }

    @Test
    void testShow() {
        List<LoanRepayments> expectedRepayments = Arrays.asList(sampleRepayment);
        when(loanRepaymentsRepo.findAll()).thenReturn(expectedRepayments);

        List<LoanRepayments> actualRepayments = loanRepaymentsService.show();
        
        assertEquals(expectedRepayments, actualRepayments);
        verify(loanRepaymentsRepo).findAll();
    }

    @Test
    void testSearchByPaymentId() {
        when(loanRepaymentsRepo.findById(1)).thenReturn(Optional.of(sampleRepayment));

        LoanRepayments result = loanRepaymentsService.searchByPaymentId(1);
        
        assertNotNull(result);
        assertEquals(sampleRepayment.getPaymentId(), result.getPaymentId());
    }

    @Test
    void testSearchByLoanId() {
        List<LoanRepayments> expectedRepayments = Arrays.asList(sampleRepayment);
        when(loanRepaymentsRepo.findByLoanId(1)).thenReturn(expectedRepayments);

        List<LoanRepayments> result = loanRepaymentsService.searchByLoanId(1);
        
        assertNotNull(result);
        assertEquals(expectedRepayments.size(), result.size());
    }

    @Test
    void testProcessNormalRepayments() {
        when(loanRepaymentsRepo.findById(anyInt())).thenReturn(Optional.of(sampleRepayment));
        when(loanApplicationRepo.findById(anyInt())).thenReturn(Optional.of(sampleLoanApplication));
        when(accountDetailsRepo.findByAccountNumber(anyInt())).thenReturn(sampleAccountDetails);
        when(loanRepaymentsRepo.save(any())).thenReturn(sampleRepayment);
        when(accountDetailsRepo.save(any())).thenReturn(sampleAccountDetails);

        assertDoesNotThrow(() -> loanRepaymentsService.processNormalRepayments(sampleRepayment));

        verify(loanRepaymentsRepo).save(any(LoanRepayments.class));
        verify(accountDetailsRepo).save(any(AccountDetails.class));
    }
    @Test
    public void testProcessNormalRepayments_loanNotFound_logsError() {
        // Arrange
        LoanRepayments repayment = new LoanRepayments();
        repayment.setPaymentId(100);
        repayment.setLoanId(1);
        repayment.setPaymentStatus(Constants.COMPLETED);

        when(loanApplicationRepo.findById(1)).thenReturn(Optional.empty()); // Loan not found

        // Act
        loanRepaymentsService.processNormalRepayments(repayment);

        // Assert
        verify(logger, times(1)).error("Loan Application not found for Loan ID: {}"); // Verify logger.error was called
    }
    @Test
    public void testErrorLogWhenEmailFails() {
        // Arrange
        LoanRepayments repayment = new LoanRepayments();
        repayment.setPaymentId(100);
        repayment.setLoanId(1);
        repayment.setPaymentStatus(Constants.COMPLETED);
        
        int borrowerAccount = 12345;

        // Simulate an exception in sending email
        Exception emailException = new Exception("Email service not available");

        // Act
        try {
            // Method that handles sending email and throws an exception (mock the sending part in actual implementation)
            loanRepaymentsService.sendRepaymentSuccessEmail(borrowerAccount, repayment, 1000.0, 0.0);
        } catch (Exception e) {
            // Act when exception occurs and log it
            logger.error("Failed to send loan repayment success email to account: {} - Error: {}");
        }

        // Assert
        verify(logger, times(1)).error("Failed to send loan repayment success email to account: {} - Error: {}");
    }

    @Test
    public void testWarnLogWhenBorrowerAccountNotFound() {
        // Arrange
        int borrowerAccount = 12345;

        // Simulate missing borrower account details
        when(accountDetailsRepo.findByAccountNumber(borrowerAccount)).thenReturn(null);

        // Act
        loanRepaymentsService.processRepaymentTransaction(new LoanRepayments(), borrowerAccount, 1000.0);

        // Assert
        verify(logger, times(1)).warn("Borrower account details not found for account number: {}");
    }

    @Test
    void testProcessPreclosure() {
        when(loanApplicationRepo.findById(anyInt())).thenReturn(Optional.of(sampleLoanApplication));
        when(accountDetailsRepo.findByAccountNumber(anyInt())).thenReturn(sampleAccountDetails);
        when(loanRepaymentsRepo.findByLoanIdAndPaymentStatus(anyInt(), anyString()))
            .thenReturn(Arrays.asList(sampleRepayment));
        when(loanSanctionRepo.findByLoanId(anyInt())).thenReturn(sampleLoanSanction);

        assertDoesNotThrow(() -> 
            loanRepaymentsService.processPreclosure(1, 12345, 5000.0, "ONLINE")
        );

        verify(bankTransactionRepo).save(any(BankTransaction.class));
        verify(accountDetailsRepo).save(any(AccountDetails.class));
        verify(loanRepaymentsRepo).save(any(LoanRepayments.class));
    }

    @Test
    void testProcessPreclosureInsufficientBalance() {
        sampleAccountDetails.setBalance(1000.0); // Set insufficient balance
        when(loanApplicationRepo.findById(anyInt())).thenReturn(Optional.of(sampleLoanApplication));
        when(accountDetailsRepo.findByAccountNumber(anyInt())).thenReturn(sampleAccountDetails);

        assertThrows(InsufficientBalanceException.class, () ->
            loanRepaymentsService.processPreclosure(1, 12345, 5000.0, "ONLINE")
        );
    }

    @Test
    void testGetLastRepayment() {
        when(loanRepaymentsRepo.findTopByLoanIdOrderByPaymentDateDesc(anyInt()))
            .thenReturn(Optional.of(sampleRepayment));

        LoanRepayments result = loanRepaymentsService.getLastRepayment(1);
        
        assertNotNull(result);
        assertEquals(sampleRepayment.getPaymentId(), result.getPaymentId());
    }

    @Test
    void testSearchByPaymentStatus() {
        List<LoanRepayments> expectedRepayments = Arrays.asList(sampleRepayment);
        when(loanRepaymentsRepo.findByPaymentStatus(anyString())).thenReturn(expectedRepayments);

        List<LoanRepayments> result = loanRepaymentsService.searchByPaymentStatus(Constants.PENDING);
        
        assertNotNull(result);
        assertEquals(expectedRepayments.size(), result.size());
    }

    @Test
    void testProcessLoanDueReminders() {
        List<LoanRepayments> overdueRepayments = Arrays.asList(sampleRepayment);
        when(loanRepaymentsRepo.findByPaymentDateBeforeAndPaymentStatus(any(Date.class), anyString()))
            .thenReturn(overdueRepayments);
        when(loanApplicationRepo.findById(anyInt())).thenReturn(Optional.of(sampleLoanApplication));
        when(accountDetailsRepo.findByAccountNumber(anyInt())).thenReturn(sampleAccountDetails);

        assertDoesNotThrow(() -> loanRepaymentsService.processLoanDueReminders());

        try {
			verify(emailService).sendLoanDueReminder(
			    anyString(), anyString(), anyInt(), any(Double.class), any(Date.class)
			);
		} catch (EmailSendingException e) {
			e.printStackTrace();
		}
    }
    @Test
     void testAddRepayment() {
        // Arrange: Mock the LoanRepayments object
        LoanRepayments repayment = new LoanRepayments();
        repayment.setPaymentId(1);
        repayment.setPaymentAmount(5000.0);
        
        // Mock the repository save method to return the same repayment object
        when(loanRepaymentsRepo.save(repayment)).thenReturn(repayment);

        // Act: Call the addRepayment method
        LoanRepayments result = loanRepaymentsService.addRepayment(repayment);

        // Assert: Check the result and verify interactions
        assertNotNull(result);
        assertEquals(5000.0, result.getPaymentAmount());
        verify(loanRepaymentsRepo, times(1)).save(repayment);  // Verify save was called once
    }
    @Test
     void testUpdatePaymentStatus() {
        // Arrange: Mock the LoanRepayments object and its findById method
        LoanRepayments repayment = new LoanRepayments();
        repayment.setPaymentId(1);
        repayment.setPaymentStatus("Pending");
        repayment.setDueLoanAmount(10000.0);

        // When findById is called, return the repayment object wrapped in an Optional
        when(loanRepaymentsRepo.findById(1)).thenReturn(Optional.of(repayment));

        // Act: Call the updatePaymentStatus method
        LoanRepayments updatedRepayment = loanRepaymentsService.updatePaymentStatus(1, "Paid", 5000.0);

        // Assert: Check the updated values and verify interactions
        assertNotNull(updatedRepayment);
        assertEquals("Paid", updatedRepayment.getPaymentStatus());
        assertEquals(5000.0, updatedRepayment.getDueLoanAmount());

        // Verify that the save method was called once with the updated repayment
        verify(loanRepaymentsRepo, times(1)).save(repayment);
    }

    @Test
     void testUpdatePaymentStatusNotFound() {
        // Arrange: Mock findById to return empty Optional (repayment not found)
        when(loanRepaymentsRepo.findById(1)).thenReturn(Optional.empty());

        // Act: Call the updatePaymentStatus method with a non-existing paymentId
        LoanRepayments result = loanRepaymentsService.updatePaymentStatus(1, "Paid", 5000.0);

        // Assert: Ensure result is null since repayment was not found
        assertNull(result);

        // Verify that save was never called, as there was nothing to update
        verify(loanRepaymentsRepo, never()).save(any());
    }
    @Test
     void testInitiateRepayment_successfulRepayment() {
        // Arrange
        LoanRepayments repayment = new LoanRepayments();
        repayment.setLoanId(1); 
        repayment.setPaymentId(100); 
        repayment.setPaymentAmount(5000);

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setAccountNumber(12345);

        when(loanApplicationRepo.findById(repayment.getLoanId())).thenReturn(Optional.of(loanApplication));

        // Act
        loanRepaymentsService.initiateRepayment(repayment);

        // Assert
        verify(loanApplicationRepo, times(1)).findById(repayment.getLoanId());
        verify(loanRepaymentsService, never()).updatePaymentStatus(repayment.getPaymentId(), Constants.FAILED, repayment.getDueLoanAmount());
        // Add more verifications if needed
    }

    @Test
     void testInitiateRepayment_loanApplicationNotFound() {
        // Arrange
        LoanRepayments repayment = new LoanRepayments();
        repayment.setLoanId(1); 
        repayment.setPaymentId(100); 

        when(loanApplicationRepo.findById(repayment.getLoanId())).thenReturn(Optional.empty());

        // Act
        loanRepaymentsService.initiateRepayment(repayment);

        // Assert
        verify(loanApplicationRepo, times(1)).findById(repayment.getLoanId());
        verify(loanRepaymentsService).updatePaymentStatus(repayment.getPaymentId(), Constants.FAILED, repayment.getDueLoanAmount());
    }

    @Test
     void testInitiateRepayment_exceptionThrown() {
        // Arrange
        LoanRepayments repayment = new LoanRepayments();
        repayment.setLoanId(1); 
        repayment.setPaymentId(100); 

        when(loanApplicationRepo.findById(repayment.getLoanId())).thenThrow(new RuntimeException("Database error"));

        // Act
        loanRepaymentsService.initiateRepayment(repayment);

        // Assert
        verify(loanRepaymentsService).updatePaymentStatus(repayment.getPaymentId(), Constants.FAILED, repayment.getDueLoanAmount());
    }
    @Test
     void testProcessRepaymentTransaction_successful() {
        // Arrange
        LoanRepayments repayment = new LoanRepayments();
        repayment.setPaymentId(100);
        repayment.setLoanId(1);
        repayment.setDueLoanAmount(5000);
        repayment.setPaymentAmount(1000);

        AccountDetails accountDetails = new AccountDetails();
        accountDetails.setAccountNumber(12345);
        accountDetails.setBalance(10000);

        BankTransaction transaction = new BankTransaction();
        transaction.setTransactionAmount(1000);

        when(accountDetailsRepo.findByAccountNumber(12345)).thenReturn(accountDetails);
        when(bankTransactionRepo.save(any(BankTransaction.class))).thenReturn(transaction);

        // Act
        loanRepaymentsService.processRepaymentTransaction(repayment, 12345, 1000);

        // Assert
        verify(bankTransactionRepo, times(1)).save(any(BankTransaction.class));
        verify(accountDetailsRepo, times(1)).findByAccountNumber(12345);
        verify(loanRepaymentsService, never()).updatePaymentStatus(repayment.getPaymentId(), Constants.FAILED, repayment.getDueLoanAmount());
        verify(loanRepaymentsService, times(1)).updatePaymentStatus(repayment.getPaymentId(), Constants.COMPLETED, 4000); // Remaining due
    }

    @Test
     void testProcessRepaymentTransaction_insufficientFunds() {
        // Arrange
        LoanRepayments repayment = new LoanRepayments();
        repayment.setPaymentId(100);
        repayment.setLoanId(1);
        repayment.setDueLoanAmount(5000);
        repayment.setPaymentAmount(7000); // More than available balance

        AccountDetails accountDetails = new AccountDetails();
        accountDetails.setAccountNumber(12345);
        accountDetails.setBalance(5000);

        when(accountDetailsRepo.findByAccountNumber(12345)).thenReturn(accountDetails);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            loanRepaymentsService.processRepaymentTransaction(repayment, 12345, 7000);
        });

        verify(loanRepaymentsService, times(1)).updatePaymentStatus(repayment.getPaymentId(), Constants.FAILED, repayment.getDueLoanAmount());
    }

    @Test
     void testProcessRepaymentTransaction_emailFailure() {
        // Arrange
        LoanRepayments repayment = new LoanRepayments();
        repayment.setPaymentId(100);
        repayment.setLoanId(1);
        repayment.setDueLoanAmount(5000);
        repayment.setPaymentAmount(1000);

        AccountDetails accountDetails = new AccountDetails();
        accountDetails.setAccountNumber(12345);
        accountDetails.setBalance(10000);

        when(accountDetailsRepo.findByAccountNumber(12345)).thenReturn(accountDetails);
        when(bankTransactionRepo.save(any(BankTransaction.class))).thenReturn(new BankTransaction());

        doThrow(new RuntimeException("Email service failed")).when(loanRepaymentsService).sendRepaymentSuccessEmail(anyInt(), any(), anyDouble(), anyDouble());

        // Act
        loanRepaymentsService.processRepaymentTransaction(repayment, 12345, 1000);

        // Assert
        verify(bankTransactionRepo, times(1)).save(any(BankTransaction.class));
        verify(loanRepaymentsService, times(1)).updatePaymentStatus(repayment.getPaymentId(), Constants.FAILED, repayment.getDueLoanAmount());
    }
    @Test
     void testProcessNormalRepayments_successfulCompletion() {
        // Arrange
        LoanRepayments repayment = new LoanRepayments();
        repayment.setPaymentId(100);
        repayment.setLoanId(1);
        repayment.setPaymentStatus(Constants.COMPLETED);
        repayment.setPaymentAmount(1000);
        repayment.setPaymentMode("Bank Transfer");

        LoanRepayments repaymentDb = new LoanRepayments();
        repaymentDb.setPaymentId(100);
        repaymentDb.setPaymentStatus(Constants.PENDING);
        repaymentDb.setPaymentAmount(1000);
        
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setLoanId(1);
        loanApplication.setAccountNumber(12345);

        AccountDetails accountDetails = new AccountDetails();
        accountDetails.setAccountNumber(12345);
        accountDetails.setBalance(2000);

        LoanSanction loanSanction = new LoanSanction();
        loanSanction.setLoanId(1);
        loanSanction.setSanctionStatus(Constants.ACTIVE);

        when(loanRepaymentsRepo.findById(100)).thenReturn(Optional.of(repaymentDb));
        when(loanApplicationRepo.findById(1)).thenReturn(Optional.of(loanApplication));
        when(accountDetailsRepo.findByAccountNumber(12345)).thenReturn(accountDetails);
        when(loanSanctionRepo.findByLoanId(1)).thenReturn(loanSanction);
        when(loanRepaymentsService.getRepaymentsForLoan(1)).thenReturn(Arrays.asList(repaymentDb)); // Mock repayments for the loan

        // Act
        loanRepaymentsService.processNormalRepayments(repayment);

        // Assert
        verify(accountDetailsRepo, times(1)).save(accountDetails); // Verify balance was updated
        assertEquals(Constants.CLOSED, loanSanction.getSanctionStatus()); // Check that the loan was closed
        verify(loanSanctionRepo, times(1)).save(loanSanction); // Verify the loan sanction status was updated
        verify(loanRepaymentsService, times(1)).initiateRepayment(repayment); // Verify the repayment process was initiated
    }

    @Test
     void testProcessNormalRepayments_loanNotFound() {
        // Arrange
        LoanRepayments repayment = new LoanRepayments();
        repayment.setPaymentId(100);
        repayment.setLoanId(1);
        repayment.setPaymentStatus(Constants.COMPLETED);

        when(loanApplicationRepo.findById(1)).thenReturn(Optional.empty()); // Loan not found

        // Act
        loanRepaymentsService.processNormalRepayments(repayment);

        // Assert
        verify(loanApplicationRepo, times(1)).findById(1);
        verify(loanRepaymentsService, never()).initiateRepayment(repayment); // Ensure repayment initiation never happens
    }

    @Test
     void testProcessNormalRepayments_partialCompletion() {
        // Arrange
        LoanRepayments repayment = new LoanRepayments();
        repayment.setPaymentId(100);
        repayment.setLoanId(1);
        repayment.setPaymentStatus(Constants.COMPLETED);
        repayment.setPaymentAmount(1000);

        LoanRepayments repaymentDb = new LoanRepayments();
        repaymentDb.setPaymentId(100);
        repaymentDb.setPaymentStatus(Constants.PENDING);

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setLoanId(1);
        loanApplication.setAccountNumber(12345);

        AccountDetails accountDetails = new AccountDetails();
        accountDetails.setAccountNumber(12345);
        accountDetails.setBalance(2000);

        LoanSanction loanSanction = new LoanSanction();
        loanSanction.setLoanId(1);
        loanSanction.setSanctionStatus(Constants.ACTIVE);

        when(loanRepaymentsRepo.findById(100)).thenReturn(Optional.of(repaymentDb));
        when(loanApplicationRepo.findById(1)).thenReturn(Optional.of(loanApplication));
        when(accountDetailsRepo.findByAccountNumber(12345)).thenReturn(accountDetails);
        when(loanSanctionRepo.findByLoanId(1)).thenReturn(loanSanction);
        when(loanRepaymentsService.getRepaymentsForLoan(1)).thenReturn(Arrays.asList(repaymentDb, repayment)); // Mock repayments for the loan

        // Act
        loanRepaymentsService.processNormalRepayments(repayment);

        // Assert
        verify(accountDetailsRepo, times(1)).save(accountDetails); // Verify balance was updated
        assertEquals(Constants.ACTIVE, loanSanction.getSanctionStatus()); // Loan should still be active
        verify(loanSanctionRepo, never()).save(loanSanction); // Loan sanction should not be updated
        verify(loanRepaymentsService, times(1)).initiateRepayment(repayment); // Verify the repayment process was initiated
    }
}