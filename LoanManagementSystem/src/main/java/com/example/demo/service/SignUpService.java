package com.example.demo.service;

import org.apache.log4j.Logger;


import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.exception.EmailSendingException;
import com.example.demo.model.SignUp;
import com.example.demo.repo.SignUpRepo;

@Service
@Primary
public class SignUpService implements UserDetailsService {
    private static final Logger logger = Logger.getLogger(SignUpService.class);

    private final SignUpRepo signupRepo;
    private final PasswordEncoder encoder;
    private final OtpService otpService;
    private final EmailService emailService;

    // Constructor injection
    public SignUpService(SignUpRepo signupRepo, PasswordEncoder encoder, OtpService otpService, EmailService emailService) {
        this.signupRepo = signupRepo;
        this.encoder = encoder;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SignUp signUp = signupRepo.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return new SignUpDataDetails(signUp); // Return your custom SignUpDataDetails object
    }

    public String addSignUp(SignUp signUp) {
        logger.info("Adding new user " + signUp.getUsername());
        signUp.setPassword(encoder.encode(signUp.getPassword()));
        signupRepo.save(signUp);
        logger.info("New user {} added successfully " + signUp.getUsername());
        return "New User Added Successfully";
    }
    public List<SignUp> showSignUp() {
        logger.debug("Fetching all sign-ups.");
        return signupRepo.findAll();
    }


    public SignUp searchSignUpByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Searching for user by username " + username);
        return signupRepo.findByUsername(username)
                         .orElseThrow(() -> new UsernameNotFoundException("User Not Found: " + username));
    }

    public SignUp searchByUsernamePasswordAndRole(String username, String password, String role) {
        logger.debug("Searching for user by username: " + username + " role " + role);
        return signupRepo.findByUsernameAndPasswordAndRole(username, password, role);
    }

    public SignUp searchSignUpByEmail(String email) {
        logger.debug("Searching for user by email " + email);
        return signupRepo.searchByEmail(email);
    }

    
    public void updateSignUp(SignUp signup) {
        logger.info("Updating user details for " + signup.getUsername());
        signupRepo.save(signup);
    }

    

    public String search(String username, String role) {
        logger.debug("Counting users with username and role " + username + role);
        long count = signupRepo.countByUsernameAndRole(username, role);
        return String.valueOf(count);
    }

    public String forgotPassword(String email) throws EmailSendingException {
        logger.info("Processing forgot password for email " + email);
        Optional<SignUp> user = signupRepo.findByEmail(email);
        if (user.isEmpty()) {
            logger.warn("Email not found " + email);
            return "Email not found";
        }

        String userName = user.get().getUsername();
        String otp = otpService.generateOtp(email);
        emailService.sendOtpEmail(email, otp, userName);
        logger.info("OTP sent successfully to email " + email);
        return "✅ OTP sent successfully to your email!";
    }

    public boolean validateOtp(String email, String otp) {
        logger.debug("Validating OTP for email " + email);
        return otpService.validateOtp(email, otp);
    }

    public String resetPassword(String email, String newPassword) {
        logger.info("Resetting password for email " + email);
        Optional<SignUp> user = signupRepo.findByEmail(email);
        if (user.isEmpty()) {
            logger.warn("Email not found " + email);
            return "Email not found";
        }
        SignUp existingUser = user.get();
        existingUser.setPassword(encoder.encode(newPassword));
        signupRepo.save(existingUser);
        otpService.clearOtp(email);
        logger.info("Password successfully updated for email " + email);
        return "Password updated successfully";
    }

    public String signin(String username, String role) {
        logger.debug("Signing in with username and role " + username + role);
        long count = signupRepo.countByUsernameAndRole(username, role);
        return String.valueOf(count);
    }

    public Long countByUsernameAndRole(String username, String role) {
        logger.debug("Counting users with username and role " + username + role);
        return signupRepo.countByUsernameAndRole(username, role);
    }

    public Optional<SignUp> findByUserName(String username) {
        logger.debug("Finding user by username " + username);
        return signupRepo.findByUsername(username);
    }

    public String login(String username, String role) {
        logger.debug("Attempting login for username with role " + username + role);
        long count = signupRepo.countByUsernameAndRole(username, role);
        logger.debug("Found {} user(s) with username and role " + count + username + role);

        String res = "";
        res += count;

        return res;
    }
    
    
    
}
