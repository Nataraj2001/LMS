package com.example.demo.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name="LOANSANCTION")
public class LoanSanction {

	@Id
	@Column(name="SANCTIONID")
	private int sanctionId;
	@Column(name="LOANID")
	private int loanId;
	@Column(name="SANCTIONDATE")
	private Date sanctionDate;
	@Column(name="SANCTIONAMOUNT")
	private double sanctionAmount;
	@Column(name="SANCTIONEDBY")
	private String sanctionedBy;
	@Column(name="SANCTIONSTATUS")
	private String sanctionStatus;
	@Column(name="LOANSTARTDATE")
	private Date loanStartDate;
	@Column(name="LOANENDDATE")
	private Date loanEndDate;
	@Column(name="INTERESTRATE")
	private double interestRate;
	@Column(name="MONTHLYINSTALLMENTSAMOUNT")
	private double monthlyInstallmentsAmount;
	
}
