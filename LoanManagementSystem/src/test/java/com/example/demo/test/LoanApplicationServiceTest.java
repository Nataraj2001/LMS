package com.example.demo.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.demo.exception.EmailSendingException;
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
import com.example.demo.service.BankTransactionService;
import com.example.demo.service.EmailService;
import com.example.demo.service.LoanApplicationService;

class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepo loanApplicationRepo;
    @Mock
    private LoanSanctionRepo loanSanctionRepo;
    @Mock
    private AccountDetailsRepo accountDetailsRepo;
    @Mock
    private LoanRepaymentsRepo loanRepaymentsRepo;
    @Mock
    private EmailService emailService;
    @Mock
    private BankTransactionService bankTransactionService;
    @Mock
    private LoanApplicationService loanApplicationService;
    @Mock
    private Logger logger;

    // Custom appender to capture log events
    private TestAppender testAppender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Instantiate the service using constructor injection
        loanApplicationService = new LoanApplicationService(
                loanApplicationRepo, loanSanctionRepo, accountDetailsRepo,
                loanRepaymentsRepo, emailService, bankTransactionService);

        // Attach custom appender to the static logger in LoanApplicationService
        testAppender = new TestAppender();
        Logger.getLogger(LoanApplicationService.class).addAppender(testAppender);
    }

    @AfterEach
    void tearDown() {
        Logger.getLogger(LoanApplicationService.class).removeAppender(testAppender);
    }

    // A simple custom Appender to capture log messages.
    private static class TestAppender extends AppenderSkeleton {
        private final List<LoggingEvent> events = new ArrayList<>();

        @Override
        protected void append(LoggingEvent event) {
            events.add(event);
        }

         List<LoggingEvent> getEvents() {
            return events;
        }

         @Override
         public void close() {
             throw new UnsupportedOperationException("close() operation is not supported in LoanApplicationServiceTest.");
         }

        @Override
		
        public  boolean requiresLayout() {
            return false;
        }
    }

    // ==================== Tests for addLoanApplication() ====================

    @Test
    void testAddLoanApplication_BadRequest_DueToLoanTypeRestriction() {
        // Create a loan application that triggers a loan type restriction (e.g. already has an Education Loan)
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setAccountNumber(1001);
        loanApplication.setLoanType(Constants.EDUCATION);

        // Simulate an existing Education loan
        LoanApplication existingLoan = new LoanApplication();
        existingLoan.setLoanType(Constants.EDUCATION);
        when(loanApplicationRepo.findByAccountNumber(1001)).thenReturn(Arrays.asList(existingLoan));

        ResponseEntity<String> response = loanApplicationService.addLoanApplication(loanApplication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("You cannot apply for any other loan if you already have an Education Loan.", response.getBody());

        // Verify that a warn log was generated
        boolean foundWarn = testAppender.getEvents().stream().anyMatch(e ->
                e.getLevel().toString().equals("WARN") &&
                e.getRenderedMessage().contains("Existing Education Loan prevents new loan applications"));
        assertTrue(foundWarn, "Expected warn log for education loan restriction not found.");
    }

    @Test
    void testAddLoanApplication_BadRequest_DueToEmploymentRestriction() {
        // Loan type PERSONAL but employment type is not SALARIED
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setAccountNumber(1002);
        loanApplication.setLoanType("PERSONAL");
        loanApplication.setEmployType("SELF_EMPLOYED");

        when(loanApplicationRepo.findByAccountNumber(1002)).thenReturn(Collections.emptyList());

        ResponseEntity<String> response = loanApplicationService.addLoanApplication(loanApplication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Personal Loan can only be applied by salaried individuals.", response.getBody());

        boolean foundWarn = testAppender.getEvents().stream().anyMatch(e ->
                e.getLevel().toString().equals("WARN") &&
                e.getRenderedMessage().contains("Personal Loan application rejected: Only salaried individuals can apply"));
        assertTrue(foundWarn, "Expected warn log for employment restriction not found.");
    }

    @Test
    void testAddLoanApplication_BadRequest_DueToDuplicateLoan() {
        // Duplicate check: already an approved loan exists.
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setAccountNumber(1003);
        loanApplication.setLoanType("PERSONAL");
        loanApplication.setStatus("PENDING");  // new application 

        LoanApplication existingLoan = new LoanApplication();
        existingLoan.setLoanType("PERSONAL");
        existingLoan.setStatus(Constants.APPROVED);
        when(loanApplicationRepo.findByAccountNumber(1003)).thenReturn(Arrays.asList(existingLoan));

        ResponseEntity<String> response = loanApplicationService.addLoanApplication(loanApplication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("You already have an approved PERSONAL. You cannot apply again.", response.getBody());

        boolean foundWarn = testAppender.getEvents().stream().anyMatch(e ->
                e.getLevel().toString().equals("WARN") &&
                e.getRenderedMessage().contains("Duplicate loan application rejected: Already approved loan exists"));
        assertTrue(foundWarn, "Expected warn log for duplicate loan restriction not found.");
    }

    @Test
    void testAddLoanApplication_Success() throws EmailSendingException {
        // A valid loan application
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setAccountNumber(1004);
        loanApplication.setLoanType("PERSONAL");
        loanApplication.setEmployType("SALARIED");
        loanApplication.setLoanAmount(50000.0);

        when(loanApplicationRepo.findByAccountNumber(1004)).thenReturn(Collections.emptyList());
        
        // Assume account exists
        AccountDetails accountDetails = new AccountDetails();
        accountDetails.setAccountNumber(1004);
        accountDetails.setEmail("test@example.com");
        accountDetails.setFirstName("John");
        when(accountDetailsRepo.findByAccountNumber(1004)).thenReturn(accountDetails);
        
        doNothing().when(emailService).sendLoanApplicationEmail(anyString(), anyString(), anyString(), anyDouble());

        ResponseEntity<String> response = loanApplicationService.addLoanApplication(loanApplication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Loan application submitted successfully.", response.getBody());

        verify(loanApplicationRepo, times(1)).save(loanApplication);
        
        // Directly pass the values, removing eq(...)
        verify(emailService, times(1)).sendLoanApplicationEmail(
                accountDetails.getEmail(),
                accountDetails.getFirstName(),
                loanApplication.getLoanType(),
                loanApplication.getLoanAmount()
        );
    }

    // ==================== Tests for acceptLoan() ====================

    @Test
    void testAcceptLoan_Success() throws EmailSendingException {
        // Prepare a loan application
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setLoanId(1);
        loanApplication.setAccountNumber(2001);
        loanApplication.setLoanAmount(100000.0);
        loanApplication.setInterestRate(12.0);
        loanApplication.setTenure(5);
        loanApplication.setStatus("PENDING");

        when(loanApplicationRepo.findById(1)).thenReturn(Optional.of(loanApplication));
        when(loanApplicationRepo.save(any(LoanApplication.class))).thenReturn(loanApplication);
        // Assume no existing sanction for simplicity
        when(loanSanctionRepo.findByLoanId(1)).thenReturn(null);
        when(loanRepaymentsRepo.save(any(LoanRepayments.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(bankTransactionService).creditLoanAmount(anyInt(), anyDouble());
        // Provide account details
        AccountDetails accDetails = new AccountDetails();
        accDetails.setAccountNumber(2001);
        accDetails.setEmail("user@example.com");
        accDetails.setFirstName("Jane");
        when(accountDetailsRepo.findByAccountNumber(2001)).thenReturn(accDetails);
        doNothing().when(emailService).sendLoanStatusEmail(anyString(), anyString(), null, anyDouble(),anyBoolean());

        boolean result = loanApplicationService.acceptLoan(1);
        assertTrue(result);
        verify(loanApplicationRepo, times(1)).findById(1);
        verify(loanApplicationRepo, times(1)).save(loanApplication);
    }

    // ==================== Tests for rejectLoan() ====================

    @Test
    void testRejectLoan_Success() {
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setLoanId(2);
        loanApplication.setStatus("PENDING");

        when(loanApplicationRepo.findById(2)).thenReturn(Optional.of(loanApplication));

        boolean result = loanApplicationService.rejectLoan(2);
        assertTrue(result);
        verify(loanApplicationRepo, times(1)).findById(2);
        verify(loanApplicationRepo, times(1)).save(loanApplication);

        boolean foundInfo = testAppender.getEvents().stream().anyMatch(e ->
                e.getRenderedMessage().contains("successfully rejected"));
        assertTrue(foundInfo, "Expected info log for loan rejection success not found.");
    }

    @Test
    void testRejectLoan_Failure() {
        when(loanApplicationRepo.findById(3)).thenReturn(Optional.empty());

        LoanRejectionException ex = assertThrows(LoanRejectionException.class, () -> {
            loanApplicationService.rejectLoan(3);
        });
        assertEquals("Failed to reject loan with ID 3", ex.getMessage());

        boolean foundError = testAppender.getEvents().stream().anyMatch(e ->
                e.getRenderedMessage().contains("Failed to reject loan with ID 3"));
        assertTrue(foundError, "Expected error log for loan rejection failure not found.");
    }
    
    @Test
     void testShowLoanapplication() {
        List<LoanApplication> loanApplications = new ArrayList<>();
        loanApplications.add(new LoanApplication());  // Add dummy loan applications

        when(loanApplicationRepo.findAll()).thenReturn(loanApplications);

        List<LoanApplication> result = loanApplicationService.showLoanapplication();
        assertEquals(loanApplications, result);

        verify(loanApplicationRepo, times(1)).findAll();
    }
    @Test
     void testSearchLoanapplicationById() {
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setLoanId(1);

        when(loanApplicationRepo.findById(1)).thenReturn(Optional.of(loanApplication));

        LoanApplication result = loanApplicationService.searchLoanapplicationById(1);
        assertEquals(loanApplication, result);

        verify(loanApplicationRepo, times(1)).findById(1);
    }

    @Test
     void testSearchLoanapplicationByIdNotFound() {
        when(loanApplicationRepo.findById(1)).thenReturn(Optional.empty());

        loanApplicationService.searchLoanapplicationById(1);  // Should throw exception

        verify(loanApplicationRepo, times(1)).findById(1);
    }
    @Test
     void testUpdateLoanapplication() {
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setLoanId(1);

        when(loanApplicationRepo.save(loanApplication)).thenReturn(loanApplication);

        loanApplicationService.updateLoanapplication(loanApplication);

        verify(loanApplicationRepo, times(1)).save(loanApplication);
        verify(logger).info("Updating loan application with ID: 1");
    }

    @Test
     void testDeleteLoanapplication() {
        int loanId = 1;

        doNothing().when(loanApplicationRepo).deleteById(loanId);

        loanApplicationService.deleteLoanapplication(loanId);

        verify(loanApplicationRepo, times(1)).deleteById(loanId);
        verify(logger).info("Deleting loan application with ID: " + loanId);
    }

    @Test
     void testAcceptLoan_ApprovalSuccess() throws EmailSendingException {
        // Mock loan application
        LoanApplication mockLoanApplication = new LoanApplication();
        mockLoanApplication.setLoanId(1);
        mockLoanApplication.setLoanAmount(100000);
        mockLoanApplication.setInterestRate(7.5);
        mockLoanApplication.setTenure(2); // 2 years
        mockLoanApplication.setAccountNumber(123456);
        mockLoanApplication.setLoanType(Constants.PERSONAL);
        when(loanApplicationRepo.findById(1)).thenReturn(Optional.of(mockLoanApplication));

        // Mock account details
        AccountDetails mockAccountDetails = new AccountDetails();
        mockAccountDetails.setAccountNumber(123456);
        mockAccountDetails.setEmail("test@example.com");
        mockAccountDetails.setFirstName("John");
        when(accountDetailsRepo.findByAccountNumber(123456)).thenReturn(mockAccountDetails);

        // Mock saving loan sanction
        LoanSanction mockLoanSanction = new LoanSanction();
        when(loanSanctionRepo.save(any(LoanSanction.class))).thenReturn(mockLoanSanction);

        // Mock sending loan approval email
        doNothing().when(emailService).sendLoanStatusEmail(anyString(), anyString(), anyString(), anyDouble(),anyBoolean());

        // Execute the method
        boolean result = loanApplicationService.acceptLoan(1);

        // Verify the method behavior
        assertTrue(result);
        verify(loanApplicationRepo).findById(1);
        verify(loanApplicationRepo).save(mockLoanApplication);
        verify(loanSanctionRepo).save(any(LoanSanction.class));
        verify(emailService).sendLoanStatusEmail(anyString(), anyString(), anyString(), anyDouble(), anyBoolean());

    }

        @Test
         void testCheckEmploymentTypeForLoan_PersonalLoanForNonSalaried() {
            // Mock LoanApplication
            LoanApplication loanApplication = mock(LoanApplication.class);
            
            // Mock the loan type and employment type
            when(loanApplication.getLoanType()).thenReturn("PERSONAL");
            when(loanApplication.getEmployType()).thenReturn("SELF_EMPLOYED");


            // Call the method
            String result = loanApplicationService.checkEmploymentTypeForLoan(loanApplication);

            // Verify the result
            assertEquals("Personal Loan can only be applied by salaried individuals.", result);
        }

        @Test
         void testCheckEmploymentTypeForLoan_BusinessLoanForNonSelfEmployed() {
            // Mock LoanApplication
            LoanApplication loanApplication = mock(LoanApplication.class);
            
            // Mock the loan type and employment type
            when(loanApplication.getLoanType()).thenReturn(Constants.BUSINESS);
            when(loanApplication.getEmployType()).thenReturn("SALARIED");

            // Call the method
            String result = loanApplicationService.checkEmploymentTypeForLoan(loanApplication);

            // Verify the result
            assertEquals("Business Loan can only be applied by self-employed individuals.", result);
        }

        @Test
         void testCheckEmploymentTypeForLoan_EducationLoanForNonStudent() {
            // Mock LoanApplication
            LoanApplication loanApplication = mock(LoanApplication.class);
            
            // Mock the loan type and employment type
            when(loanApplication.getLoanType()).thenReturn("EDUCATION");
            when(loanApplication.getEmployType()).thenReturn("SALARIED");

            // Call the method
            String result = loanApplicationService.checkEmploymentTypeForLoan(loanApplication);

            // Verify the result
            assertEquals("Educational Loan can only be applied by students.", result);
        }

        @Test
         void testCheckEmploymentTypeForLoan_ValidLoan() {
            // Mock LoanApplication
            LoanApplication loanApplication = mock(LoanApplication.class);
            
            // Mock the loan type and employment type
            when(loanApplication.getLoanType()).thenReturn("EDUCATION");
            when(loanApplication.getEmployType()).thenReturn("STUDENT");

            // Call the method
            String result = loanApplicationService.checkEmploymentTypeForLoan(loanApplication);

            // Verify the result (should be null since it's valid)
            assertNull(result);
        }

    @Test
     void testSearchLoanApplicationsByAccountNumberAndLoanType() {
        List<LoanApplication> mockLoanApplications = Arrays.asList(new LoanApplication(), new LoanApplication());
        when(loanApplicationRepo.findByAccountNumberAndLoanType(123456, Constants.PERSONAL)).thenReturn(mockLoanApplications);

        List<LoanApplication> result = loanApplicationService.searchLoanApplicationsByAccountNumberAndLoanType(123456, Constants.PERSONAL);

        assertEquals(2, result.size());
        verify(loanApplicationRepo).findByAccountNumberAndLoanType(123456, Constants.PERSONAL);
    }
    @Test
     void testSearchLoanApplicationByApprovalStatus() {
        List<LoanApplication> mockLoanApplications = Arrays.asList(new LoanApplication(), new LoanApplication());
        when(loanApplicationRepo.findByStatus("APPROVED")).thenReturn(mockLoanApplications);

        List<LoanApplication> result = loanApplicationService.searchLoanApplicationByApprovalStatus("APPROVED");

        assertEquals(2, result.size());
        verify(loanApplicationRepo).findByStatus("APPROVED");
    }

    @Test
     void testSearchLoanApplicationByAccountNumber() {
        // Given: An account number and mocked loan applications
        int accountNumber = 123456;
        LoanApplication loanApp1 = new LoanApplication();
        LoanApplication loanApp2 = new LoanApplication();
        loanApp1.setAccountNumber(accountNumber);
        loanApp2.setAccountNumber(accountNumber);
        
        // Mocking the repository's behavior
        when(loanApplicationRepo.findByAccountNumber(accountNumber))
                .thenReturn(Arrays.asList(loanApp1, loanApp2));

        // When: Calling the method
        List<LoanApplication> result = loanApplicationService.searchloanApplicationByaccountNumber(accountNumber);

        // Then: Verify the result
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(accountNumber, result.get(0).getAccountNumber());
        assertEquals(accountNumber, result.get(1).getAccountNumber());

        // Verify the interaction with the repository
        verify(loanApplicationRepo, times(1)).findByAccountNumber(accountNumber);
    }
    @Test
     void testCheckForDuplicateLoanApplication() {
        // Given: A loan application and existing approved loans
        LoanApplication newLoanApplication = new LoanApplication();
        newLoanApplication.setAccountNumber(123456);
        newLoanApplication.setLoanType("HOME_LOAN");

        LoanApplication existingLoan = new LoanApplication();
        existingLoan.setAccountNumber(123456);
        existingLoan.setLoanType("HOME_LOAN");
        existingLoan.setStatus(Constants.APPROVED);  // Approved loan

        // Mock the repository's behavior to return the existing loans
        when(loanApplicationRepo.findByAccountNumber(newLoanApplication.getAccountNumber()))
                .thenReturn(Arrays.asList(existingLoan));

        // When: Calling the method
        String result = loanApplicationService.checkForDuplicateLoanApplication(newLoanApplication);

        // Then: Verify that the correct message is returned
        assertEquals("You already have an approved HOME_LOAN. You cannot apply again.", result);

        // Verify that the repository was called correctly
        verify(loanApplicationRepo, times(1)).findByAccountNumber(newLoanApplication.getAccountNumber());
    }

    @Test
     void testCheckForDuplicateLoanApplication_NoDuplicate() {
        // Given: A loan application with no matching approved loans
        LoanApplication newLoanApplication = new LoanApplication();
        newLoanApplication.setAccountNumber(123456);
        newLoanApplication.setLoanType("CAR_LOAN");

        LoanApplication existingLoan = new LoanApplication();
        existingLoan.setAccountNumber(123456);
        existingLoan.setLoanType("HOME_LOAN");  // Different loan type
        existingLoan.setStatus(Constants.APPROVED);

        // Mock the repository to return non-matching loans
        when(loanApplicationRepo.findByAccountNumber(newLoanApplication.getAccountNumber()))
                .thenReturn(Arrays.asList(existingLoan));

        // When: Calling the method
        String result = loanApplicationService.checkForDuplicateLoanApplication(newLoanApplication);

        // Then: Verify that no error message is returned (no duplicate)
        assertNull(result);

        // Verify the repository interaction
        verify(loanApplicationRepo, times(1)).findByAccountNumber(newLoanApplication.getAccountNumber());
    }
    
    @Test
     void testLoanTypeRestriction() {
        // Given: A loan application for an Education Loan
        LoanApplication newLoanApplication = new LoanApplication();
        newLoanApplication.setAccountNumber(123456);
        newLoanApplication.setLoanType("EDUCATION");  // New loan type being applied for

        // Mock an existing Personal Loan for the same account number
        LoanApplication existingLoan = new LoanApplication();
        existingLoan.setAccountNumber(123456);
        existingLoan.setLoanType("PERSONAL");  // Existing Personal Loan
        existingLoan.setStatus(Constants.APPROVED);  // Already approved Personal Loan

        // Mock the repository to return the existing Personal Loan
        when(loanApplicationRepo.findByAccountNumber(newLoanApplication.getAccountNumber()))
                .thenReturn(Arrays.asList(existingLoan));

        // When: Calling the method that checks for loan restrictions
        String result = loanApplicationService.checkLoanTypeRestrictions(newLoanApplication);

        // Then: Verify that the correct message is returned for the restriction
        assertEquals("You cannot apply for an Education or Business Loan if you already have a Personal Loan.", result);

        // Verify that the repository was called correctly
        verify(loanApplicationRepo, times(1)).findByAccountNumber(newLoanApplication.getAccountNumber());
    }

    @Test
     void testNoLoanTypeRestriction() {
        // Given: A loan application for a Business Loan
        LoanApplication newLoanApplication = new LoanApplication();
        newLoanApplication.setAccountNumber(123456);
        newLoanApplication.setLoanType("BUSINESS");  // New loan type being applied for

        // Mock an existing loan of a different type (e.g., Home Loan)
        LoanApplication existingLoan = new LoanApplication();
        existingLoan.setAccountNumber(123456);
        existingLoan.setLoanType("HOME_LOAN");  // Different existing loan
        existingLoan.setStatus(Constants.APPROVED);

        // Mock the repository to return the existing Home Loan
        when(loanApplicationRepo.findByAccountNumber(newLoanApplication.getAccountNumber()))
                .thenReturn(Arrays.asList(existingLoan));

        // When: Calling the method that checks for loan restrictions
        String result = loanApplicationService.checkLoanTypeRestrictions(newLoanApplication);

        // Then: Verify that no restriction is applied and null is returned
        assertNull(result);

        // Verify that the repository was called correctly
        verify(loanApplicationRepo, times(1)).findByAccountNumber(newLoanApplication.getAccountNumber());
    }
    
    @Test
     void testLoanTypeRestriction_BusinessLoanPreventsPersonalLoan() {
        // Given: A loan application for a Personal Loan
        LoanApplication newLoanApplication = new LoanApplication();
        newLoanApplication.setAccountNumber(123456);
        newLoanApplication.setLoanType("PERSONAL");  // New loan type being applied for

        // Mock an existing Business Loan for the same account number
        LoanApplication existingLoan = new LoanApplication();
        existingLoan.setAccountNumber(123456);
        existingLoan.setLoanType("BUSINESS");  // Existing Business Loan
        existingLoan.setStatus(Constants.APPROVED);  // Already approved Business Loan

        // Mock the repository to return the existing Business Loan
        when(loanApplicationRepo.findByAccountNumber(newLoanApplication.getAccountNumber()))
                .thenReturn(Arrays.asList(existingLoan));

        // When: Calling the method that checks for loan restrictions
        String result = loanApplicationService.checkLoanTypeRestrictions(newLoanApplication);

        // Then: Verify that the correct message is returned for the restriction
        assertEquals("You cannot apply for a Personal Loan if you already have a Business Loan.", result);

        // Verify that the repository was called correctly
        verify(loanApplicationRepo, times(1)).findByAccountNumber(newLoanApplication.getAccountNumber());
    }

    @Test
     void testNoLoanTypeRestriction_BusinessLoanAllowsOtherLoanTypes() {
        // Given: A loan application for a Car Loan (other loan types should not trigger the restriction)
        LoanApplication newLoanApplication = new LoanApplication();
        newLoanApplication.setAccountNumber(123456);
        newLoanApplication.setLoanType("CAR_LOAN");  // New loan type being applied for

        // Mock an existing Business Loan for the same account number
        LoanApplication existingLoan = new LoanApplication();
        existingLoan.setAccountNumber(123456);
        existingLoan.setLoanType("BUSINESS");  // Existing Business Loan
        existingLoan.setStatus(Constants.APPROVED);  // Already approved Business Loan

        // Mock the repository to return the existing Business Loan
        when(loanApplicationRepo.findByAccountNumber(newLoanApplication.getAccountNumber()))
                .thenReturn(Arrays.asList(existingLoan));

        // When: Calling the method that checks for loan restrictions
        String result = loanApplicationService.checkLoanTypeRestrictions(newLoanApplication);

        // Then: Verify that no restriction is applied and null is returned
        assertNull(result);

        // Verify that the repository was called correctly
        verify(loanApplicationRepo, times(1)).findByAccountNumber(newLoanApplication.getAccountNumber());
    }
  
    @Test
     void testNoLoanTypeRestriction_EducationLoanAllowedWithNoPersonalOrBusinessLoan() {
        // Given: A loan application for an Education Loan with no existing Personal or Business Loans
        LoanApplication newLoanApplication = new LoanApplication();
        newLoanApplication.setAccountNumber(123456);
        newLoanApplication.setLoanType("EDUCATION");  // New loan type being applied for

        // Mock the repository to return no existing Personal or Business Loans
        when(loanApplicationRepo.findByAccountNumber(newLoanApplication.getAccountNumber()))
                .thenReturn(Arrays.asList());

        // When: Calling the method that checks for loan type restrictions
        String result = loanApplicationService.checkLoanTypeRestrictions(newLoanApplication);

        // Then: Verify that no restriction is applied and null is returned
        assertNull(result);

        // Verify that the repository was called correctly
        verify(loanApplicationRepo, times(1)).findByAccountNumber(newLoanApplication.getAccountNumber());
    }
    @Test
     void testEducationLoanBlockedByPersonalAndBusinessLoans() {
        // Given: An Education Loan application
        LoanApplication educationLoanApplication = new LoanApplication();
        educationLoanApplication.setAccountNumber(10001);
        educationLoanApplication.setLoanType("EDUCATION");

        // Mock an existing approved Personal Loan
        LoanApplication personalLoan = new LoanApplication();
        personalLoan.setAccountNumber(10001);
        personalLoan.setLoanType("PERSONAL");
        personalLoan.setStatus(Constants.APPROVED);

        // Mock an existing approved Business Loan
        LoanApplication businessLoan = new LoanApplication();
        businessLoan.setAccountNumber(10001);
        businessLoan.setLoanType("BUSINESS");
        businessLoan.setStatus(Constants.APPROVED);

        // Mock the repository to return both approved Personal and Business Loans
        List<LoanApplication> existingLoans = Arrays.asList(personalLoan, businessLoan);
        when(loanApplicationRepo.findByAccountNumber(educationLoanApplication.getAccountNumber()))
                .thenReturn(existingLoans);

        // When: Calling the service method to check for loan type restriction
        String result = loanApplicationService.checkLoanTypeRestrictions(educationLoanApplication);

        // Then: Verify that the loan type restriction is triggered correctly
        assertEquals("You cannot apply for an Education Loan if you already have both a Personal and Business Loan approved.", result);

        // Verify that the repository method was called once with the correct account number
        verify(loanApplicationRepo, times(1)).findByAccountNumber(educationLoanApplication.getAccountNumber());
    }
}
