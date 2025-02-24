package com.example.demo.service;

import org.apache.log4j.Logger;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import com.example.demo.exception.AccountNotFoundException;
import com.example.demo.exception.InsufficientBalanceException;
import com.example.demo.model.AccountDetails;
import com.example.demo.model.BankTransaction;
import com.example.demo.model.Constants;
import com.example.demo.repo.AccountDetailsRepo;
import com.example.demo.repo.BankTransactionRepo;
import jakarta.transaction.Transactional;
 
@Service
@Transactional
public class BankTransactionService {

    // Logger to track the service's operations
    private static Logger logger = Logger.getLogger(BankTransactionService.class);

    private final BankTransactionRepo bankTransactionRepo;
    
    private final AccountDetailsRepo accountDetailsRepo;

    // Constructor Injection
   
    public BankTransactionService(BankTransactionRepo bankTransactionRepo, 
                                  
                                  AccountDetailsRepo accountDetailsRepo) {
        this.bankTransactionRepo = bankTransactionRepo;
        
        this.accountDetailsRepo = accountDetailsRepo;
    }
 
    /**
     * Fetches all bank transactions.
     * @return List of all bank transactions.
     */
    public List<BankTransaction> showTransactionInfo() {
        logger.info("Fetching all bank transactions.");
        return bankTransactionRepo.findAll();
    }
 
    /**
     * Searches for a bank transaction by transaction ID.
     * @param transactionId the ID of the transaction to search for.
     * @return BankTransaction found transaction.
     * @throws RuntimeException if transaction not found.
     */
    public BankTransaction searchByTransactionId(int transactionId) {
        logger.info("Searching for transaction with TransactionId: " + transactionId);
        return bankTransactionRepo.findById(transactionId).orElseThrow(() ->
            new RuntimeException("Transaction not found"));
    }
 
    /**
     * Searches for transactions by account number.
     * @param accountNumber the account number to search by.
     * @return List of BankTransactions for the given account.
     * @throws AccountNotFoundException if no transactions are found.
     */
    public List<BankTransaction> searchByAccountNumber(int accountNumber) {
        logger.info("Searching for transactions for AccountNumber: " + accountNumber);
        List<BankTransaction> transactions = bankTransactionRepo.findByAccountNumber(accountNumber);
        if (transactions.isEmpty()) {
            logger.error("No transactions found for account number: " + accountNumber);
            throw new AccountNotFoundException("No transactions found for account number: " + accountNumber);
        }
        logger.info("Found {} transactions for account number: " + transactions.size() + accountNumber);
        return transactions;
    }

    
    /**
     * Transfers funds between two accounts.
     * @param accountNumber the source account number.
     * @param toAccNo the destination account number.
     * @param amount the amount to transfer.
     * @throws InsufficientBalanceException if the source account has insufficient balance.
     */
    public void transferFunds(int accountNumber, int toAccNo, double amount) {
        logger.info("Transferring funds from AccountNumber:  to AccountNumber: " +  accountNumber + toAccNo);
        AccountDetails fromAccount = accountDetailsRepo.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("From account not found"));
        AccountDetails toAccount = accountDetailsRepo.findById(toAccNo)
                .orElseThrow(() -> new AccountNotFoundException("To account not found"));
        if (Double.compare(fromAccount.getBalance(), amount) < 0) {
            createTransaction(accountNumber, toAccNo,Constants.DEBIT, amount, fromAccount.getBalance(),Constants.FAILED);
            createTransaction(accountNumber, toAccNo, Constants.DEBIT, amount, fromAccount.getBalance(),Constants.FAILED);

            logger.error("Insufficient balance in account " + accountNumber);
            throw new InsufficientBalanceException("Insufficient balance in account " + accountNumber);
        }
        double fromNewBalance = fromAccount.getBalance() - amount;
        double toNewBalance = toAccount.getBalance() + amount;
        fromAccount.setBalance(fromNewBalance);
        toAccount.setBalance(toNewBalance);
        accountDetailsRepo.save(fromAccount);
        accountDetailsRepo.save(toAccount);
        createTransaction(accountNumber, toAccNo,Constants.DEBIT, amount, fromNewBalance,Constants.SUCCESS);
        createTransaction(toAccNo, accountNumber,Constants.CREDIT, amount, toNewBalance,Constants.SUCCESS);
    }
 
    /**
     * Makes a manual payment (debit) from an account.
     * @param accountNumber the account number to debit from.
     * @param paymentAmount the payment amount.
     * @throws InsufficientBalanceException if the account balance is insufficient.
     */
   
    public void manualPayment(int accountNumber, double paymentAmount) {
        logger.info("Making manual payment of amount:  from AccountNumber: " + paymentAmount + accountNumber);
        AccountDetails account = accountDetailsRepo.findByAccountNumber(accountNumber);
        if (account == null) {
            logger.error("Account not found with AccountNumber: " + accountNumber);
            throw new AccountNotFoundException("Account not found");
        }
        if (account.getBalance() < paymentAmount) {
           createTransaction(accountNumber, 0, Constants.DEBIT, paymentAmount, account.getBalance(),Constants.FAILED);
           logger.error("Insufficient balance for account: " + accountNumber);
           throw new InsufficientBalanceException("Insufficient balance");
        }
        account.setBalance(account.getBalance() - paymentAmount);
        accountDetailsRepo.save(account);
        createTransaction(accountNumber, 0, Constants.DEBIT, paymentAmount, account.getBalance(),Constants.SUCCESS);
    }
 
    /**
     * Creates a transaction record in the database.
     * @param accountNumber the source account number.
     * @param toAccNo the destination account number (or 0 for manual payments).
     * @param transactionType type of transaction (Debit/Credit).
     * @param amount the transaction amount.
     * @param newBalance balance after transaction.
     * @param status transaction status (SUCCESS/FAILED).
     */
    private void createTransaction(int accountNumber, int toAccNo, String transactionType, double amount, double newBalance, String status) {
        logger.info("Creating transaction for AccountNumber: + Amount: + Type: + Status: " + accountNumber + amount + transactionType + status);
        
        BankTransaction transaction = new BankTransaction();
        transaction.setAccountNumber(accountNumber);
        transaction.setToAccNo(toAccNo);
        transaction.setTransactionType(transactionType);
        transaction.setTransactionAmount(amount);
        transaction.setTransactionDate(new Date());
        transaction.setBalanceAfterTransaction(newBalance);
        transaction.setTransactionStatus(status);
        bankTransactionRepo.save(transaction);
        logger.info("Transaction recorded with status: " + status);
    }
 
    /**
     * Credits a loan amount to the specified account.
     * @param accountNumber the account number to credit.
     * @param amount the loan amount to credit.
     */
    public void creditLoanAmount(int accountNumber, double amount) {
        logger.info("Crediting loan amount:  to AccountNumber: " + amount + accountNumber);
        AccountDetails account = accountDetailsRepo.findById(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        double newBalance = account.getBalance() + amount;
        account.setBalance(newBalance);
        accountDetailsRepo.save(account);
        BankTransaction creditTransaction = new BankTransaction();
        creditTransaction.setAccountNumber(accountNumber);
        creditTransaction.setToAccNo(accountNumber);
        creditTransaction.setTransactionType(Constants.CREDIT);
        creditTransaction.setTransactionAmount(amount);
        creditTransaction.setTransactionDate(new Date());
        creditTransaction.setBalanceAfterTransaction(newBalance);
        creditTransaction.setTransactionStatus(Constants.SUCCESS);
        bankTransactionRepo.save(creditTransaction);
        logger.info("Loan amount credited successfully to AccountNumber: " + accountNumber);
    }
}

 
 