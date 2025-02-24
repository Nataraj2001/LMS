package com.example.demo.service;
 
import org.apache.log4j.Logger;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.demo.model.SignUp;
 
 
public class SignUpDataDetails implements UserDetails {

    
    
    private static Logger logger = Logger.getLogger(SignUpDataDetails.class);
 
    private String username;
    private String password;
    private List<GrantedAuthority> authorities;
 
    /**
     * Constructor to initialize user details from a SignUp model.
     * @param signUpInfo The SignUp object containing user details like username, password, and roles.
     */
    public SignUpDataDetails(SignUp signUpInfo) {
        logger.info("Initializing SignUpDataDetails with username " +  signUpInfo.getUsername());
        username = signUpInfo.getUsername();
        password = signUpInfo.getPassword();
        authorities = Arrays.stream(signUpInfo.getRole().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        
        logger.info("Assigned roles to the user " + authorities);
    }
 
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        logger.debug("Fetching authorities for username " + username);
        return authorities;
    }
 
    @Override
    public String getPassword() {
        logger.debug("Fetching password for username " + username);
        return password;
    }
 
    @Override
    public String getUsername() {
        logger.debug("Fetching username " + username);
        return username;
    }
 
    @Override
    public boolean isAccountNonExpired() {
        logger.debug("Checking if account for username is non-expired. " + username);
        return true;
    }
 
    @Override
    public boolean isAccountNonLocked() {
        logger.debug("Checking if account for username  is non-locked. " + username);
        return true;  
    }
 
    
    @Override
    public boolean isCredentialsNonExpired() {
        logger.debug("Checking if credentials for username are non-expired. " + username);
        return true;
    }
 
    @Override
    public boolean isEnabled() {
        logger.debug("Checking if user account for username is enabled. " + username);
        return true;  
    }
}

 