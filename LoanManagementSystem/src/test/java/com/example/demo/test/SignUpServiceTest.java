package com.example.demo.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.exception.EmailSendingException;
import com.example.demo.model.SignUp;
import com.example.demo.repo.SignUpRepo;
import com.example.demo.service.EmailService;
import com.example.demo.service.OtpService;
import com.example.demo.service.SignUpService;

class SignUpServiceTest {

    @InjectMocks
    private SignUpService signUpService;

    @Mock
    private SignUpRepo signupRepo;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddSignUp() {
        SignUp signUp = new SignUp();
        signUp.setUsername("testUser");
        signUp.setPassword("testPassword");

        when(encoder.encode(signUp.getPassword())).thenReturn("encodedPassword");
        when(signupRepo.save(signUp)).thenReturn(signUp);

        String result = signUpService.addSignUp(signUp);
        assertEquals("New User Added Successfully", result);

        verify(signupRepo, times(1)).save(signUp);
    }

    @Test
    void testShowSignUp() {
        List<SignUp> mockSignUps = new ArrayList<>();
        when(signupRepo.findAll()).thenReturn(mockSignUps);

        List<SignUp> result = signUpService.showSignUp();
        assertNotNull(result);
        assertEquals(mockSignUps, result);

        verify(signupRepo, times(1)).findAll();
    }

    @Test
    void testSearchSignUpByUsername_Success() {
        SignUp mockSignUp = new SignUp();
        mockSignUp.setUsername("testUser");

        when(signupRepo.findByUsername("testUser")).thenReturn(Optional.of(mockSignUp));

        SignUp result = signUpService.searchSignUpByUsername("testUser");
        assertNotNull(result);
        assertEquals("testUser", result.getUsername());

        verify(signupRepo, times(1)).findByUsername("testUser");
    }

    @Test
    void testSearchSignUpByUsername_NotFound() {
        when(signupRepo.findByUsername("testUser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            signUpService.searchSignUpByUsername("testUser");
        });

        verify(signupRepo, times(1)).findByUsername("testUser");
    }

    @Test
    void testSearchByUsernamePasswordAndRole() {
        SignUp mockSignUp = new SignUp();
        mockSignUp.setUsername("testUser");
        mockSignUp.setPassword("testPassword");
        mockSignUp.setRole("USER");

        when(signupRepo.findByUsernameAndPasswordAndRole("testUser", "testPassword", "USER")).thenReturn(mockSignUp);

        SignUp result = signUpService.searchByUsernamePasswordAndRole("testUser", "testPassword", "USER");
        assertNotNull(result);
        assertEquals("testUser", result.getUsername());

        verify(signupRepo, times(1)).findByUsernameAndPasswordAndRole("testUser", "testPassword", "USER");
    }
    @Test
    void testForgotPassword() throws EmailSendingException {
        SignUp mockSignUp = new SignUp();
        mockSignUp.setEmail("test@example.com");
        mockSignUp.setUsername("testUser");

        when(signupRepo.findByEmail("test@example.com")).thenReturn(Optional.of(mockSignUp));
        when(otpService.generateOtp("test@example.com")).thenReturn("123456");

        String result = signUpService.forgotPassword("test@example.com");
        assertEquals("✅ OTP sent successfully to your email!", result);

        verify(otpService, times(1)).generateOtp("test@example.com");
        verify(emailService, times(1)).sendOtpEmail("test@example.com", "123456", "testUser");
    }

