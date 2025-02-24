package com.example.demo.test;


import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.demo.controller.LoanApplicationController;
import com.example.demo.exception.LoanApplicationNotFoundException;
import com.example.demo.exception.LoanApprovalException;
import com.example.demo.exception.LoanRejectionException;
import com.example.demo.model.LoanApplication;
import com.example.demo.service.LoanApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;

class LoanApplicationControllerTest {
    private MockMvc mockMvc;

    @Mock
    private LoanApplicationService loanApplicationService;

    @InjectMocks
    private LoanApplicationController loanApplicationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(loanApplicationController).build();
    }

    @Test
    void testShowApplications() throws Exception {
        LoanApplication loanApp1 = new LoanApplication(1, 1001, 0, "Personal", null, 50000, "Pending", 6.5, 2, 0);
        LoanApplication loanApp2 = new LoanApplication(2, 1002, 0, "Home", null, 200000, "Approved", 5.5, 15, 0);
        List<LoanApplication> applications = Arrays.asList(loanApp1, loanApp2);

        when(loanApplicationService.showLoanapplication()).thenReturn(applications);

        mockMvc.perform(get("/loanapplication/showloanapplication"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].loanId").value(1))
                .andExpect(jsonPath("$[1].loanId").value(2))
                .andDo(print());
    }

    @Test
    void testSearchLoanApplicationById_Success() throws Exception {
        LoanApplication loanApp = new LoanApplication(1, 1001, 0, "Personal", null, 50000, "Pending", 6.5, 2, 0);

        when(loanApplicationService.searchLoanapplicationById(1)).thenReturn(loanApp);

        mockMvc.perform(get("/loanapplication/searchloanapplicationById/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanId").value(1))
                .andDo(print());
    }

    @Test
    void testSearchLoanApplicationById_NotFound() throws Exception {
        when(loanApplicationService.searchLoanapplicationById(1)).thenThrow(new LoanApplicationNotFoundException("Loan application not found"));

        mockMvc.perform(get("/loanapplication/searchloanapplicationById/1"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }
    

    @Test
    void testAddLoanApplication_Success() throws Exception {
        LoanApplication loanApp = new LoanApplication(3, 1003, 0, "Auto", null, 30000, "Pending", 7.0, 5, 0);

        // Mock the service call to return a success response
        when(loanApplicationService.addLoanApplication(any(LoanApplication.class)))
            .thenReturn(new ResponseEntity<>("Loan application added successfully.", HttpStatus.OK));

        // Perform the MockMvc request and verify the results
        mockMvc.perform(post("/loanapplication/addLoanapplication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(loanApp)))
                .andExpect(status().isOk())
                .andExpect(content().string("Loan application added successfully."))
                .andDo(print());
    }

    @Test
    void testAcceptLoan_Success() throws Exception {
        when(loanApplicationService.acceptLoan(1)).thenReturn(true);

        mockMvc.perform(put("/loanapplication/acceptLoan/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Loan approved and saved to LoanSanction table."))
                .andDo(print());
    }

    @Test
    void testAcceptLoan_Failure() throws Exception {
        when(loanApplicationService.acceptLoan(1)).thenThrow(new LoanApprovalException("Failed to approve loan"));

        mockMvc.perform(put("/loanapplication/acceptLoan/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to approve loan"))
                .andDo(print());
    }

    @Test
    void testRejectLoan_Success() throws Exception {
        when(loanApplicationService.rejectLoan(1)).thenReturn(true);

        mockMvc.perform(put("/loanapplication/rejectLoan/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Loan rejected and saved to LoanSanction table."))
                .andDo(print());
    }

    @Test
    void testRejectLoan_Failure() throws Exception {
        when(loanApplicationService.rejectLoan(1)).thenThrow(new LoanRejectionException("Failed to reject loan"));

        mockMvc.perform(put("/loanapplication/rejectLoan/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to reject loan"))
                .andDo(print());
    }

    @Test
    void testDeleteLoanApplication() throws Exception {
        doNothing().when(loanApplicationService).deleteLoanapplication(1);

        mockMvc.perform(delete("/loanapplication/deleteLoanapplication/1"))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
     void testSearchLoanapplicationByAccountNumber() {
        int accountNumber = 12345;
        List<LoanApplication> mockLoanApplications = new ArrayList<>();
        mockLoanApplications.add(new LoanApplication()); // Add some mock data

        // Mocking the service method
        when(loanApplicationService.searchloanApplicationByaccountNumber(accountNumber))
            .thenReturn(mockLoanApplications);

        // Call the method
        ResponseEntity<List<LoanApplication>> response = ResponseEntity.ok(loanApplicationController.searchLoanapplicationByAccountNumber(accountNumber));

        // Verify service call and assertions
        verify(loanApplicationService).searchloanApplicationByaccountNumber(accountNumber);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockLoanApplications, response.getBody());
    }
    @Test
     void testSearchLoanApplicationByApprovalStatus() {
        String approvalStatus = "APPROVED";
        
        // Mocking data
        List<LoanApplication> mockLoanApplications = new ArrayList<>();
        LoanApplication loanApplication1 = new LoanApplication();
        LoanApplication loanApplication2 = new LoanApplication();
        mockLoanApplications.add(loanApplication1);
        mockLoanApplications.add(loanApplication2);

        // Mocking service response
        when(loanApplicationService.searchLoanApplicationByApprovalStatus(approvalStatus))
            .thenReturn(mockLoanApplications);

        // Call the controller method
        List<LoanApplication> response = loanApplicationController.searchLoanApplicationByApprovalStatus(approvalStatus);

        // Verify the service method was called
        verify(loanApplicationService).searchLoanApplicationByApprovalStatus(approvalStatus);

        // Asserting the result
        assertEquals(mockLoanApplications.size(), response.size());
        assertEquals(mockLoanApplications, response);
    }
    @Test
     void testUpdateLoanapplication() {
        LoanApplication loanApplication = new LoanApplication(); // Create a mock loan application object

        // Mock the service layer method to do nothing (void method)
        doNothing().when(loanApplicationService).updateLoanapplication(loanApplication);

        // Call the controller method
        loanApplicationController.updateLoanapplication(loanApplication);

        // Verify that the service layer method was called
        verify(loanApplicationService).updateLoanapplication(loanApplication);
    }
 

    @Test
    void testAddLoanApplication_BadRequest() {
        // Create a mock loan application that will fail validation (or trigger a bad request)
        LoanApplication loanApplication = new LoanApplication();
        
        // Mock the service call to return the expected bad request message
        when(loanApplicationService.addLoanApplication(any(LoanApplication.class)))
            .thenReturn(new ResponseEntity<>("❌ Invalid loan application data", HttpStatus.BAD_REQUEST));
        
        // Call the controller method and assert the result
        ResponseEntity<String> response = loanApplicationController.addLoanapplication(loanApplication);

        // Assert that the response status is BAD REQUEST and the message is as expected
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("❌ Invalid loan application data", response.getBody());
    }


      
        @Test
         void testLoanApproval_Failure() {
            int loanId = 1;
            when(loanApplicationService.acceptLoan(loanId)).thenThrow(new LoanApprovalException("Failed to approve loan."));

            ResponseEntity<String> response = loanApplicationController.acceptLoan(loanId);

            // Assert that the response status is INTERNAL_SERVER_ERROR and the message is as expected
            assertEquals(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to approve loan."), response);
        }
        @Test
         void testLoanRejection_Failure() {
            int loanId = 1;
            when(loanApplicationService.rejectLoan(loanId)).thenThrow(new LoanRejectionException("Failed to reject loan."));

            ResponseEntity<String> response = loanApplicationController.rejectLoan(loanId);

            // Assert that the response status is INTERNAL_SERVER_ERROR and the message is as expected
            assertEquals(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to reject loan."), response);
        }
    }



