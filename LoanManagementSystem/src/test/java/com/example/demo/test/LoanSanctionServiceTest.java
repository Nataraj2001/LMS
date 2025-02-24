package com.example.demo.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.demo.model.LoanApplication;
import com.example.demo.model.LoanSanction;
import com.example.demo.repo.LoanApplicationRepo;
import com.example.demo.repo.LoanSanctionRepo;
import com.example.demo.service.LoanSanctionService;

class LoanSanctionServiceTest {

    @Mock
    private LoanSanctionRepo loanSanctionRepo;

    @Mock
    private LoanApplicationRepo loanApplicationRepo;
    
    @Mock
    private LoanSanction loanSanction;
    
    @Mock
    private LoanApplication loanApplication;

    @InjectMocks
    private LoanSanctionService loanSanctionService;



        @BeforeEach
         void setUp() {
            MockitoAnnotations.openMocks(this);

            loanSanction = new LoanSanction();
            loanSanction.setSanctionId(1);
            loanSanction.setLoanId(101);
            loanSanction.setInterestRate(5.5);
            
            loanApplication = new LoanApplication();
            loanApplication.setLoanId(101);
            loanApplication.setLoanAmount(100000);
            loanApplication.setTenure(10);
        }

        @Test
         void testShow() {
            List<LoanSanction> sanctions = new ArrayList<LoanSanction>();
            sanctions.add(loanSanction);

            // Mocking the behavior of loanSanctionRepo.findAll()
            when(loanSanctionRepo.findAll()).thenReturn(sanctions);

            // Mocking the behavior of loanApplicationRepo.findById()
            when(loanApplicationRepo.findById(101)).thenReturn(Optional.of(loanApplication));

            // Call the service method
            List<LoanSanction> result = loanSanctionService.show();

            // Verify the interactions and the result
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(loanSanction.getSanctionId(), result.get(0).getSanctionId());

            // Verify that the method calculateAndSetMonthlyInstallment was called once
            verify(loanSanctionRepo, times(1)).findAll();
            verify(loanApplicationRepo, times(1)).findById(101);
        }

        @Test
        void testCalculateAndSetMonthlyInstallment() {
            // Mocking the behavior of loanApplicationRepo.findById()
            when(loanApplicationRepo.findById(loanSanction.getLoanId())).thenReturn(Optional.of(loanApplication));

            // Call the method to calculate monthly installment
            loanSanctionService.calculateAndSetMonthlyInstallment(loanSanction);

            // Verify the monthly installment calculation logic
            assertTrue(loanSanction.getMonthlyInstallmentsAmount() > 0, 
                "The monthly installment amount should be greater than 0");
        }


        @Test
         void testCalculateAndSetMonthlyInstallmentWhenLoanApplicationNotFound() {
            // Mocking the behavior of loanApplicationRepo.findById() to return empty
            when(loanApplicationRepo.findById(loanSanction.getLoanId())).thenReturn(Optional.empty());

            // Call the method to calculate monthly installment
            loanSanctionService.calculateAndSetMonthlyInstallment(loanSanction);

            // Verify the monthly installment is set to 0 when loan application is not found
            assertEquals(0.0, loanSanction.getMonthlyInstallmentsAmount(), 0);
        }

        @Test
         void testCalculateAndSetMonthlyInstallmentWhenInvalidData() {
            // Set invalid data in loan application
            loanApplication.setLoanAmount(0);
            loanApplication.setTenure(0);
            loanApplication.setLoanAmount(0);

            // Mocking the behavior of loanApplicationRepo.findById() to return loan application
            when(loanApplicationRepo.findById(loanSanction.getLoanId())).thenReturn(Optional.of(loanApplication));

            // Call the method to calculate monthly installment
            loanSanctionService.calculateAndSetMonthlyInstallment(loanSanction);

            // Verify the monthly installment is set to 0 due to invalid loan data
            assertEquals(0.0, loanSanction.getMonthlyInstallmentsAmount(), 0);
        }

 

        @Test
        void testSearchById_Found() {
            LoanSanction loanSanctionData = new LoanSanction();  // Renamed the variable
            loanSanctionData.setSanctionId(1);
            loanSanctionData.setLoanId(100);

            LoanApplication loanApp = new LoanApplication();  // Renamed the local variable
            loanApp.setLoanAmount(50000);
            loanApp.setTenure(10);

            when(loanSanctionRepo.findById(1)).thenReturn(Optional.of(loanSanctionData));
            when(loanApplicationRepo.findById(100)).thenReturn(Optional.of(loanApp));

            LoanSanction result = loanSanctionService.searchById(1);

            assertNotNull(result);
            assertEquals(1, result.getSanctionId());
            verify(loanSanctionRepo).findById(1);
            verify(loanApplicationRepo).findById(100);
        }


    @Test
    void testSearchById_NotFound() {
        when(loanSanctionRepo.findById(1)).thenReturn(Optional.empty());

        LoanSanction result = loanSanctionService.searchById(1);

        assertNull(result);
        verify(loanSanctionRepo).findById(1);
    }

    @Test
    void testSearchByLoanId_Found() {
        LoanSanction loanSanctionData = new LoanSanction();  // Renamed the variable
        loanSanctionData.setLoanId(100);

        LoanApplication loanApplicationData = new LoanApplication();  // Renamed the variable
        loanApplicationData.setLoanAmount(50000);
        loanApplicationData.setTenure(10);

        when(loanSanctionRepo.findByLoanId(100)).thenReturn(loanSanctionData);
        when(loanApplicationRepo.findById(100)).thenReturn(Optional.of(loanApplicationData));

        LoanSanction result = loanSanctionService.searchByLoanId(100);

        assertNotNull(result);
        verify(loanSanctionRepo).findByLoanId(100);
        verify(loanApplicationRepo).findById(100);
    }



    @Test
    void testSearchByLoanId_NotFound() {
        when(loanSanctionRepo.findByLoanId(100)).thenReturn(null);

        LoanSanction result = loanSanctionService.searchByLoanId(100);

        assertNull(result);
        verify(loanSanctionRepo).findByLoanId(100);
    }

    @Test
    void testSearchBySanctionStatus() {
        LoanSanction sanction1 = new LoanSanction();
        sanction1.setSanctionStatus("APPROVED");
        LoanSanction sanction2 = new LoanSanction();
        sanction2.setSanctionStatus("APPROVED");

        when(loanSanctionRepo.findBySanctionStatus("APPROVED"))
                .thenReturn(Arrays.asList(sanction1, sanction2));

        List<LoanSanction> result = loanSanctionService.searchBySanctionStatus("APPROVED");

        assertEquals(2, result.size());
        verify(loanSanctionRepo).findBySanctionStatus("APPROVED");
    }
}
