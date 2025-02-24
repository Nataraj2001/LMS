package com.example.demo.test;

import static org.mockito.Mockito.*;


import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.exception.AccountNotFoundException;
import com.example.demo.exception.InsufficientBalanceException;
import com.example.demo.model.AccountDetails;
import com.example.demo.model.BankTransaction;
import com.example.demo.model.Constants;
import com.example.demo.repo.AccountDetailsRepo;
import com.example.demo.repo.BankTransactionRepo;
import com.example.demo.service.AccountDetailsService;
import com.example.demo.service.BankTransactionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

 class BankTransactionServiceTest {

    @Mock
    private BankTransactionRepo bankTransactionRepo;

    @Mock
    private AccountDetailsRepo accountDetailsRepo;
    
    @Mock
    private AccountDetailsService accountDetailsService;

    @InjectMocks
    private BankTransactionService bankTransactionService;



    private AccountDetails fromAccount;
    private AccountDetails toAccount;

    @BeforeEach
     void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup mock accounts with all fields
        fromAccount = new AccountDetails(
            1,                          // accountNumber
            "Saving",                   // accountType
            "John",                     // firstName
            "Doe",                      // lastName
            "john.doe@example.com",     // email
            "1234567890",               // mobileNo
            "123 Street, City",         // address
            new Date(1990, 1, 1),       // dateOfBirth (Note: Date constructor is deprecated; use Calendar or LocalDate for new code)
            "Male",                     // gender
            "Jane Doe",                 // nomineeName
            "Active",                   // accountStatus
            1000.0,                     // balance
            new Date(),                 // accountCreationDate
            null                        // accountClosedDate (assuming account is active)
        );

        toAccount = new AccountDetails(
            2,                          // accountNumber
            "Checking",                 // accountType
            "Jane",                     // firstName
            "Doe",                      // lastName
            "jane.doe@example.com",     // email
            "0987654321",               // mobileNo
            "456 Avenue, City",         // address
            new Date(1992, 5, 15),      // dateOfBirth
            "Female",                   // gender
            "John Doe",                 // nomineeName
            "Active",                   // accountStatus
            500.0,                      // balance
            new Date(),                 // accountCreationDate
            null                        // accountClosedDate
        );
    }

    @Test
     void testShowTransactionInfo() {
        // Arrange
        List<BankTransaction> transactions = new ArrayList<>();
        transactions.add(new BankTransaction(1, 1, 2, "DEBIT", 200, new Date(), 800, Constants.SUCCESS));
        transactions.add(new BankTransaction(2, 2, 1, "CREDIT", 200, new Date(), 700, Constants.SUCCESS));
        when(bankTransactionRepo.findAll()).thenReturn(transactions);

        // Act
        List<BankTransaction> result = bankTransactionService.showTransactionInfo();

        // Assert
        assertEquals(2, result.size());
        verify(bankTransactionRepo, times(1)).findAll();
    }

    @Test
     void testSearchByTransactionId_Found() {
        // Arrange
        BankTransaction transaction = new BankTransaction(1, 1, 2, "DEBIT", 200, new Date(), 800, Constants.SUCCESS);
        when(bankTransactionRepo.findById(1)).thenReturn(Optional.of(transaction));

        // Act
        BankTransaction result = bankTransactionService.searchByTransactionId(1);

        // Assert
        assertEquals(1, result.getTransactionId());
        verify(bankTransactionRepo, times(1)).findById(1);
    }

    @Test
     void testSearchByTransactionId_NotFound() {
        // Arrange
        when(bankTransactionRepo.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bankTransactionService.searchByTransactionId(999);
        });
        assertEquals("Transaction not found", exception.getMessage());
    }

    @Test
     void testSearchByAccountNumber() {
        // Arrange
        List<BankTransaction> transactions = new ArrayList<>();
        transactions.add(new BankTransaction(1, 1, 2, "DEBIT", 200, new Date(), 800, Constants.SUCCESS));
        when(bankTransactionRepo.findByAccountNumber(1)).thenReturn(transactions);

        // Act
        List<BankTransaction> result = bankTransactionService.searchByAccountNumber(1);

        // Assert
        assertEquals(1, result.size());
        verify(bankTransactionRepo, times(1)).findByAccountNumber(1);
    }

    @Test
     void testManualPayment_AccountNotFound() {
        // Arrange
        double paymentAmount = 100.0;
        
        // Mock the behavior of accountDetailsRepo.findByAccountNumber() to return null
        when(accountDetailsRepo.findByAccountNumber(3)).thenReturn(null);
        
        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            bankTransactionService.manualPayment(3, paymentAmount);
        });
        
        // Verify the exception message
        assertEquals("Account not found", exception.getMessage());
        
        // Verify that accountDetailsRepo.findByAccountNumber() was called once
        verify(accountDetailsRepo, times(1)).findByAccountNumber(3);
    }

    @Test
     void testSearchByAccountNumber_NotFound() {
        // Arrange
        when(bankTransactionRepo.findByAccountNumber(999)).thenReturn(Collections.emptyList());

        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            bankTransactionService.searchByAccountNumber(999);
        });
        assertEquals("No transactions found for account number: 999", exception.getMessage());
    }

    @Test
     void testTransferFunds_InsufficientBalance() {
        // Arrange
        when(accountDetailsRepo.findById(1)).thenReturn(Optional.of(fromAccount));
        when(accountDetailsRepo.findById(2)).thenReturn(Optional.of(toAccount));

        // Act & Assert
        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class, () -> {
            bankTransactionService.transferFunds(1, 2, 1500);
        });
        assertEquals("Insufficient balance in account 1", exception.getMessage());
    }

    @Test
     void testTransferFunds_Success() {
        // Arrange
        when(accountDetailsRepo.findById(1)).thenReturn(Optional.of(fromAccount));
        when(accountDetailsRepo.findById(2)).thenReturn(Optional.of(toAccount));

        // Act
        bankTransactionService.transferFunds(1, 2, 500);

        // Assert
        assertEquals(500.0, fromAccount.getBalance());
        assertEquals(1000.0, toAccount.getBalance());
        verify(bankTransactionRepo, times(2)).save(any(BankTransaction.class));
    }

    @Test
     void testManualPayment_InsufficientBalance() {
        // Arrange
        when(accountDetailsRepo.findByAccountNumber(1)).thenReturn(fromAccount);

        // Act & Assert
        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class, () -> {
            bankTransactionService.manualPayment(1, 1500);
        });
        assertEquals("Insufficient balance", exception.getMessage());
    }

    @Test
     void testManualPayment_Success() {
        // Arrange
        when(accountDetailsRepo.findByAccountNumber(1)).thenReturn(fromAccount);

        // Act
        bankTransactionService.manualPayment(1, 500);

        // Assert
        assertEquals(500.0, fromAccount.getBalance());
        verify(bankTransactionRepo, times(1)).save(any(BankTransaction.class));
    }

    @Test
     void testCreditLoanAmount() {
        // Arrange
        when(accountDetailsRepo.findById(1)).thenReturn(Optional.of(fromAccount));

        // Act
        bankTransactionService.creditLoanAmount(1, 2000);

        // Assert
        assertEquals(3000.0, fromAccount.getBalance());
        verify(bankTransactionRepo, times(1)).save(any(BankTransaction.class));
    }

    @Test
     void testTransferFunds_FromAccountNotFound() {
        // Arrange
        when(accountDetailsRepo.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            bankTransactionService.transferFunds(1, 2, 500);
        });
        assertEquals("From account not found", exception.getMessage());
    }

    @Test
    void testTransferFunds_ToAccountNotFound() {
        // Arrange
        when(accountDetailsRepo.findById(2)).thenReturn(Optional.empty());

        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            bankTransactionService.transferFunds(1, 2, 500);
        });
        assertEquals("To account not found", exception.getMessage());
    }
    @Test
    void testFindToAccount_AccountNotFound() {
        // Arrange: Set up the mock behavior where the account is not found
        when(accountDetailsRepo.findById(2)).thenThrow(new AccountNotFoundException("To account not found"));

        // Act & Assert: Verify that AccountNotFoundException is thrown with appropriate message
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> 
            accountDetailsRepo.findById(2)
        );
        
        // Verify exception message
        assertEquals("To account not found", exception.getMessage());

        // Verify that accountDetailsRepo.findById() was called once
        verify(accountDetailsRepo, times(1)).findById(2);
    }



    
    @Test
    void testFindToAccount_AccountNotFound_RuntimeException() {
        // Arrange: Set up the mock behavior where the account is not found
        when(accountDetailsRepo.findById(2)).thenReturn(Optional.empty());

        // Act & Assert: Verify that RuntimeException is thrown by the method under test
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountDetailsService.searchAccountDetailsById(2);  // Simulate the service method throwing the exception
        });

        // Verify exception message
        assertEquals("Account not found", exception.getMessage());

    
    }



    @Test
    void testTransferFunds_AccountNotFound() {
        int fromAccountNumber = 123;
        int toAccountNumber = 456;
        double amount = 1000.0;

        // Mock AccountDetailsRepo to return empty for both accounts to trigger AccountNotFoundException
        when(accountDetailsRepo.findById(fromAccountNumber)).thenReturn(Optional.empty());
        when(accountDetailsRepo.findById(toAccountNumber)).thenReturn(Optional.empty());

        // Expect AccountNotFoundException to be thrown when calling transferFunds
        assertThrows(AccountNotFoundException.class, () -> {
            bankTransactionService.transferFunds(fromAccountNumber, toAccountNumber, amount);
        });
    }

    @Test
    void testCreditLoanAmount_AccountNotFound() {
        int accountNumber = 123;
        double loanAmount = 5000.0;

        // Mock AccountDetailsRepo to return empty for the account to trigger RuntimeException
        when(accountDetailsRepo.findById(accountNumber)).thenReturn(Optional.empty());

        // Expect RuntimeException to be thrown when calling creditLoanAmount
        assertThrows(RuntimeException.class, () -> {
            bankTransactionService.creditLoanAmount(accountNumber, loanAmount);
        });
    }
    @Test
    void testTransferFunds_AccountNotFound_ToAccount() {
        int fromAccountNumber = 123;
        int toAccountNumber = 456;
        double amount = 1000.0;

        // Mock the behavior where the source account exists, but destination account is not found
        AccountDetails sourceAccount = new AccountDetails();  // Renamed from 'fromAccount' to 'sourceAccount'
        sourceAccount.setBalance(5000.0);  // Set an arbitrary balance for the source account

        when(accountDetailsRepo.findById(fromAccountNumber)).thenReturn(Optional.of(sourceAccount));
        when(accountDetailsRepo.findById(toAccountNumber)).thenReturn(Optional.empty());

        // Expect AccountNotFoundException to be thrown for the destination account
        assertThrows(AccountNotFoundException.class, () -> {
            bankTransactionService.transferFunds(fromAccountNumber, toAccountNumber, amount);
        });
    }


} 


 


