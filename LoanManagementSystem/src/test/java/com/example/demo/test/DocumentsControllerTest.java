package com.example.demo.test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.controller.DocumentsController;
import com.example.demo.exception.DocumentNotFoundException;
import com.example.demo.exception.LoanApprovalException;
import com.example.demo.model.Documents;
import com.example.demo.service.DocumentsService;



class DocumentsControllerTest {

    @Mock
    private DocumentsService documentsService;

    @InjectMocks
    private DocumentsController documentsController;

    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    void testUploadDocuments_WhenDocumentsExist() {
        int accountNumber = 123;
        MultipartFile aadhaarFile = mock(MultipartFile.class);
        MultipartFile panFile = mock(MultipartFile.class);
        MultipartFile signatureFile = mock(MultipartFile.class);

        // Simulating existing documents
        when(documentsService.getDocumentsByAccountNumber(accountNumber))
            .thenReturn(Arrays.asList(new Documents()));

        // Call the uploadDocuments method
        ResponseEntity<String> response = documentsController.uploadDocuments(accountNumber, aadhaarFile, panFile, signatureFile);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Documents already exist. Please fetch and update if needed.", response.getBody());
    }


    @Test
    void testUploadDocuments_Success() throws IOException {
        int accountNumber = 123;
        MultipartFile aadhaarFile = mock(MultipartFile.class);
        MultipartFile panFile = mock(MultipartFile.class);
        MultipartFile signatureFile = mock(MultipartFile.class);

        when(documentsService.getDocumentsByAccountNumber(accountNumber)).thenReturn(Arrays.asList()); // No existing documents
        doNothing().when(documentsService).saveOrUpdateDocuments(accountNumber, aadhaarFile, panFile, signatureFile);

        ResponseEntity<String> response = documentsController.uploadDocuments(accountNumber, aadhaarFile, panFile, signatureFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Documents uploaded successfully!", response.getBody());
    }

    @Test
    void testGetDocuments_DocumentsFound() {
        int accountNumber = 123;
        Documents doc = new Documents();
        when(documentsService.getDocumentsByAccountNumber(accountNumber)).thenReturn(Arrays.asList(doc));

        ResponseEntity<List<Documents>> response = documentsController.getDocuments(accountNumber);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetDocuments_NoDocumentsFound() {
        int accountNumber = 123;
        when(documentsService.getDocumentsByAccountNumber(accountNumber)).thenReturn(Arrays.asList());

        DocumentNotFoundException exception = assertThrows(DocumentNotFoundException.class, () -> {
            documentsController.getDocuments(accountNumber);
        });

        assertEquals("No documents found for account number " + accountNumber, exception.getMessage());
    }

    @Test
    void testDownloadDocument_Success() {
        int documentId = 1;
        Documents document = new Documents();
        document.setDocumentName("test.pdf");
        document.setFileType("application/pdf");
        document.setFileData(new byte[]{1, 2, 3});

        when(documentsService.getDocumentById(documentId)).thenReturn(Optional.of(document));

        ResponseEntity<byte[]> response = documentsController.downloadDocument(documentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(new byte[]{1, 2, 3}, response.getBody());
    }

    @Test
    void testDownloadDocument_DocumentNotFound() {
        int documentId = 1;
        when(documentsService.getDocumentById(documentId)).thenReturn(Optional.empty());

        ResponseEntity<byte[]> response = documentsController.downloadDocument(documentId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testApproveLoan_AllDocumentsVerified() {
        int accountNumber = 123;
        Documents doc1 = new Documents();
        doc1.setVerificationStatus("Verified");
        Documents doc2 = new Documents();
        doc2.setVerificationStatus("Verified");

        when(documentsService.getDocumentsByAccountNumber(accountNumber))
            .thenReturn(Arrays.asList(doc1, doc2));

        ResponseEntity<String> response = documentsController.approveLoan(accountNumber);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Loan approved successfully.", response.getBody());
    }

    @Test
    void testApproveLoan_DocumentsNotVerified() {
        int accountNumber = 123;
        Documents doc1 = new Documents();
        doc1.setVerificationStatus("Verified");
        Documents doc2 = new Documents();
        doc2.setVerificationStatus("Not Verified");

        when(documentsService.getDocumentsByAccountNumber(accountNumber))
            .thenReturn(Arrays.asList(doc1, doc2));

        LoanApprovalException exception = assertThrows(LoanApprovalException.class, () -> {
            documentsController.approveLoan(accountNumber);
        });

        assertEquals("Cannot approve loan. Not all documents are verified.", exception.getMessage());
    }
    
}
