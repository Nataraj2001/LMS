package com.example.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.BankTransaction;

@Repository
public interface BankTransactionRepo extends JpaRepository<BankTransaction, Integer>{

	List<BankTransaction> findByAccountNumber(int accountNumber);
}
