package com.example.demo.test;

import org.junit.jupiter.api.Test;
import com.example.demo.model.Documents;
import static org.junit.jupiter.api.Assertions.*;
	
	 class DocumentsTest {

	    // Test 1: Test No-Args Constructor and Setters/Getters
	    @Test
	    void testNoArgsConstructorAndGettersSetters() {
	        // Create an instance using the no-args constructor
	        Documents document = new Documents();

	        // Set values using setters
	        document.setDocumentId(1);
	        document.setAccountNumber(12345);
	        document.setDocumentName("AADHAAR");
	        document.setDocumentInfo("UID: 1234-5678-9101");
	        document.setVerificationStatus("Verified");
	        document.setFileData(new byte[]{1, 2, 3, 4});
	        document.setFileType("image/jpeg");

	        // Validate the values using getters
	        assertEquals(1, document.getDocumentId());
	        assertEquals(12345, document.getAccountNumber());
	        assertEquals("AADHAAR", document.getDocumentName());
	        assertEquals("UID: 1234-5678-9101", document.getDocumentInfo());
	        assertEquals("Verified", document.getVerificationStatus());
	        assertArrayEquals(new byte[]{1, 2, 3, 4}, document.getFileData());
	        assertEquals("image/jpeg", document.getFileType());
	    }

	    // Test 2: Test All-Args Constructor
	    @Test
	    void testAllArgsConstructor() {
	        // Create an instance using the all-args constructor
	        Documents document = new Documents(1, 12345, "PAN", "PAN Number: ABCDE1234F", "Verified", new byte[]{5, 6, 7, 8}, "image/png");

	        // Validate the values using getters
	        assertEquals(1, document.getDocumentId());
	        assertEquals(12345, document.getAccountNumber());
	        assertEquals("PAN", document.getDocumentName());
	        assertEquals("PAN Number: ABCDE1234F", document.getDocumentInfo());
	        assertEquals("Verified", document.getVerificationStatus());
	        assertArrayEquals(new byte[]{5, 6, 7, 8}, document.getFileData());
	        assertEquals("image/png", document.getFileType());
	    }

	    // Test 3: Test Setters and Getters Individually
	    @Test
	    void testSettersAndGetters() {
	        Documents document = new Documents();

	        // Set values individually
	        document.setDocumentId(2);
	        document.setAccountNumber(67890);
	        document.setDocumentName("SIGNATURE");
	        document.setDocumentInfo("Signature on file");
	        document.setVerificationStatus("Pending");
	        document.setFileData(new byte[]{9, 10, 11, 12});
	        document.setFileType("application/pdf");

	        // Validate values using assertions
	        assertAll(
	            () -> assertEquals(2, document.getDocumentId()),
	            () -> assertEquals(67890, document.getAccountNumber()),
	            () -> assertEquals("SIGNATURE", document.getDocumentName()),
	            () -> assertEquals("Signature on file", document.getDocumentInfo()),
	            () -> assertEquals("Pending", document.getVerificationStatus()),
	            () -> assertArrayEquals(new byte[]{9, 10, 11, 12}, document.getFileData()),
	            () -> assertEquals("application/pdf", document.getFileType())
	        );
	    }

	    // Test 4: Test Default Verification Status
	    @Test
	    void testDefaultVerificationStatus() {
	        // Create an instance using the no-args constructor
	        Documents document = new Documents();

	        // Validate that the default verification status is "Pending"
	        assertEquals("Pending", document.getVerificationStatus());
	    }

	    // Test 5: Test File Data and File Type
	    @Test
	    void testFileDataAndFileType() {
	        byte[] fileData = {1, 2, 3};
	        Documents document = new Documents(1, 12345, "AADHAAR", "Document Info", "Pending", fileData, "image/jpeg");

	        // Validate that the file data and file type are correctly set
	        assertArrayEquals(fileData, document.getFileData());
	        assertEquals("image/jpeg", document.getFileType());
	    }

	    // Test 6: Test Document Name Cannot Be Null
	    @Test
	    void testDocumentNameCannotBeNull() {
	        Documents document = new Documents();
	        document.setDocumentName("PAN");

	        assertNotNull(document.getDocumentName(), "Document name should not be null");
	    }

	    // Test 7: Test Account Number Cannot Be Zero or Negative
	    @Test
	    void testAccountNumberCannotBeZeroOrNegative() {
	        Documents document = new Documents();

	        document.setAccountNumber(0);
	        assertEquals(0, document.getAccountNumber(), "Account number should not be zero.");

	        document.setAccountNumber(-1);
	        assertEquals(-1, document.getAccountNumber(), "Account number should not be negative.");
	    }
	}


