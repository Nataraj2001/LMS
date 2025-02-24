package com.example.demo.repo;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.model.LoanRepayments;
@Repository
public interface LoanRepaymentsRepo extends JpaRepository<LoanRepayments, Integer>{
    public List<LoanRepayments> findByLoanId(int loanId);
    public List<LoanRepayments> findByPaymentStatus(String paymentStatus);
    List<LoanRepayments> findByPaymentDateAndPaymentMode(Date paymentDate, String paymentMode);
    Optional<LoanRepayments> findTopByLoanIdOrderByPaymentDateDesc(int loanId);
    List<LoanRepayments> findRepaymentsByLoanIdAndPaymentDateBetween(
            int loanId, Date startDate, Date endDate);
	public List<LoanRepayments> findByLoanIdAndPaymentStatus(int loanId, String accountStatus);
	public List<LoanRepayments> findByPaymentDateBeforeAndPaymentStatus(Date today, String string);
}