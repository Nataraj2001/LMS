package com.example.demo.repo;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.SignUp;

@Repository
public interface SignUpRepo extends JpaRepository<SignUp, Integer> {


	long countByUsernameAndRole(String username,String role);

	SignUp findResByUsername(String username);
	
	Optional<SignUp> findByUsername(String username);
	SignUp findByUsernameAndRole(String username, String role);
	
	SignUp findByUsernameAndPasswordAndRole(String username, String password, String role);
		
	long countByUsernameAndPasswordAndRole(String username, String password, String role);
	
	Optional<SignUp> findByEmail(String email);
	
    SignUp searchByEmail(String email);


}
