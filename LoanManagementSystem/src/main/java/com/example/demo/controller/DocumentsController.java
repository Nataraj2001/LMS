package com.example.demo.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.exception.DocumentNotFoundException;
import com.example.demo.exception.FileUploadException;
import com.example.demo.exception.LoanApprovalException;
import com.example.demo.model.Constants;
import com.example.demo.model.Documents;
import com.example.demo.service.DocumentsService;

@RestController
@RequestMapping(value = "/documents")
@CrossOrigin(origins = "*")
public class DocumentsController {

    private final DocumentsService documentsService;

    // Constructor Injection
    public DocumentsController(DocumentsService documentsService) {
		this.documentsService = documentsService;
	}

    /**
	 * Upload Documents - If documents already exist, send a message to fetch and
	 * update instead.
	 */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocuments(
            @RequestParam int accountNumber,
            @RequestParam(Constants.AADHAAR) MultipartFile aadhaarFile,
            @RequestParam(Constants.PAN) MultipartFile panFile,
            @RequestParam(Constants.BANK_STATEMENT) MultipartFile bankStatmentFile) {
        try {
            List<Documents> existingDocs = documentsService.getDocumentsByAccountNumber(accountNumber);
            if (!existingDocs.isEmpty()) {
                // Return the message instead of throwing an exception
                return ResponseEntity.ok("Documents already exist. Please fetch and update if needed.");
            }
            documentsService.saveOrUpdateDocuments(accountNumber, aadhaarFile, panFile, bankStatmentFile);
            return ResponseEntity.ok("Documents uploaded successfully!");
        } catch (IOException e) {
            throw new FileUploadException("File upload failed: " + e.getMessage());
        }
    }

     
    

 

	/**
	 * Fetch all documents for an account number.
	 */
	@GetMapping("/{accountNumber}")
	public ResponseEntity<List<Documents>> getDocuments(@PathVariable int accountNumber) {
		List<Documents> documents = documentsService.getDocumentsByAccountNumber(accountNumber);
		if (documents.isEmpty()) {
			throw new DocumentNotFoundException("No documents found for account number " + accountNumber);
		}
		return ResponseEntity.ok(documents);
	}

	/**
	 * Download a document by its ID.
	 */
	@GetMapping("/download/{id}")
	public ResponseEntity<byte[]> downloadDocument(@PathVariable int id) {
		Optional<Documents> documentOptional = documentsService.getDocumentById(id);

		if (documentOptional.isPresent()) {
			Documents document = documentOptional.get();
			MediaType mediaType = determineMediaType(document.getFileType());

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + document.getDocumentName())
					.contentType(mediaType).body(document.getFileData());
		}
		return ResponseEntity.notFound().build();
	}

	/**
	 * Update a document by document ID.
	 */
	@PutMapping("/update/{documentId}")
	public ResponseEntity<String> updateDocument(@PathVariable int documentId,
	        @RequestParam("file") MultipartFile file) {  // Changed "File" to "file"
	    try {
	        Optional<Documents> existingDocument = documentsService.getDocumentById(documentId);
	        if (existingDocument.isEmpty()) {
	            return ResponseEntity.status(404).body("Document not found.");
	        }

	        documentsService.saveOrUpdateDocument(existingDocument.get().getAccountNumber(),
	                existingDocument.get().getDocumentName(), file);
	        return ResponseEntity.ok("Document updated successfully!");
	    } catch (IOException e) {
	        return ResponseEntity.status(500).body("File update failed. " + e.getMessage());
	    }
	}

	/**
	 * Delete a document by document ID.
	 */
	@DeleteMapping("/delete/{documentId}")
	public ResponseEntity<String> deleteDocument(@PathVariable int documentId) {
		Optional<Documents> existingDocument = documentsService.getDocumentById(documentId);
		if (existingDocument.isPresent()) {
			documentsService.deleteDocuments(documentId);
			return ResponseEntity.ok("Document deleted successfully!");
		} else {
			return ResponseEntity.status(404).body("Document not found.");
		}
	}

	/**
	 * Determines the MediaType of a document for downloading.
	 */
	private MediaType determineMediaType(String fileType) {
		switch (fileType) {
		case "image/png":
			return MediaType.IMAGE_PNG;
		case "image/jpeg":
			return MediaType.IMAGE_JPEG;
		case "application/pdf":
			return MediaType.APPLICATION_PDF;
		default:
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

	// Update verification status of document
	@PutMapping("/approve/{documentId}")
	public ResponseEntity<String> approveDocument(@PathVariable int documentId) {
		Optional<Documents> documentOptional = documentsService.getDocumentById(documentId);

		if (documentOptional.isPresent()) {
			Documents document = documentOptional.get();
			document.setVerificationStatus("VERIFIED");
			documentsService.saveOrUpdateDocument(document); // Update document status

			// Check if all documents are verified
			List<Documents> allDocuments = documentsService.getDocumentsByAccountNumber(document.getAccountNumber());
			boolean allVerified = allDocuments.stream().allMatch(doc -> "Verified".equals(doc.getVerificationStatus()));

			if (allVerified) {
				// Enable the loan approval button if all documents are verified
				return ResponseEntity.ok("Document approved. All documents are verified, you can approve the loan.");
			} else {
				return ResponseEntity.ok("Document approved, waiting for other documents to be verified.");
			}
		} else {
			return ResponseEntity.status(404).body("Document not found");
		}
	}

	// Approve loan
	@PutMapping("/approve-loan/{accountNumber}")
	public ResponseEntity<String> approveLoan(@PathVariable int accountNumber) {
		List<Documents> documents = documentsService.getDocumentsByAccountNumber(accountNumber);
		boolean allVerified = documents.stream().allMatch(doc -> "Verified".equals(doc.getVerificationStatus()));
		if (!allVerified) {
			throw new LoanApprovalException("Cannot approve loan. Not all documents are verified.");
		} // Loan approval logic here
		return ResponseEntity.ok("Loan approved successfully."); 
	}
	
	
	
}
