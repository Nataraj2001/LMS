package com.example.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.AccountDetails;


@Repository
public interface AccountDetailsRepo extends JpaRepository<AccountDetails, Integer>{
	
	AccountDetails findByFirstName(String firstName);
	
	public AccountDetails findByAccountNumber(int accountNumber);
	
	public AccountDetails findByEmail(String email);
	
	
	boolean existsByEmailOrMobileNo(String email, String mobileNo);


}
