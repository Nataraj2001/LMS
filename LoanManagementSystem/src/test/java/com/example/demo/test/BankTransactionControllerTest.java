package com.example.demo.test;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.demo.controller.BankTransactionController;
import com.example.demo.exception.AccountNotFoundException;
import com.example.demo.exception.InsufficientBalanceException;
import com.example.demo.model.AccountDetails;
import com.example.demo.model.BankTransaction;
import com.example.demo.repo.AccountDetailsRepo;
import com.example.demo.service.BankTransactionService;

 class BankTransactionControllerTest {

    @Mock
    private BankTransactionService bankTransactionService;

    @Mock
    private AccountDetailsRepo accountDetailsRepo;

    @InjectMocks
    private BankTransactionController bankTransactionController;

    @BeforeEach
     void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // Test case for showTransactionInfo
    @Test
     void testShowTransactionInfo() {
        List<BankTransaction> transactions = Arrays.asList(new BankTransaction(), new BankTransaction());
        when(bankTransactionService.showTransactionInfo()).thenReturn(transactions);

        List<BankTransaction> result = bankTransactionController.showTransactionInfo();

        assert result.size() == 2;
        verify(bankTransactionService).showTransactionInfo();
    }

    // Test case for searchByTransactionId - success scenario
    @Test
     void testSearchByTransactionId_Success() {
        BankTransaction transaction = new BankTransaction();
        when(bankTransactionService.searchByTransactionId(anyInt())).thenReturn(transaction);

        ResponseEntity<BankTransaction> response = bankTransactionController.searchByTransactionId(1);

        assert response.getStatusCode() == HttpStatus.OK;
        verify(bankTransactionService).searchByTransactionId(1);
    }

    // Test case for searchByTransactionId - failure scenario
    @Test
     void testSearchByTransactionId_Failure() {
        when(bankTransactionService.searchByTransactionId(anyInt())).thenThrow(new NoSuchElementException());

        ResponseEntity<BankTransaction> response = bankTransactionController.searchByTransactionId(1);

        assert response.getStatusCode() == HttpStatus.NOT_FOUND;
        verify(bankTransactionService).searchByTransactionId(1);
    }

    // Test case for searchByAccountNumber - success scenario
    @Test
     void testSearchByAccountNumber_Success() throws AccountNotFoundException {
        List<BankTransaction> transactions = Arrays.asList(new BankTransaction(), new BankTransaction());
        when(bankTransactionService.searchByAccountNumber(anyInt())).thenReturn(transactions);

        ResponseEntity<List<BankTransaction>> response = bankTransactionController.searchByAccountNumber(123);

        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody().size() == 2;
        verify(bankTransactionService).searchByAccountNumber(123);
    }

    // Test case for searchByAccountNumber - failure scenario
    @Test
     void testSearchByAccountNumber_Failure() throws AccountNotFoundException {
        when(bankTransactionService.searchByAccountNumber(anyInt())).thenThrow(new AccountNotFoundException("Account not found"));

        ResponseEntity<List<BankTransaction>> response = bankTransactionController.searchByAccountNumber(123);

        assert response.getStatusCode() == HttpStatus.NOT_FOUND;
        verify(bankTransactionService).searchByAccountNumber(123);
    }

    // Test case for transferFunds - success scenario
    @Test
     void testTransferFunds_Success() throws InsufficientBalanceException, AccountNotFoundException {
        doNothing().when(bankTransactionService).transferFunds(anyInt(), anyInt(), anyDouble());

        ResponseEntity<String> response = bankTransactionController.transferFunds(123, 456, 1000.0);

        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody().equals("Transfer successful");
        verify(bankTransactionService).transferFunds(123, 456, 1000.0);
    }

    // Test case for transferFunds - InsufficientBalanceException
    @Test
     void testTransferFunds_InsufficientBalance() throws InsufficientBalanceException, AccountNotFoundException {
        doThrow(new InsufficientBalanceException("Insufficient balance")).when(bankTransactionService).transferFunds(anyInt(), anyInt(), anyDouble());

        ResponseEntity<String> response = bankTransactionController.transferFunds(123, 456, 1000.0);

        assert response.getStatusCode() == HttpStatus.BAD_REQUEST;
        assert response.getBody().equals("Insufficient balance");
        verify(bankTransactionService).transferFunds(123, 456, 1000.0);
    }

    // Test case for transferFunds - AccountNotFoundException
    @Test
     void testTransferFunds_AccountNotFound() throws InsufficientBalanceException, AccountNotFoundException {
        doThrow(new AccountNotFoundException("Account not found")).when(bankTransactionService).transferFunds(anyInt(), anyInt(), anyDouble());

        ResponseEntity<String> response = bankTransactionController.transferFunds(123, 456, 1000.0);

        assert response.getStatusCode() == HttpStatus.NOT_FOUND;
        assert response.getBody().equals("Account not found");
        verify(bankTransactionService).transferFunds(123, 456, 1000.0);
    }

    // Test case for manualPayments - success scenario
    @Test
     void testProcessPayment_Success() {
        AccountDetails account = new AccountDetails();
        account.setBalance(2000.0);
        when(accountDetailsRepo.findByAccountNumber(anyInt())).thenReturn(account);

        ResponseEntity<String> response = bankTransactionController.processPayment(123, 1000.0);

        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody().equals("Payment processed successfully");
        verify(accountDetailsRepo).save(account);
    }

    // Test case for manualPayments - insufficient balance
    @Test
    void testProcessPayment_InsufficientBalance() {
        // Simulate account with insufficient balance
        AccountDetails account = new AccountDetails();
        account.setBalance(500.0); // Less than the payment amount
        when(accountDetailsRepo.findByAccountNumber(anyInt())).thenReturn(account);

        // Invoke the method being tested
        ResponseEntity<String> response = bankTransactionController.processPayment(123, 1000.0);

        // Assertions
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Expected BAD_REQUEST status");
        assertEquals("Insufficient balance", response.getBody(), "Expected 'Insufficient balance' message");
    } 
    // Test case for manualPayments - account not found
    @Test
    void testProcessPayment_AccountNotFound() {
        // Simulate account not found
        when(accountDetailsRepo.findByAccountNumber(anyInt())).thenReturn(null);

        // Invoke the method being tested
        ResponseEntity<String> response = bankTransactionController.processPayment(123, 1000.0);

        // Assertions
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Expected BAD_REQUEST status");
        assertEquals("Account not found", response.getBody(), "Expected 'Account not found' message");
    }
    @Test
     void testProcessPaymentExceptionDuringSave() {
        // Arrange
        int accountNumber = 12345;
        double paymentAmount = 500.0;

        AccountDetails accountDetails = new AccountDetails();
        accountDetails.setAccountNumber(accountNumber);
        accountDetails.setBalance(1000.0);

        // Mock fetching account details
        when(accountDetailsRepo.findByAccountNumber(accountNumber)).thenReturn(accountDetails);

        // Mock exception during save
        doThrow(new RuntimeException("Database error")).when(accountDetailsRepo).save(any(AccountDetails.class));

        // Act
        ResponseEntity<String> response = bankTransactionController.processPayment(accountNumber, paymentAmount);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error saving account: Database error", response.getBody());
    }
}

