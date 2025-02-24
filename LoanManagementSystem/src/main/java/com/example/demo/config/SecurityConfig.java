package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.service.SignUpService;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

private final JwtService jwtService;
    
    public SecurityConfig(JwtService jwtService) {
    	this.jwtService=jwtService;
    }
    
    public JwtAuthFilter jwtAuthFilter(UserDetailsService userDetailsService) {
    	return new JwtAuthFilter(jwtService, userDetailsService);
    }
    
    @Bean
    public UserDetailsService userDetailsService(SignUpService signUpService) {
    	return signUpService;
    }

   
    // Configuring HttpSecurity
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,JwtAuthFilter jwtAuthFilter) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.requestMatchers("/signup/**").permitAll())
                .authorizeHttpRequests(auth -> auth.requestMatchers("/accountdetails/**").permitAll())
                .authorizeHttpRequests(auth -> auth.requestMatchers("/banktransaction/**").permitAll())
                .authorizeHttpRequests(auth -> auth.requestMatchers("/loanapplication/**").permitAll())
                .authorizeHttpRequests(auth -> auth.requestMatchers("/documents/**").permitAll())
                .authorizeHttpRequests(auth -> auth.requestMatchers("/loanRepayments/**").permitAll())
                .authorizeHttpRequests(auth -> auth.requestMatchers("/loanSanction/**").permitAll())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider(userDetailsService(null)))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    

    // Password Encoding
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
   
    
    @Bean
    AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


}