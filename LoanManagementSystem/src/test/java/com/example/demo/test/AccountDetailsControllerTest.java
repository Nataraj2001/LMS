package com.example.demo.test;


import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyInt;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.demo.controller.AccountDetailsController;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.AccountDetails;
import com.example.demo.service.AccountDetailsService;

@ExtendWith(MockitoExtension.class)
 class AccountDetailsControllerTest {

    @Mock
    private AccountDetailsService accountDetailsService;

    @InjectMocks
    private AccountDetailsController accountDetailsController;

    private AccountDetails accountDetails;

    @BeforeEach
     void setUp() {
        accountDetails = new AccountDetails();
        accountDetails.setAccountNumber(123);
        accountDetails.setFirstName("John");
        accountDetails.setEmail("john.doe@example.com");
        accountDetails.setAccountStatus("active");
    }

    @Test
     void testShowAccountDetails() {
        when(accountDetailsService.showAccountDetails()).thenReturn(Arrays.asList(accountDetails));

        // Call the controller method
        var accountList = accountDetailsController.showAccountDetails();

        // Verify that the mock service was called
        verify(accountDetailsService).showAccountDetails();

        // Assertions
        assert accountList.size() == 1;
        assert accountList.get(0).getFirstName().equals("John");
    }

    @Test
     void testSearchAccountDetailsById() {
        when(accountDetailsService.searchAccountDetailsById(anyInt())).thenReturn(Optional.of(accountDetails));

        ResponseEntity<AccountDetails> response = accountDetailsController.searchAccountDetailsById(123);

        verify(accountDetailsService).searchAccountDetailsById(123);
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody().getAccountNumber() == 123;
    }

    @Test
     void testSearchAccountDetailsByIdNotFound() {
        when(accountDetailsService.searchAccountDetailsById(anyInt())).thenReturn(Optional.empty());

        try {
            accountDetailsController.searchAccountDetailsById(999);
        } catch (ResourceNotFoundException e) {
            assert e.getMessage().contains("Account not found with AccountNumber: 999");
        }

        verify(accountDetailsService).searchAccountDetailsById(999);
    }

    @Test
     void testAddAccountDetails() {
        doNothing().when(accountDetailsService).addAccountDetails(any(AccountDetails.class));

        accountDetailsController.addEmploy(accountDetails);

        verify(accountDetailsService).addAccountDetails(accountDetails);
    }

    @Test
     void testUpdateAccountDetails() {
        doNothing().when(accountDetailsService).updateAccountStatus(anyInt(), any(String.class), any());

        ResponseEntity<String> response = accountDetailsController.updateAccountDetails(123, accountDetails);

        verify(accountDetailsService).updateAccountStatus(eq(123), eq("active"), any());
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody().equals("Account successfully deactivated.");
    }

    @Test
     void testSearchAccountDetailsByFirstName() {
        when(accountDetailsService.searchAccountDetailsByFirstName(any(String.class))).thenReturn(accountDetails);

        ResponseEntity<AccountDetails> response = accountDetailsController.searchAccountDetailsByFirstname("John");

        verify(accountDetailsService).searchAccountDetailsByFirstName("John");
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody().getFirstName().equals("John");
    }

    @Test
     void testSearchAccountDetailsByFirstNameNotFound() {
        when(accountDetailsService.searchAccountDetailsByFirstName(any(String.class))).thenThrow(new ResourceNotFoundException("Account not found with FirstName: John"));

        try {
            accountDetailsController.searchAccountDetailsByFirstname("John");
        } catch (ResourceNotFoundException e) {
            assert e.getMessage().contains("Account not found with FirstName: John");
        }

        verify(accountDetailsService).searchAccountDetailsByFirstName("John");
    }

    @Test
     void testSearchAccountDetailsByEmail() {
        when(accountDetailsService.searchAccountDetailsByEmail(any(String.class))).thenReturn(accountDetails);

        ResponseEntity<AccountDetails> response = accountDetailsController.searchAccountDetailsByEmail("john.doe@example.com");

        verify(accountDetailsService).searchAccountDetailsByEmail("john.doe@example.com");
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody().getEmail().equals("john.doe@example.com");
    }

    @Test
     void testSearchAccountDetailsByEmailNotFound() {
        when(accountDetailsService.searchAccountDetailsByEmail(any(String.class))).thenThrow(new NoSuchElementException());

        ResponseEntity<AccountDetails> response = accountDetailsController.searchAccountDetailsByEmail("unknown@example.com");

        verify(accountDetailsService).searchAccountDetailsByEmail("unknown@example.com");
        assert response.getStatusCode() == HttpStatus.NOT_FOUND;
    }
    
    @Test
     void testUpdateAccountDetails_Exception() {
        // Simulate an exception thrown by the service
        doThrow(new RuntimeException("Service error")).when(accountDetailsService)
                .updateAccountStatus(anyInt(), any(String.class), any());

        // Call the controller method, expecting an error response
        ResponseEntity<String> response = accountDetailsController.updateAccountDetails(123, accountDetails);

        // Verify the service call
        verify(accountDetailsService).updateAccountStatus(eq(123), eq("active"), any());

        // Assertions for error response
        assert response.getStatusCode() == HttpStatus.BAD_REQUEST;
        assert response.getBody().equals("Error deactivating account.");
    }

}

