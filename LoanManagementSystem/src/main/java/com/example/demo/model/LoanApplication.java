package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Entity
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//@Table(name="LOANAPPLICATION")

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="LOANAPPLICATION")
public class LoanApplication {

	@Id
	@Column(name="LOANID")
	private int loanId;
	@Column(name="ACCOUNTNUMBER")
	private int accountNumber;
	@Column(name="LOANAMOUNT")
	private double loanAmount;
	@Column(name="LOANTYPE")
	private String loanType;
	@Column(name="EMPLOYTYPE")
	private String employType;
	@Column(name="ANNUALINCOME")
	private double annualIncome;
	@Column(name="STATUS")
	private String status = "PENDING";  // Default value

	@Column(name="INTERESTRATE")
	private double interestRate;
	@Column(name="TENURE")
	private int tenure;
	@Column(name="CREDITSCORE")
	private int creditScore;
	
	
}
