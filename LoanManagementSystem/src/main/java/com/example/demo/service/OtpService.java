package com.example.demo.service;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final Logger logger = Logger.getLogger(OtpService.class);
    private static final int OTP_EXPIRY_TIME_MINUTES = 10;

    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    // Using SecureRandom instead of Random for more secure OTP generation.
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Internal class to store OTP and its timestamp.
     */
    private static class OtpData {
        private final String otp;
        private final Instant timestamp;

        public OtpData(String otp) {
            this.otp = otp;
            this.timestamp = Instant.now();
        }

        public String getOtp() {
            return otp;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Generates a new OTP for the given email and stores it in otpStorage.
     * @param email The email for which the OTP is generated.
     * @return The generated OTP as a 6-digit string.
     */
    public String generateOtp(String email) {
        // Generate a 6-digit OTP
        String otp = String.format("%06d", secureRandom.nextInt(1000000));  // This ensures 6-digit OTP
        otpStorage.put(email, new OtpData(otp));
        logger.info("Generated OTP for email : {} OTP: " + email + otp);
        return otp;
    }

    /**
     * Validates the entered OTP for a given email.
     * @param email The email to validate OTP for.
     * @param enteredOtp The OTP entered by the user.
     * @return True if OTP is valid and not expired, false otherwise.
     */
    public boolean validateOtp(String email, String enteredOtp) {
        if (otpStorage.containsKey(email)) {
            OtpData otpData = otpStorage.get(email);
            if (otpData.getOtp().equals(enteredOtp) && !isOtpExpired(otpData)) {
                otpStorage.remove(email);
                logger.info("OTP validation successful for email: " + email);
                return true;
            }
        }
        logger.warn("OTP validation failed for email: {}. Either OTP expired or incorrect OTP entered." + email);
        return false;
    }

    /**
     * Clears the stored OTP for a given email (e.g., when OTP is no longer needed).
     * @param email The email whose OTP needs to be cleared.
     */
    public void clearOtp(String email) {
        otpStorage.remove(email);
        logger.info("OTP cleared for email: " + email);
    }

    /**
     * Checks if the OTP has expired based on the timestamp.
     * @param otpData The OTP data object containing the OTP and timestamp.
     * @return True if OTP has expired, false otherwise.
     */
    private boolean isOtpExpired(OtpData otpData) {
        boolean expired = Instant.now().isAfter(otpData.getTimestamp().plusSeconds(OTP_EXPIRY_TIME_MINUTES * 60L));
        if (expired) {
            logger.info("OTP expired for OTP: " +  otpData.getOtp());
        }
        return expired;
    }

    /**
     * Scheduled task to remove expired OTPs every minute.
     * This method will be triggered every minute to ensure expired OTPs are removed from the storage.
     */
    @Scheduled(fixedRate = 60000)
    public void removeExpiredOtps() {
        logger.info("Running scheduled task to remove expired OTPs.");
        otpStorage.entrySet().removeIf(entry -> isOtpExpired(entry.getValue()));
        logger.info("Expired OTPs removed.");
    }
}
