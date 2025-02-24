package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.exception.LoanApplicationNotFoundException;
import com.example.demo.exception.LoanApprovalException;
import com.example.demo.exception.LoanRejectionException;
import com.example.demo.model.LoanApplication;
import com.example.demo.service.LoanApplicationService;

@RestController
@RequestMapping(value="/loanapplication")
@CrossOrigin("*")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    // Constructor Injection
   
    public LoanApplicationController(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

   @GetMapping(value="/showloanapplication")
   public List<LoanApplication> showApplications(){
       return loanApplicationService.showLoanapplication();
   }

   @GetMapping(value="/searchloanapplicationById/{loanId}")
   public ResponseEntity<LoanApplication> searchLoanapplication(@PathVariable int loanId) {
       try {
           LoanApplication loanApplication = loanApplicationService.searchLoanapplicationById(loanId);
           return new ResponseEntity<>(loanApplication, HttpStatus.OK);
       } catch(LoanApplicationNotFoundException ex) {
           return new ResponseEntity<>(HttpStatus.NOT_FOUND);
       }
   }

   @GetMapping(value="/searchloanApplicationByAccountNumber/{accountNumber}")
   public List<LoanApplication> searchLoanapplicationByAccountNumber(@PathVariable int accountNumber) {
       return loanApplicationService.searchloanApplicationByaccountNumber(accountNumber);
   }

   @GetMapping(value="/searchLoanAplicationByStatus/{approvalStatus}")
   public List<LoanApplication> searchLoanApplicationByApprovalStatus(@PathVariable String approvalStatus){
       return loanApplicationService.searchLoanApplicationByApprovalStatus(approvalStatus);
   }

   @PostMapping(value = "/addLoanapplication")
   public ResponseEntity<String> addLoanapplication(@RequestBody LoanApplication loanApplication) {

       
       return loanApplicationService.addLoanApplication(loanApplication); // Return success message
   }

   @PutMapping(value="/updateLoanapplication")
   public void updateLoanapplication(@RequestBody LoanApplication loanapplication) {
       loanApplicationService.updateLoanapplication(loanapplication);
   }

   @DeleteMapping(value="/deleteLoanapplication/{loanId}")
   public void deleteLoanapplication(@PathVariable int loanId) {
       loanApplicationService.deleteLoanapplication(loanId);
   }

   @PutMapping("/acceptLoan/{loanId}")
   public ResponseEntity<String> acceptLoan(@PathVariable int loanId) {
       try {
           boolean isAccepted = loanApplicationService.acceptLoan(loanId);
           if (isAccepted) {
               return ResponseEntity.ok("Loan approved and saved to LoanSanction table.");
           } else {
               return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to approve loan.");
           }
       } catch (LoanApprovalException ex) {
           return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
       }
   }
   
   @PutMapping("/rejectLoan/{loanId}")
   public ResponseEntity<String> rejectLoan(@PathVariable int loanId){
       try {
           boolean isRejected = loanApplicationService.rejectLoan(loanId);
           if(isRejected) {
               return ResponseEntity.ok("Loan rejected and saved to LoanSanction table.");
           } else {
               return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to reject loan.");
           }
       } catch (LoanRejectionException ex) {
           return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
       }
   }
   
   
}
