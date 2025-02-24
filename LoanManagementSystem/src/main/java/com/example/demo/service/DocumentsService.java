package com.example.demo.service;

import org.apache.log4j.Logger;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.exception.DocumentNotFoundException;
import com.example.demo.model.Constants;
import com.example.demo.model.Documents;
import com.example.demo.repo.DocumentsRepo;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class DocumentsService {

    private static Logger logger = Logger.getLogger(DocumentsService.class);

    private final DocumentsRepo documentsRepo;
    

    // Constructor injection for DocumentsRepo
    public DocumentsService(DocumentsRepo documentsRepo) {
        this.documentsRepo = documentsRepo;
    }

    /**
     * Retrieves all documents from the repository.
     * @return List of all Documents.
     */
    public List<Documents> showDocuments() {
        logger.info("Fetching all documents from the repository.");
        return documentsRepo.findAll();
    }

    /**
     * Searches for a document by its ID.
     * @param documentId the ID of the document to search for.
     * @return the document if found, otherwise null.
     */
    public Documents searchDocumentById(int documentId) {
        logger.info("Searching for document with ID: " + documentId);
        return documentsRepo.findById(documentId).orElse(null);
    }

    /**
     * Deletes a document by its ID.
     * @param documentId the ID of the document to delete.
     */
    public void deleteDocuments(int documentId) {
        logger.info("Deleting document with ID: " + documentId);
        documentsRepo.deleteById(documentId);
    }
 
    /**
     * Saves or updates documents based on account number and file types.
     * This method handles Aadhaar, PAN, and Signature file uploads.
     * @param accountNumber the account number to associate the documents with.
     * @param aadhaarFile the Aadhaar file to be uploaded.
     * @param panFile the PAN file to be uploaded.
     * @param signatureFile the Signature file to be uploaded.
     * @throws IOException if an I/O error occurs while reading the file data.
     */
    public void saveOrUpdateDocuments(int accountNumber, MultipartFile aadhaarFile, MultipartFile panFile, MultipartFile bankStatementFile) throws IOException {
        logger.info("Saving or updating documents for account number: " + accountNumber);
        
        // Save or update each document type
        saveOrUpdateDocument(accountNumber, "AADHAAR", aadhaarFile);
        saveOrUpdateDocument(accountNumber, "PAN", panFile);
        saveOrUpdateDocument(accountNumber, "BANK_STATEMENT", bankStatementFile);
    }

    /**
     * Saves or updates a single document.
     * @param document the document to save or update.
     */
    public void saveOrUpdateDocument(Documents document) {
        logger.info("Saving or updating document: " + document.getDocumentName());
        documentsRepo.save(document);
    }

    
    /**
     * Saves or updates a single document based on account number, document type, and file data.
     * @param accountNumber the account number to associate the document with.
     * @param documentType the type of document (e.g., Aadhaar, PAN, Signature).
     * @param file the file data for the document.
     * @throws IOException if an I/O error occurs while reading the file data.
     */
    public void saveOrUpdateDocument(int accountNumber, String documentType, MultipartFile file) throws IOException {
        logger.info("Saving or updating document of type: " + documentType + " for account number: " + accountNumber);
        Optional<Documents> existingDocument = documentsRepo.findByAccountNumberAndDocumentName(accountNumber, documentType);

        if (existingDocument.isPresent()) {
            Documents document = existingDocument.get();
            document.setFileData(file.getBytes());
            document.setFileType(file.getContentType());
            document.setDocumentInfo(documentType + " updated");
            documentsRepo.save(document);
            logger.info("Updated existing document: " + documentType);
        } else {
            Documents newDocument = new Documents();
            newDocument.setAccountNumber(accountNumber);
            newDocument.setDocumentName(documentType);
            newDocument.setDocumentInfo(documentType + " uploaded");
            newDocument.setFileData(file.getBytes());
            newDocument.setFileType(file.getContentType());
            newDocument.setVerificationStatus(Constants.PENDING);
            documentsRepo.save(newDocument);
            logger.info("Inserted new document: " + documentType);
        }
    }

    /**
     * Retrieves documents by account number.
     * @param accountNumber the account number to fetch documents for.
     * @return List of Documents associated with the given account number.
     */
    public List<Documents> getDocumentsByAccountNumber(int accountNumber) {
        logger.info("Fetching documents for account number: " + accountNumber);
        return documentsRepo.findByAccountNumber(accountNumber);
    }

    /**
     * Retrieves a document by its ID.
     * @param id the ID of the document to retrieve.
     * @return the document if found.
     * @throws DocumentNotFoundException if the document is not found.
     */
    public Optional<Documents> getDocumentById(int id) {
        logger.info("Fetching document with ID: " + id);
        Optional<Documents> document = documentsRepo.findById(id);
        if (document.isEmpty()) {
            logger.error("Document not found with ID: " + id);
            throw new DocumentNotFoundException("Document not found with ID: " + id);
        }
        logger.info("Document found with ID: " + id);
        return document;
    }
}
