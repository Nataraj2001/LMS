package com.example.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.LoanApplication;

@Repository
public interface LoanApplicationRepo extends JpaRepository<LoanApplication, Integer>{
	
	List<LoanApplication> findByAccountNumber(int accountNumber);
	
	List<LoanApplication> findByStatus(String status);
	
	 List<LoanApplication> findByAccountNumberAndLoanType(int accountNumber, String loanType);
	 
	 Optional<LoanApplication> findByloanId(int loanId);
}
