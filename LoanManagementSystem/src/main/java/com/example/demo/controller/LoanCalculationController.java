package com.example.demo.controller;
 

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
 
import com.example.demo.service.LoanCalculationService;
import com.example.demo.service.LoanCalculationService.LoanCalculationResult;
 
 
@RestController
@RequestMapping("/loanCalculator")
public class LoanCalculationController {

    private final LoanCalculationService loanCalculationService;

    // Constructor Injection

    public LoanCalculationController(LoanCalculationService loanCalculationService) {
        this.loanCalculationService = loanCalculationService;
    }
 
    @PostMapping("/calculate")
    public LoanCalculationResult calculateLoan(@RequestParam double loanAmount,
                                               @RequestParam double annualInterestRate,
                                               @RequestParam int loanTerm,
                                               @RequestParam String loanType) {
        LoanCalculationResult result = loanCalculationService.calculateLoan(loanAmount, annualInterestRate, loanTerm, loanType);
        if (result == null) {
            throw new IllegalArgumentException("Invalid loan calculation parameters.");
        }
        return result;
    }
 
}
