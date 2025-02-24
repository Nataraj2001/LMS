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
@AllArgsConstructor
@NoArgsConstructor
@Table(name="banktransaction")
public class BankTransaction {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="TRANSACTIONID")
	private int transactionId;
	@Column(name="ACCOUNTNUMBER")
	private int accountNumber ;
	@Column(name="TOACCNO")
	private int toAccNo ;
	@Column(name="TRANSACTIONTYPE")
	private String transactionType;
	@Column(name="TRANSACTIONAMOUNT")
	private double transactionAmount;
	@Column(name="TRANSACTIONDATE")
	private Date transactionDate;
	@Column(name="BALANCEAFTERTRANSACTION")
	private double balanceAfterTransaction;
	@Column(name="TRANSACTIONSTATUS")
	private String transactionStatus;
	
}
