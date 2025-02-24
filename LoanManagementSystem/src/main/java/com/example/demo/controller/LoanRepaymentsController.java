package com.example.demo.controller;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping; // Add this import for the PUT method
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.model.LoanRepayments;
import com.example.demo.service.LoanRepaymentsService;
@RestController
@RequestMapping(value = "/loanRepayments")
@CrossOrigin("*")

public class LoanRepaymentsController {
	private final LoanRepaymentsService loanRepaymentsService;

    // Constructor injection
    public LoanRepaymentsController(LoanRepaymentsService loanRepaymentsService) {
        this.loanRepaymentsService = loanRepaymentsService;
    }
    @GetMapping(value = "/showLoanRepayments")
    public List<LoanRepayments> show() {
        return loanRepaymentsService.show();
    }
    @GetMapping("/getLastRepayment/{loanId}")
    public ResponseEntity<LoanRepayments> getLastRepayment(@PathVariable int loanId) {
        LoanRepayments lastRepayment = loanRepaymentsService.getLastRepayment(loanId);
        return new ResponseEntity<>(lastRepayment, HttpStatus.OK);
    }
    @PostMapping("/addRepayment")
    public LoanRepayments addRepayment(@RequestBody LoanRepayments repayment) {
        return loanRepaymentsService.addRepayment(repayment);
    }
    @GetMapping(value = "/searchByPaymentId/{paymentId}")
    public LoanRepayments searchByPayId(@PathVariable int paymentId) {
        return loanRepaymentsService.searchByPaymentId(paymentId);
    }
    @GetMapping(value = "/searchByPaymentStatus/{paymentStatus}")
    public List<LoanRepayments> searchByPayStatus(@PathVariable String paymentStatus) {
        return loanRepaymentsService.searchByPaymentStatus(paymentStatus);
    }
    @GetMapping(value = "/searchByLoanId/{loanId}")
    public List<LoanRepayments> searchByLoanId(@PathVariable int loanId) {
        return loanRepaymentsService.searchByLoanId(loanId);
    }
    @PutMapping("/process-normal-repayments")
    public String processNormalRepayments(@RequestBody LoanRepayments repayment) {
        loanRepaymentsService.processNormalRepayments(repayment);
        return "Normal repayments processing started!";
    }
    // New endpoint to update payment status
    @PutMapping("/updatePaymentStatus")
    public ResponseEntity<LoanRepayments> updatePaymentStatus(
            @RequestBody LoanRepayments updatedRepayment) {
        LoanRepayments repayment = loanRepaymentsService.updatePaymentStatus(
                updatedRepayment.getPaymentId(),
                updatedRepayment.getPaymentStatus(),
                updatedRepayment.getDueLoanAmount()
        );
        if (repayment != null) {
            return new ResponseEntity<>(repayment, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
// Add this to your LoanRepaymentsController class
    @PostMapping("/process-preclosure")
    public ResponseEntity<String> processPreclosure(@RequestBody Map<String, Object> request) {
        try {
            int loanId = Integer.parseInt(request.get("loanId").toString());
            int accountNumber = Integer.parseInt(request.get("accountNumber").toString());
            double preclosureAmount = Double.parseDouble(request.get("preclosureAmount").toString());
            String paymentMode = request.get("paymentMode").toString();
            loanRepaymentsService.processPreclosure(loanId, accountNumber, preclosureAmount, paymentMode);
            return new ResponseEntity<>("Pre-closure processed successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Pre-closure failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}