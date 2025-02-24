package com.example.demo.repo;
 
import java.util.List;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import com.example.demo.model.LoanSanction;
 
@Repository
public interface LoanSanctionRepo extends JpaRepository<LoanSanction, Integer> {
 
	LoanSanction findByLoanId(int loanId);
	List<LoanSanction> findBySanctionStatus(String sanctionStatus);
}