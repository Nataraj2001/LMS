package com.example.demo.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="LOANREPAYMENTS")

public class LoanRepayments {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="PAYMENTID")
	private int paymentId;
	@Column(name="LOANID")
	private int loanId;
	@Column(name="PAYMENTDATE")
	private Date paymentDate;
	@Column(name="PAYMENTAMOUNT")
	private double paymentAmount;
	@Column(name="PAYMENTMODE")
	private String paymentMode;
	@Column(name="PAYMENTSTATUS")
	private String paymentStatus;
	@Column(name="DUELOANAMOUNT")
	private double dueLoanAmount;
}
