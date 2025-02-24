package com.example.demo.controller;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.AccountDetails;
import com.example.demo.service.AccountDetailsService;

@RestController
@RequestMapping(value = "/accountdetails")
@CrossOrigin(origins = "*")
public class AccountDetailsController {

    private final AccountDetailsService accountdetailsService;

    // Constructor Injection
   
    public AccountDetailsController(AccountDetailsService accountdetailsService) {
        this.accountdetailsService = accountdetailsService;
    }

	@GetMapping(value = "/showAccountDetails")
	public List<AccountDetails> showAccountDetails() {
		return accountdetailsService.showAccountDetails();
	}

	@GetMapping(value = "/searchAccountDetails/{accountNumber}")
	public ResponseEntity<AccountDetails> searchAccountDetailsById(@PathVariable int accountNumber) {
		Optional<AccountDetails> accountOpt = accountdetailsService.searchAccountDetailsById(accountNumber);

		return accountOpt.map(account -> new ResponseEntity<>(account, HttpStatus.OK)).orElseThrow(
				() -> new ResourceNotFoundException("Account not found with AccountNumber: " + accountNumber));
	}

	@PostMapping(value = "/addAccountDetails")
	public void addEmploy(@RequestBody AccountDetails accountdetails) {
		accountdetailsService.addAccountDetails(accountdetails);
	}

	@PutMapping("/updateAccountDetails/{accountNumber}")
	public ResponseEntity<String> updateAccountDetails(@PathVariable int accountNumber,
			@RequestBody AccountDetails accountDetails) {
		try {
			accountdetailsService.updateAccountStatus(accountNumber, accountDetails.getAccountStatus(),
					accountDetails.getAccountClosedDate());
			return ResponseEntity.ok("Account successfully deactivated.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deactivating account.");
		}
	}

	@GetMapping(value = "/searchAccountDetailsByfirstName/{firstName}")
	public ResponseEntity<AccountDetails> searchAccountDetailsByFirstname(@PathVariable String firstName) {
		try {
			AccountDetails accountdetails = accountdetailsService.searchAccountDetailsByFirstName(firstName);
			return new ResponseEntity<>(accountdetails, HttpStatus.OK);
		} catch (ResourceNotFoundException e) {
			throw new ResourceNotFoundException("Account not found with FirstName: " + firstName);
		}
	}

	@GetMapping(value="/searchAccountDetailsByEmail/{email}")
	public ResponseEntity<AccountDetails> searchAccountDetailsByEmail(@PathVariable String email) {
	    try {
	    	
	        AccountDetails accountdetails = accountdetailsService.searchAccountDetailsByEmail(email);
	        return new ResponseEntity<>(accountdetails, HttpStatus.OK);
	    } catch (NoSuchElementException e) {
	        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	    }
	}

}
