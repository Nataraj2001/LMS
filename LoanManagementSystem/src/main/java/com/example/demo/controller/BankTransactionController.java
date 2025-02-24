package com.example.demo.controller;

 
import java.util.List;
import java.util.NoSuchElementException;
 

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.exception.AccountNotFoundException;
import com.example.demo.exception.InsufficientBalanceException;
import com.example.demo.model.AccountDetails;
import com.example.demo.model.BankTransaction;
import com.example.demo.repo.AccountDetailsRepo;
import com.example.demo.service.BankTransactionService;
 
@RestController
@RequestMapping(value="/banktransaction")
@CrossOrigin("*")
public class BankTransactionController {

    private final BankTransactionService bankTransactionService;
    
    private final AccountDetailsRepo accountDetailsRepo;

    // Constructor Injection
  
    public BankTransactionController(BankTransactionService bankTransactionService,
                               AccountDetailsRepo accountDetailsRepo) {
        this.bankTransactionService = bankTransactionService;
        
        this.accountDetailsRepo = accountDetailsRepo;
    }
	@GetMapping(value="/showTransactionInfo")
	public List<BankTransaction> showTransactionInfo(){
		return bankTransactionService.showTransactionInfo();
	}
	@GetMapping(value="/searchByTransactionId/{transactionId}")
	public ResponseEntity<BankTransaction> searchByTransactionId(@PathVariable int transactionId) {
		try {
			BankTransaction bankTransaction =  bankTransactionService.searchByTransactionId(transactionId);
			return new ResponseEntity<>(bankTransaction,HttpStatus.OK);
		}catch(NoSuchElementException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	@GetMapping(value="/searchByAccountNumber/{accountNumber}")
	public ResponseEntity<List<BankTransaction>> searchByAccountNumber(@PathVariable int accountNumber) {
	    try {
	        List<BankTransaction> bankTransactions = bankTransactionService.searchByAccountNumber(accountNumber);
	        return new ResponseEntity<>(bankTransactions, HttpStatus.OK); // Return data and status
	    } catch (AccountNotFoundException e) {
	        return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Return 404 if not found
	    }
	}
 
    @PostMapping(value="/transferFunds")
    public ResponseEntity<String> transferFunds(@RequestParam int accountNumber, @RequestParam int toAccNo, @RequestParam double amount) {
        try {
        	bankTransactionService.transferFunds(accountNumber, toAccNo, amount);
            return ResponseEntity.ok("Transfer successful");
        } catch (InsufficientBalanceException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @PutMapping("/manualPayments")
    public ResponseEntity<String> processPayment(@RequestParam int accountNumber, @RequestParam double paymentAmount) {
        // Fetch the account details
        AccountDetails account = accountDetailsRepo.findByAccountNumber(accountNumber);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account not found");
        }
 
      
     
 
        // Check if balance is sufficient
        if (account.getBalance() < paymentAmount) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient balance");
        }
 
        // Deduct the payment amount from balance
        account.setBalance(account.getBalance() - paymentAmount);
 
        // Log the new balance after deduction
        
 
        // Save updated account balance
        try {
            accountDetailsRepo.save(account);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving account: " + e.getMessage());
        }
 
        // Proceed with payment processing (log transaction, etc.)
        return ResponseEntity.ok("Payment processed successfully");
    }
}
