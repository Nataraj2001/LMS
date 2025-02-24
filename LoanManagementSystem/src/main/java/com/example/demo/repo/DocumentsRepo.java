package com.example.demo.repo;
 
import java.util.List;
import java.util.Optional;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.model.Documents;
 
@Repository
public interface DocumentsRepo extends JpaRepository<Documents, Integer>{
	List<Documents> findByAccountNumber(int accountNumber);
	Optional<Documents> findByAccountNumberAndDocumentName(int accountNumber, String documentType);
}