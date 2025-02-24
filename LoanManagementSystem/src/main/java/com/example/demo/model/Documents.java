package com.example.demo.model;
 
import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "documents")
public class Documents {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DOCUMENTID")
    private int documentId;
 
    @Column(name = "ACCOUNTNUMBER", nullable = false)
    private int accountNumber;
 
    @Column(name = "DOCUMENTNAME", nullable = false)
    private String documentName; // AADHAAR, PAN, SIGNATURE
 
    @Column(name = "DOCUMENTINFO")
    private String documentInfo;
 
    @Column(name = "VERIFICATIONSTATUS", nullable = false)
    private String verificationStatus = "Pending";
 
    @Lob
    @Column(name = "FILEDATA", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] fileData;
 
    @Column(name = "FILETYPE", nullable = false)
    private String fileType;
}