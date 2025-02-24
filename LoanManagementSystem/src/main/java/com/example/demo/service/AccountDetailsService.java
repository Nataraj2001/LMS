package com.example.demo.service;
import org.apache.log4j.Logger;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

import com.example.demo.exception.EmailSendingException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.AccountDetails;
import com.example.demo.repo.AccountDetailsRepo;
import jakarta.transaction.Transactional;
 
@Service
@Transactional
public class AccountDetailsService {

    // Logger to track the service's operations
    private static Logger logger = Logger.getLogger(AccountDetailsService.class);

    private final AccountDetailsRepo accountdetailsRepo;
    private final EmailService emailService;

	private double balance;

    // Constructor Injection
  
    public AccountDetailsService(AccountDetailsRepo accountdetailsRepo, EmailService emailService) {
        this.accountdetailsRepo = accountdetailsRepo;
        this.emailService = emailService;
    }
    
 // Modify the setBalance method to prevent negative balance
    public void setBalance(double balance) {
        if (balance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative.");
        }
        this.balance = balance;
    }
 
    /**
    * This method retrieves all account details from the repository.
     * @return List<AccountDetails> List of all account details.
     */
    public List<AccountDetails> showAccountDetails() {
        logger.info("Fetching all account details.");
        List<AccountDetails> accountDetailsList = accountdetailsRepo.findAll();
        logger.info("Fetched {} account details." + accountDetailsList.size());
        return accountDetailsList;
    }
 
    /**
     * This method searches for an account by its ID (AccountNumber).
     * @param AccountNumber the ID of the account to search.
     * @return Optional<AccountDetails> account details if found, throws ResourceNotFoundException if not.
     */
    public Optional<AccountDetails> searchAccountDetailsById(int accountNumber) {
        logger.info("Searching for account with AccountNumber: " + accountNumber);
        return Optional.ofNullable(accountdetailsRepo.findById(accountNumber).orElseThrow(
                () -> new ResourceNotFoundException(" The account with AccountNumber was not found." + accountNumber)));
    }
 
    /**
     * This method adds a new account to the repository and sends a creation email.
     * @param accountdetails the account details to add.
     */
    public void addAccountDetails(AccountDetails accountdetails) {
        logger.info("Adding new account with AccountNumber: " + accountdetails.getAccountNumber());
        accountdetailsRepo.save(accountdetails);

        String email = accountdetails.getEmail();
        String userName = accountdetails.getFirstName();
        int accountNumber = accountdetails.getAccountNumber();

        try {
            emailService.sendAccountCreationEmail(email, userName, accountNumber);
        } catch (EmailSendingException e) {
            logger.error("An error occurred while sending the email", e);
        }

        logger.info("Account creation email sent to " + email);
    }

    /**
     * This method updates the account status and closed date for a given account number.
     * @param accountNo the account number to update.
     * @param accountStatus the status of the account.
     * @param accountClosedDate the date when the account was closed.
     */
    public void updateAccountStatus(int accountNo, String accountStatus, Date accountClosedDate) {
        logger.info("Updating status for account with AccountNumber: " + accountNo);
        AccountDetails existingAccount = accountdetailsRepo.findByAccountNumber(accountNo);
        if (existingAccount == null) {
            logger.error("The AccountNumber provided does not match any existing account." + accountNo);
            throw new ResourceNotFoundException("Account with the specified AccountNumber does not exist. " + accountNo);
        }
        existingAccount.setAccountStatus(accountStatus);
        existingAccount.setAccountClosedDate(accountClosedDate);
        accountdetailsRepo.save(existingAccount);
        logger.info("Account status updated for AccountNumber: " + accountNo);
    }
 
    /**
     * This method searches for an account by the first name.
     * @param firstName the first name to search by.
     * @return AccountDetails account details if found, throws ResourceNotFoundException if not.
     */
    public AccountDetails searchAccountDetailsByFirstName(String firstName) {
        logger.info("Searching for account with FirstName: " + firstName);
        AccountDetails accountDetails = accountdetailsRepo.findByFirstName(firstName);
        if (accountDetails == null) {
            logger.error("Account  is not found with FirstName: " + firstName);
            throw new ResourceNotFoundException("Account not found with FirstName: " + firstName);
        }
        logger.info("Account found with FirstName: " + firstName);
        return accountDetails;
    }
 
    /**
     * This method searches for an account by email.
     * @param email the email to search by.
     * @return AccountDetails account details if found.
     */
    public AccountDetails searchAccountDetailsByEmail(String email) {
        logger.info("Searching for account with Email: " + email);
        AccountDetails accountDetails = accountdetailsRepo.findByEmail(email);
        if (accountDetails == null) {
            logger.error("Account not found with Email: " + email);
        }
        logger.info("Account found with Email: " + email);
        return accountDetails;
    }
 
    /**
     * This method checks if an account exists based on the email or mobile number.
     * @param email the email to check.
     * @param mobileNo the mobile number to check.
     * @return boolean true if account exists, false otherwise.
     */
    

    
   
    public boolean existsByEmailOrMobileNo(String email, String mobileNo) {
        logger.debug("Checking if account exists with email:  or mobile number: " + email + mobileNo);
        
        boolean exists = accountdetailsRepo.existsByEmailOrMobileNo(email, mobileNo);
        
        if (exists) {
            logger.info("Account exists with email:  or mobile number: " + email + mobileNo);
        } else {
            logger.info("No account found with email:  or mobile number: " + email + mobileNo);
        }
        
        return exists;
    }
 
    
}