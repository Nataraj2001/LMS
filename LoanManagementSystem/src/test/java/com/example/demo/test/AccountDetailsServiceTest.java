package com.example.demo.test;

import com.example.demo.exception.EmailSendingException;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.AccountDetails;
import com.example.demo.repo.AccountDetailsRepo;
import com.example.demo.service.AccountDetailsService;
import com.example.demo.service.EmailService;
import org.slf4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

 class AccountDetailsServiceTest {

    private static final int ACCOUNT_NUMBER = 12345;
    private static final String FIRST_NAME = "John";
    private static final String EMAIL = "test@example.com";

    @Mock
    private AccountDetailsRepo accountdetailsRepo;

    @Mock
    private EmailService emailService;
    @Mock
    private Logger logger;  

    private AccountDetailsService accountDetailsService;

    @BeforeEach
     void setUp() {
        MockitoAnnotations.openMocks(this);
        accountDetailsService = new AccountDetailsService(accountdetailsRepo, emailService);
    }

  

    // Test for addAccountDetails method
    @Test
    void shouldAddAccountDetailsSuccessfully() throws EmailSendingException {
        AccountDetails accountDetails = createAccountDetails();

        // Mock the repo's save method
        when(accountdetailsRepo.save(any(AccountDetails.class))).thenReturn(accountDetails);

        // Call the service method
        accountDetailsService.addAccountDetails(accountDetails);

        // Verify that save was called once
        verify(accountdetailsRepo, times(1)).save(accountDetails);

        // Verify that emailService.sendAccountCreationEmail was called once
        verify(emailService, times(1)).sendAccountCreationEmail(
                accountDetails.getEmail(),
                accountDetails.getFirstName(),
                accountDetails.getAccountNumber()
        );

    }

    @Test
    void shouldLogErrorWhenEmailSendingFails() throws EmailSendingException {
        AccountDetails accountDetails = createAccountDetails();

        // Mock the repo's save method
        when(accountdetailsRepo.save(any(AccountDetails.class))).thenReturn(accountDetails);

        // Simulate an exception when sending the email
        doThrow(new EmailSendingException("Email sending failed", null))
            .when(emailService).sendAccountCreationEmail(anyString(), anyString(), anyInt());

        // Call the service method and assert that the email sending exception is logged
        assertThrows(EmailSendingException.class, () -> {
            accountDetailsService.addAccountDetails(accountDetails);
        });

        // Verify that the email service was called
        verify(emailService).sendAccountCreationEmail(anyString(), anyString(), anyInt());
    }

    private AccountDetails createAccountDetails() {
        AccountDetails accountDetails = new AccountDetails();
        accountDetails.setAccountNumber(12345);
        accountDetails.setEmail("test@example.com");
        accountDetails.setFirstName("John");
        return accountDetails;
    }
    // Test for searchAccountDetailsById method (Account found)
    @Test
     void shouldReturnAccountDetailsWhenFoundById() {
        AccountDetails accountDetails = createAccountDetails();
        when(accountdetailsRepo.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(accountDetails));

        Optional<AccountDetails> result = accountDetailsService.searchAccountDetailsById(ACCOUNT_NUMBER);
        
        assertTrue(result.isPresent());
        assertEquals(ACCOUNT_NUMBER, result.get().getAccountNumber());
    }

    // Test for searchAccountDetailsById method (Account not found)
    @Test
     void shouldThrowExceptionWhenAccountNotFoundById() {
        when(accountdetailsRepo.findById(ACCOUNT_NUMBER)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                accountDetailsService.searchAccountDetailsById(ACCOUNT_NUMBER)
        );

        assertEquals(" The account with AccountNumber was not found." + ACCOUNT_NUMBER, exception.getMessage());
    }

    // Test for updateAccountStatus method (Account exists)
    @Test
     void shouldUpdateAccountStatusSuccessfully() {
        AccountDetails accountDetails = createAccountDetails();
        accountDetails.setAccountStatus("Active");
        when(accountdetailsRepo.findByAccountNumber(ACCOUNT_NUMBER)).thenReturn(accountDetails);

        accountDetailsService.updateAccountStatus(ACCOUNT_NUMBER, "Closed", new Date());

        verify(accountdetailsRepo, times(1)).save(accountDetails);
        assertEquals("Closed", accountDetails.getAccountStatus());
    }

    // Test for updateAccountStatus method (Account not found)
    @Test
    void shouldThrowExceptionWhenAccountNotFoundForStatusUpdate() {
        // Mock the repo's behavior to return null for the account number
        when(accountdetailsRepo.findByAccountNumber(ACCOUNT_NUMBER)).thenReturn(null);

        // Call the method outside the lambda and store the exception
        ResourceNotFoundException exception = null;
        try {
            accountDetailsService.updateAccountStatus(ACCOUNT_NUMBER, "Closed", new Date());
        } catch (ResourceNotFoundException e) {
            exception = e;
        }

        // Assert that the exception is not null
        assertNotNull(exception, "Expected ResourceNotFoundException to be thrown.");

        // Assert that the exception message is correct
        String expectedMessage = "Account with the specified AccountNumber does not exist. " + ACCOUNT_NUMBER;
        assertTrue(exception.getMessage().contains(expectedMessage), "Exception message should contain: " + expectedMessage);
    }


    // Test for showAccountDetails method
    @Test
     void shouldReturnAllAccountDetails() {
        AccountDetails account1 = createAccountDetails();
        account1.setAccountNumber(12345);
        account1.setEmail("test1@example.com");

        AccountDetails account2 = createAccountDetails();
        account2.setAccountNumber(67890);
        account2.setEmail("test2@example.com");

        List<AccountDetails> mockAccountDetailsList = Arrays.asList(account1, account2);

        // Mock the repository's findAll method to return the mock list
        when(accountdetailsRepo.findAll()).thenReturn(mockAccountDetailsList);

        // Call the service method
        List<AccountDetails> result = accountDetailsService.showAccountDetails();

        // Verify that findAll was called once
        verify(accountdetailsRepo, times(1)).findAll();

        // Verify the result
        assertNotNull(result, "Account details list should not be null.");
        assertEquals(2, result.size(), "Account details list should have 2 items.");
        assertEquals(account1, result.get(0), "First account details should match.");
        assertEquals(account2, result.get(1), "Second account details should match.");
    }

    // Test for searchAccountDetailsByFirstName method (Account not found)
    @Test
    void shouldThrowExceptionWhenAccountNotFoundByFirstName() {
        // Mocking the repository method to return null when searching for the account
        when(accountdetailsRepo.findByFirstName(FIRST_NAME)).thenReturn(null);

        // Asserting that the ResourceNotFoundException is thrown
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                accountDetailsService.searchAccountDetailsByFirstName(FIRST_NAME)
        );

        // Verifying the exception message
        assertEquals("Account not found with FirstName: " + FIRST_NAME, exception.getMessage());
    }

    @Test
    void shouldReturnAccountDetailsWhenAccountFoundByFirstName() {
        // Create mock account details
        AccountDetails mockAccountDetails = new AccountDetails();
        mockAccountDetails.setFirstName(FIRST_NAME);

        // Mocking the repository method to return a valid account when searched by first name
        when(accountdetailsRepo.findByFirstName(FIRST_NAME)).thenReturn(mockAccountDetails);

        // Calling the service method to fetch the account details
        AccountDetails result = accountDetailsService.searchAccountDetailsByFirstName(FIRST_NAME);

        // Verifying that the account details are returned correctly
        assertNotNull(result);
        assertEquals(FIRST_NAME, result.getFirstName());

        // Verifying that the repository method was called once
        verify(accountdetailsRepo, times(1)).findByFirstName(FIRST_NAME);
    }


    // Test for searchAccountDetailsByEmail method (Account found)
    @Test
     void shouldReturnAccountDetailsWhenFoundByEmail() {
        AccountDetails accountDetails = createAccountDetails();
        when(accountdetailsRepo.findByEmail(EMAIL)).thenReturn(accountDetails);

        AccountDetails result = accountDetailsService.searchAccountDetailsByEmail(EMAIL);

        assertNotNull(result);
        assertEquals(EMAIL, result.getEmail());
    }

    // Test for searchAccountDetailsByEmail method (Account not found)
    @Test
     void shouldReturnNullWhenAccountNotFoundByEmail() {
        when(accountdetailsRepo.findByEmail(EMAIL)).thenReturn(null);

        AccountDetails result = accountDetailsService.searchAccountDetailsByEmail(EMAIL);

        assertNull(result);
    }

    // Test for existsByEmailOrMobileNo method (Account exists)
    @Test
     void shouldReturnTrueWhenAccountExistsByEmailOrMobileNo() {
        String mobileNo = "1234567890";
        when(accountdetailsRepo.existsByEmailOrMobileNo(EMAIL, mobileNo)).thenReturn(true);

        boolean result = accountDetailsService.existsByEmailOrMobileNo(EMAIL, mobileNo);

        assertTrue(result);
    }

    // Test for existsByEmailOrMobileNo method (Account does not exist)
    @Test
     void shouldReturnFalseWhenAccountDoesNotExistByEmailOrMobileNo() {
        String mobileNo = "1234567890";
        when(accountdetailsRepo.existsByEmailOrMobileNo(EMAIL, mobileNo)).thenReturn(false);

        boolean result = accountDetailsService.existsByEmailOrMobileNo(EMAIL, mobileNo);

        assertFalse(result);
    }
}
