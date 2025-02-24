package com.example.demo.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.example.demo.config.JwtService;
import com.example.demo.exception.EmailSendingException;
import com.example.demo.model.AuthRequest;
import com.example.demo.model.SignUp;

import com.example.demo.service.SignUpDataDetails;
import com.example.demo.service.SignUpService;

@RestController
@RequestMapping(value="/signup")
@CrossOrigin(origins = "*")
public class SignUpController {

    private final SignUpService signupService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // Constructor Injection
    public SignUpController(SignUpService signupService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.signupService = signupService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Register a new user
     * @param userInfo contains user information such as user name,email, etc.
     * @return A message indicating if user was successfully added.
     */
    @PostMapping("/addSignup")
    public String addNewUser(@RequestBody SignUp userInfo) {
        return signupService.addSignUp(userInfo);
    }

    /**
     * Authenticate user and generate a JWT token
     * @param authRequest contains user name, password, and role for authentication
     * @return A JWT token if authentication is successful
     */
    @PostMapping("/generateToken")
    public String authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        SignUpDataDetails signUpDetails = (SignUpDataDetails) signupService.loadUserByUsername(authRequest.getUsername());
        if (!signUpDetails.getAuthorities().contains(new SimpleGrantedAuthority(authRequest.getRole()))) {
            throw new BadCredentialsException("Invalid role provided!");
        }
        Optional<SignUp> optionalUserAccount = signupService.findByUserName(authRequest.getUsername());
	        if (optionalUserAccount.isEmpty()) {
            throw new UsernameNotFoundException("User not found!");
        }
	        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
 
        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(authRequest.getUsername());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid user request!");
        }
    }
    /**
     * User Profile end point - Only accessible by users with 'ROLE_USER'
     * @return A message for the user profile
     */
    @GetMapping("/user/userProfile")
    @PreAuthorize("hasAuthority('USER')")
    public String userProfile() {
        return "Welcome to User Profile";
    }

    /**
     * admin Profile end point - Only accessible by users with 'ROLE_ADMIN'
     * @return A message for the admin profile
     */
    @GetMapping("/admin/adminProfile")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String adminProfile() {
        return "Welcome to Admin Profile";
    }

    /**
     * Fetch all user sign-ups
     * @return A list of all user sign-ups
     */
    @GetMapping(value="/showSignup")
    public List<SignUp> showSignUp() {
        return signupService.showSignUp();
    }

  


    /**
     * Forgot Password: Generate OTP and send via email
     * @param email User's email to send OTP to
     * @return A message indicating that the OTP was sent
     * @throws EmailSendingException 
     */
    @PostMapping("/forgotpassword")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        try {
            String responseMessage = signupService.forgotPassword(email);
            return ResponseEntity.ok(responseMessage);
        } catch (EmailSendingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending email: " + e.getMessage());
        }
    }

    /**
     * Validate OTP
     * @param email User's email
     * @param otp OTP to be validated
     * @return A response indicating whether OTP is valid or invalid
     */
    @PostMapping("/validateotp")
    public ResponseEntity<String> validateOtp(@RequestParam String email, @RequestParam String otp) {
        return signupService.validateOtp(email, otp)
                ? ResponseEntity.ok("OTP is valid")
                : ResponseEntity.badRequest().body("Invalid OTP");
    }

    /**
     * Reset Password
     * @param email User's email
     * @param newPassword New password to reset
     * @return A response indicating the result of the password reset
     */
    @PostMapping("/resetpassword")
    public ResponseEntity<String> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        return ResponseEntity.ok(signupService.resetPassword(email, newPassword));
    }

    /**
     * Update existing user's details by user name
     * @param username The user's user name
     * @param signup The user details to update
     * @return A response indicating the result of the update
     */
    @PutMapping(value="/updateSignup/{username}")
    public ResponseEntity<String> updateSignup(@PathVariable String username, @RequestBody SignUp signup) {
        SignUp existingUser = signupService.searchSignUpByUsername(username);

        if (existingUser != null) {
            existingUser.setEmail(signup.getEmail());
            existingUser.setMobileNo(signup.getMobileNo());

            signupService.updateSignUp(existingUser);
            return ResponseEntity.ok("User details updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    /**
     * Delete user by ID
     * @param Id The user ID to delete
     */


    /**
     * Search user by user name
     * @param username The user name to search for
     * @return The user details if found
     */
    @GetMapping("/showSignUpUserName/{username}")
    public ResponseEntity<SignUp> searchByUserName(@PathVariable String username) {
        SignUp signUp = signupService.searchSignUpByUsername(username);

        if (signUp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(signUp);
    }

    /**
     * Search user by email
     * @param email The email to search for
     * @return The user details if found
     */
    @GetMapping("/showSignUpEmail/{email}")
    public ResponseEntity<SignUp> searchByEmail(@PathVariable String email) {
        SignUp signUp = signupService.searchSignUpByEmail(email);

        if (signUp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(signUp);
    }

    /**
     * Show user details based on user name, password, and role
     * @param username The user name of the user
     * @param password The password of the user
     * @param role The role of the user
     * @return The user details if matched
     */
    @GetMapping(value = "/showSignupDetails/{username}/{password}/{role}")
    public SignUp showSignupDetails(@PathVariable String username, @PathVariable String password, @PathVariable String role) {
        return signupService.searchByUsernamePasswordAndRole(username, password, role);
    }

    /**
     * Sign in with user name and role
     * @param username The user name of the user
     * @param Role The role of the user
     * @return A response indicating success or failure
     */
    @GetMapping(value="/signinAccount/{username}/{role}")
    public String signin(@PathVariable String username, @PathVariable String role) {
        return signupService.signin(username, role);
    }

    /**
     * Login with user name and role
     * @param username The user name of the user
     * @param Role The role of the user
     * @return A response indicating success or failure
     */
    @GetMapping(value="/existingAccount/{username}/{role}")
    public String login(@PathVariable String username, @PathVariable String role) {
        return signupService.login(username, role);
    }
    
    
    
    
}
