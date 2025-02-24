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
@Table(name="AccountDetails")
public class AccountDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="ACCOUNTNUMBER")
	private int accountNumber ;
	@Column(name="ACCOUNTTYPE")
	private String accountType;
	@Column(name="FIRSTNAME")
	private String firstName;
	@Column(name="LASTNAME")
	private String lastName;
	@Column(name="EMAIL")
	private String email;
	@Column(name="MOBILENO")
	private String mobileNo;
	@Column(name="ADDRESS")
	private String address;
	@Column(name="DATEOFBIRTH")
	private Date dateOfBirth;
	@Column(name="GENDER")
	private String gender;
	@Column(name="NOMINEENAME")
	private String nomineeName;
	@Column(name="ACCOUNTSTATUS")
	private String accountStatus;
	@Column(name="BALANCE")
	private double balance;
	@Column(name="ACCOUNTCREATIONDATE")
	private Date accountCreationDate;
	@Column(name="ACCOUNTCLOSEDDATE")
	private Date accountClosedDate;
	
	
}