    @Test
    void testForgotPassword_EmailNotFound() throws EmailSendingException {
        when(signupRepo.findByEmail("test@example.com")).thenReturn(Optional.empty());

        String result = signUpService.forgotPassword("test@example.com");
        assertEquals("Email not found", result);

        verify(signupRepo, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testResetPassword_Success() {
        SignUp mockSignUp = new SignUp();
        mockSignUp.setEmail("test@example.com");
        mockSignUp.setPassword("oldPassword");

        when(signupRepo.findByEmail("test@example.com")).thenReturn(Optional.of(mockSignUp));
        when(encoder.encode("newPassword")).thenReturn("encodedNewPassword");

        String result = signUpService.resetPassword("test@example.com", "newPassword");
        assertEquals("Password updated successfully", result);

        verify(signupRepo, times(1)).save(mockSignUp);
        verify(otpService, times(1)).clearOtp("test@example.com");
    }

    @Test
    void testResetPassword_EmailNotFound() {
        when(signupRepo.findByEmail("test@example.com")).thenReturn(Optional.empty());

        String result = signUpService.resetPassword("test@example.com", "newPassword");
        assertEquals("Email not found", result);

        verify(signupRepo, times(1)).findByEmail("test@example.com");
    }
    

        @Test
         void testSignin() {
            String username = "testuser";
            String role = "user";

            when(signupRepo.countByUsernameAndRole(username, role)).thenReturn(1L);

            String result = signUpService.signin(username, role);

            assertEquals("1", result);
            verify(signupRepo, times(1)).countByUsernameAndRole(username, role);
        }

        @Test
         void testCountByUsernameAndRole() {
            String username = "testuser";
            String role = "user";

            when(signupRepo.countByUsernameAndRole(username, role)).thenReturn(1L);

            Long count = signUpService.countByUsernameAndRole(username, role);

            assertEquals(1L, count);
            verify(signupRepo, times(1)).countByUsernameAndRole(username, role);
        }

        @Test
         void testFindByUserName() {
            String username = "testuser";
            SignUp signUp = new SignUp();
            signUp.setUsername(username);

            when(signupRepo.findByUsername(username)).thenReturn(Optional.of(signUp));

            Optional<SignUp> found = signUpService.findByUserName(username);

            assertTrue(found.isPresent());
            assertEquals(username, found.get().getUsername());
            verify(signupRepo, times(1)).findByUsername(username);
        }

        @Test
         void testLogin() {
            String username = "testuser";
            String role = "user";

            when(signupRepo.countByUsernameAndRole(username, role)).thenReturn(1L);

            String result = signUpService.login(username, role);

            assertEquals("1", result);
            verify(signupRepo, times(1)).countByUsernameAndRole(username, role);
        }
        

            @Test
             void testSearchSignUpByEmail() {
                String email = "test@example.com";
                SignUp signUp = new SignUp();
                signUp.setEmail(email);

                when(signupRepo.searchByEmail(email)).thenReturn(signUp);

                SignUp result = signUpService.searchSignUpByEmail(email);

                assertNotNull(result);
                assertEquals(email, result.getEmail());
                verify(signupRepo, times(1)).searchByEmail(email);
            }


            @Test
             void testSearch() {
                String username = "testuser";
                String role = "user";

                when(signupRepo.countByUsernameAndRole(username, role)).thenReturn(1L);

                String result = signUpService.search(username, role);

                assertEquals("1", result);
                verify(signupRepo, times(1)).countByUsernameAndRole(username, role);
            }

            @Test
             void testValidateOtp() {
                String email = "test@example.com";
                String otp = "123456";

                when(otpService.validateOtp(email, otp)).thenReturn(true);

                boolean result = signUpService.validateOtp(email, otp);

                assertTrue(result);
                verify(otpService, times(1)).validateOtp(email, otp);
            }
           
               @Test
                 void testUpdateSignUp() {
                    // Arrange
                    SignUp signUp = new SignUp();
                    signUp.setUsername("testuser");

                    // Mock the save method
                    when(signupRepo.save(signUp)).thenReturn(signUp);

                    // Act
                    signUpService.updateSignUp(signUp);

                    // Assert
                    verify(signupRepo, times(1)).save(signUp);  // Ensure save is called once
                }
            

                @Test
                 void testLoadUserByUsername_UserFound() {
                    // Arrange
                    String username = "testuser";
                    SignUp signUp = new SignUp();
                    signUp.setUsername(username);
                    signUp.setPassword("password");

                    when(signupRepo.findByUsername(username)).thenReturn(Optional.of(signUp));

                    // Act
                    UserDetails userDetails = signUpService.loadUserByUsername(username);

                    // Assert
                    assertNotNull(userDetails);
                    assertEquals(username, userDetails.getUsername());
                    assertEquals("password", userDetails.getPassword());
                    verify(signupRepo, times(1)).findByUsername(username);
                }

                @Test
                 void testLoadUserByUsername_UserNotFound() {
                    // Arrange
                    String username = "nonexistentuser";
                    when(signupRepo.findByUsername(username)).thenReturn(Optional.empty());

                    // Act & Assert
                    assertThrows(UsernameNotFoundException.class, () -> {
                        signUpService.loadUserByUsername(username);
                    });

                    verify(signupRepo, times(1)).findByUsername(username);
                }
            }


      

    


