package com.example.demo.test;

import com.example.demo.exception.DocumentNotFoundException;

import com.example.demo.model.Documents;
import com.example.demo.repo.DocumentsRepo;
import com.example.demo.service.DocumentsService;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentsServiceTest {

    @InjectMocks
    private DocumentsService documentsService;

    @Mock
    private DocumentsRepo documentsRepo;

    @Mock
    private Logger logger;

    @Mock
    private MultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
   
    @Test
    void testShowDocuments() {
        // Arrange
        List<Documents> documentsList = Arrays.asList(new Documents(), new Documents());
        when(documentsRepo.findAll()).thenReturn(documentsList);

        // Act
        List<Documents> result = documentsService.showDocuments();

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void testSearchDocumentById_Found() {
        // Arrange
        Documents document = new Documents();
        when(documentsRepo.findById(1)).thenReturn(Optional.of(document));

        // Act
        Documents result = documentsService.searchDocumentById(1);

        // Assert
        assertEquals(document, result);
    }

    @Test
    void testSearchDocumentById_NotFound() {
        // Arrange
        when(documentsRepo.findById(1)).thenReturn(Optional.empty());

        // Act
        Documents result = documentsService.searchDocumentById(1);

        // Assert
        assertNull(result);
    }

    @Test
    void testDeleteDocuments() {
        // Act
        documentsService.deleteDocuments(1);

        // Assert

        verify(documentsRepo).deleteById(1);
    }

    @Test
    void testSaveOrUpdateDocuments() throws IOException {
        // Arrange
        when(multipartFile.getBytes()).thenReturn(new byte[0]);
        
        when(multipartFile.getContentType()).thenReturn("application/pdf");

        // Act
        documentsService.saveOrUpdateDocuments(123, multipartFile, multipartFile, multipartFile);

        // Assert
        verify(documentsRepo, times(3)).save(any(Documents.class));
    }

    @Test
    void testSaveOrUpdateDocument_ExistingDocument() throws IOException {
        // Arrange
        Documents document = new Documents();
        when(documentsRepo.findByAccountNumberAndDocumentName(123, "AADHAAR")).thenReturn(Optional.of(document));
        when(multipartFile.getBytes()).thenReturn(new byte[0]);
        when(multipartFile.getContentType()).thenReturn("application/pdf");

        // Act
        documentsService.saveOrUpdateDocument(123, "AADHAAR", multipartFile);

        // Assert
        assertEquals("AADHAAR updated", document.getDocumentInfo());
    }

    @Test
    void testSaveOrUpdateDocument_NewDocument() throws IOException {
        // Arrange
        when(documentsRepo.findByAccountNumberAndDocumentName(123, "AADHAAR")).thenReturn(Optional.empty());
        when(multipartFile.getBytes()).thenReturn(new byte[0]);
        when(multipartFile.getContentType()).thenReturn("application/pdf");

        // Act
        documentsService.saveOrUpdateDocument(123, "AADHAAR", multipartFile);

        // Assert
        verify(documentsRepo).save(any(Documents.class));
    }

    @Test
    void testGetDocumentsByAccountNumber() {
        // Arrange
        List<Documents> documentsList = Arrays.asList(new Documents(), new Documents());
        when(documentsRepo.findByAccountNumber(123)).thenReturn(documentsList);

        // Act
        List<Documents> result = documentsService.getDocumentsByAccountNumber(123);

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void testGetDocumentById_Found() {
        // Arrange
        Documents document = new Documents();
        when(documentsRepo.findById(1)).thenReturn(Optional.of(document));

        // Act
        Optional<Documents> result = documentsService.getDocumentById(1);

        // Assert

        assertTrue(result.isPresent());
    }
   
        @Test
        void testSaveOrUpdateDocument() {
            // Arrange
            Documents document = new Documents();
            document.setDocumentName("AADHAAR");
            
            // Act
            documentsService.saveOrUpdateDocument(document);

            // Assert
            // Verify that the save method is called once with the correct argument
            verify(documentsRepo, times(1)).save(document);

            // Verify logger message or other side effects if needed
        }

    @Test
    void testGetDocumentById_NotFound() {
        // Arrange
        when(documentsRepo.findById(1)).thenReturn(Optional.empty());

        
        // Act & Assert
        DocumentNotFoundException thrown = assertThrows(DocumentNotFoundException.class,
                () -> documentsService.getDocumentById(1));

        assertEquals("Document not found with ID: 1", thrown.getMessage());
    }
}
