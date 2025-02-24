package com.example.demo.controller;
 
import java.util.List;
 

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
import com.example.demo.model.LoanSanction;
import com.example.demo.service.LoanSanctionService;
 
@RestController
@RequestMapping(value = "/loanSanction")
@CrossOrigin("*")
public class LoanSanctionController {

    private final LoanSanctionService loanSanctionService;

    // Constructor Injection
   
    public LoanSanctionController(LoanSanctionService loanSanctionService) {
        this.loanSanctionService = loanSanctionService;
    }
 
    @GetMapping(value = "/showLoanSanction")
    public List<LoanSanction> show() {
        return loanSanctionService.show();
    }
 
    @GetMapping(value = "/searchById/{sanctionId}")
    public ResponseEntity<LoanSanction> searchById(@PathVariable int sanctionId) {
        LoanSanction loanSanction = loanSanctionService.searchById(sanctionId);
        if (loanSanction == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(loanSanction, HttpStatus.OK);
    }
 
    @GetMapping(value = "/searchByLoanId/{loanId}")
    public ResponseEntity<LoanSanction> searchByLoanId(@PathVariable int loanId) {
        LoanSanction loanSanction = loanSanctionService.searchByLoanId(loanId);
        if (loanSanction == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(loanSanction, HttpStatus.OK);
    }
 
    @GetMapping(value = "/searchBySanctionStatus/{sanctionStatus}")
    public ResponseEntity<List<LoanSanction>> searchBySanctionStatus(@PathVariable String sanctionStatus) {
        List<LoanSanction> sanctions = loanSanctionService.searchBySanctionStatus(sanctionStatus);
        if (sanctions.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(sanctions, HttpStatus.OK);
    }
}