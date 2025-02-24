package com.example.demo.service;
 
import java.util.Date;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
import com.example.demo.exception.EmailException;
import com.example.demo.exception.EmailSendingException;
import com.example.demo.exception.LoanRepaymentFailedException;
import com.example.demo.model.AccountDetails;
 
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
 
/**
* @author Pashya.Reddy
* @param userName
* @return
*/
 
@Service
public class EmailService {
 
	private final JavaMailSender mailSender;
	// Define a constant for the "Dear" greeting
	private static final String GREETING_TEMPLATE = "<h2>Dear ";
 
	// Constructor injection for JavaMailSender
	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}
 
	public void sendOtpEmail(String to, String otp, String userName) throws EmailSendingException {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setTo(to);
			helper.setSubject("VSPRINTS Bank - OTP for Password Reset");
 
			String emailContent = GREETING_TEMPLATE + userName + ",</h2>"
					+ "<p>You have requested to reset your password.</p>"
					+ "<p><b>Your One-Time Password (OTP):</b> <span style='font-size:18px; color:blue;'>" + otp
					+ "</span></p>"
					+ "<p>This OTP is valid for only 10 minutes. Please do not share this OTP with anyone.</p>"
					+ "<p>If you did not request a password reset, please ignore this email or contact our support team immediately.</p>"
					+ "<br/><p>Regards,</p>" + "<p><b>VSPRINTS Bank Support Team</b></p>"
					+ "<p>Customer Support: support@vsprintsbank.com</p>";
 
			helper.setText(emailContent, true); // Enable HTML content
			mailSender.send(message);
		} catch (MessagingException e) {
			throw new EmailSendingException("Error sending OTP email to " + to, e);
		}
	}
 
	public void sendAccountCreationEmail(String toEmail, String userName, int accountNumber)
			throws EmailSendingException {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setTo(toEmail);
			helper.setSubject("Welcome to VSPRINTS Bank - Your Account is Successfully Created!");
 
			String emailContent = GREETING_TEMPLATE + userName + ",</h2>"
					+ "<p>Welcome to <b>VSPRINTS Bank</b>! We are delighted to have you as our valued customer.</p>"
					+ "<p><b>Your bank account has been successfully created.</b></p>"
					+ "<p><b>Account Number:</b> <span style='font-size:18px; color:blue;'>" + accountNumber
					+ "</span></p>" + "<p>You can now access your account and manage your finances with ease.</p>"
					+ "<p>For security reasons, please do not share your account details with anyone.</p>"
					+ "<p>If you have any questions or need assistance, feel free to contact our support team.</p>"
					+ "<br/><p>Best Regards,</p>" + "<p><b>VSPRINTS Bank Team</b></p>"
					+ "<p>📧 Customer Support: support@vsprintsbank.com</p>"
					+ "<p>📞 Contact Number: +91-9876543210</p>";
 
			helper.setText(emailContent, true); // Enable HTML content
			mailSender.send(message);
		} catch (MessagingException e) {
			throw new EmailSendingException("Error sending account creation email to " + toEmail, e);
		}
	}
 
	//  New method: Send email after loan application
	public void sendLoanApplicationEmail(String recipientEmail, String userName, String loanType, double amount)
			throws EmailSendingException {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setTo(recipientEmail);
			helper.setSubject("Loan Application Received - VSPRINTS Bank");
 
			String emailContent = GREETING_TEMPLATE + userName + ",</h2>" + "<p>We have received your loan application.</p>"
					+ "<p><b>Loan Type:</b> <span style='font-size:18px; color:blue;'>" + loanType + "</span></p>"
					+ "<p><b>Loan Amount Requested:</b> <span style='font-size:18px; color:blue;'>₹" + amount
					+ "</span></p>"
					+ "<p>We are currently reviewing your application. You will be notified once it is approved.</p>"
					+ "<br/><p>Best Regards,</p>" + "<p><b>VSPRINTS Bank Team</b></p>"
					+ "<p>📧 Customer Support: support@vsprintsbank.com</p>"
					+ "<p>📞 Contact Number: +91-9876543210</p>";
 
			helper.setText(emailContent, true);
			mailSender.send(message);
		} catch (MessagingException e) {
			throw new EmailSendingException("Error sending loan application email to " + recipientEmail, e);
		}
	}
 
	public void sendLoanStatusEmail(String toEmail, String userName, String loanType, double loanAmount, boolean isApproved) {
	    try {
	        MimeMessage message = mailSender.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(message, true);
	        helper.setTo(toEmail);
 
	        String subject;
	        String emailContent;
 
	        if (isApproved) {
	            // Loan Approved Email
	            subject = "Loan Approval Notification - VSPRINTS Bank";
	            emailContent = "<h2> Hlo " + userName + ",</h2>"
	                    + "<p>We are pleased to inform you that your <b><span style='font-size:18px; color:blue;'>"
	                    + loanType + "</span></b> loan application has been <b>approved</b>.</p>"
	                    + "<p><b>Loan Amount:</b> ₹<span style='font-size:18px; color:blue;'>" + loanAmount + "</span></p>"
	                    + "<p>The sanctioned amount has been credited to your account.</p>";
	        } else {
	            // Loan Rejected Email
	            subject = "Loan Rejection Notification - VSPRINTS Bank";
	            emailContent = "<h2> Hi " + userName + ",</h2>"
	                    + "<p>We regret to inform you that your <b><span style='font-size:18px; color:red;'>"
	                    + loanType + "</span></b> loan application has been <b>rejected</b>.</p>"
	                    + "<p><b>Loan Amount Requested:</b> ₹<span style='font-size:18px; color:red;'>" + loanAmount + "</span></p>"
	                    + "<p>The reason for rejection could be insufficient credit score, incomplete documentation, or other eligibility criteria.</p>"
	                    + "<p>Please contact our support team for further assistance.</p>";
	        }
 
	        // Common footer
	        emailContent += "<br/><p>Best Regards,</p>"
	                + "<p><b>VSPRINTS Bank Team</b></p>"
	                + "<p>📧 Customer Support: support@vsprintsbank.com</p>"
	                + "<p>📞 Contact Number: +91-9876543210</p>";
 
	        helper.setSubject(subject);
	        helper.setText(emailContent, true);
 
	        mailSender.send(message);
	    } catch (MessagingException e) {
	        throw new EmailException("Error sending loan status email" + e);
	    }
	}
 
	public void sendTransactionDebitEmail(String toEmail, int accountNumber, double amount, double remainingBalance,
			int toAccNo) throws EmailSendingException {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setTo(toEmail);
			helper.setSubject("Transaction Alert - Amount Debited");
 
			String emailContent = GREETING_TEMPLATE + accountNumber + ",</h2>" + "<p>Your account has been debited.</p>"
					+ "<p><b>Amount Debited:</b> ₹<span style='color:red;'>" + amount + "</span></p>"
					+ "<p><b>Remaining Balance:</b> ₹" + remainingBalance + "</p>"
					+ "<p><b>Transferred To Account:</b> " + toAccNo + "</p>" + "<p><b>Transaction Date:</b> "
					+ new Date() + "</p>" + "<br/><p>Regards,</p>" + "<p><b>VSPRINTS Bank Support Team</b></p>";
 
			helper.setText(emailContent, true);
			mailSender.send(message);
		} catch (MessagingException e) {
			throw new EmailSendingException("Error sending debit email to account: " + accountNumber, e);
		}
	}
 
	public void sendTransactionCreditEmail(String toEmail, int toAccNo, double amount, double updatedBalance,
			AccountDetails fromAccount) throws EmailSendingException {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setTo(toEmail);
			helper.setSubject("Transaction Alert - Amount Credited");
 
			String emailContent = GREETING_TEMPLATE + toAccNo + ",</h2>" + "<p>Your account has been credited.</p>"
					+ "<p><b>Amount Credited:</b> ₹<span style='color:green;'>" + amount + "</span></p>"
					+ "<p><b>Updated Balance:</b> ₹" + updatedBalance + "</p>" + "<p><b>Received From Account:</b> "
					+ fromAccount + "</p>" + "<p><b>Transaction Date:</b> " + new Date() + "</p>"
					+ "<br/><p>Regards,</p>" + "<p><b>VSPRINTS Bank Support Team</b></p>";
 
			helper.setText(emailContent, true);
			mailSender.send(message);
		} catch (MessagingException e) {
			throw new EmailSendingException("Error sending credit email to account: " + toAccNo, e);
		}
	}
 
	public void sendLoanRepaymentEmail(String toEmail, String firstName, int loanId, double amountPaid,
			double remainingDue, Date paymentDate, String paymentMode) throws EmailSendingException {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setTo(toEmail);
			helper.setSubject("Loan Repayment Successful - Loan ID: " + loanId);
 
			String emailContent = GREETING_TEMPLATE + firstName + ",</h2>"
					+ "<p>We are pleased to inform you that your loan repayment has been successfully processed.</p>"
					+ "<p><b>Loan ID:</b> " + loanId + "</p>" + "<p><b>Payment Amount:</b> ₹" + amountPaid + "</p>"
					+ "<p><b>Remaining Due Amount:</b> ₹" + remainingDue + "</p>" + "<p><b>Payment Date:</b> "
					+ paymentDate + "</p>" + "<p><b>Payment Mode:</b> " + paymentMode + "</p>"
					+ "<p>Thank you for your prompt payment. If you have any questions, feel free to contact our support team.</p>"
					+ "<br/><p>Best Regards,</p>" + "<p><b>VSPRINTS Bank Loan Department</b></p>"
					+ "<p>📧 Customer Support: support@vsprintsbank.com</p>"
					+ "<p>📞 Contact Number: +91-9876543210</p>";
 
			helper.setText(emailContent, true);
			mailSender.send(message);
 
		} catch (MessagingException e) {
			throw new EmailSendingException("Error sending loan repayment email to " + toEmail, e);
		}
	}
 
	public void sendLoanPreclosureEmail(String toEmail, String borrowerName, int loanId, double preclosureAmount,
			Date preclosureDate, String paymentMode) throws EmailSendingException {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setTo(toEmail);
			helper.setSubject("Loan Preclosure Confirmation - Loan ID: " + loanId);
 
			String emailContent = GREETING_TEMPLATE + borrowerName + ",</h2>"
					+ "<p>We are pleased to inform you that your loan preclosure request has been successfully processed.</p>"
					+ "<p><b>Loan ID:</b> " + loanId + "</p>" + "<p><b>Preclosure Amount Paid:</b> ₹" + preclosureAmount
					+ "</p>" + "<p><b>Preclosure Date:</b> " + preclosureDate + "</p>" + "<p><b>Payment Mode:</b> "
					+ paymentMode + "</p>"
					+ "<p>Your loan has been successfully closed. If you need any further assistance, feel free to contact our support team.</p>"
					+ "<br/><p>Best Regards,</p>" + "<p><b>VSPRINTS Bank Loan Department</b></p>"
					+ "<p>📧 Customer Support: support@vsprintsbank.com</p>"
					+ "<p>📞 Contact Number: +91-9876543210</p>";
 
			helper.setText(emailContent, true);
			mailSender.send(message);
 
		} catch (MessagingException e) {
			throw new EmailSendingException("Error sending loan preclosure email to " + toEmail, e);
		}
	}
 
	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
//	public void sendLoanRepaymentReminderEmail(String toEmail, String borrowerName, int loanId, double amountDue, Date dueDate) throws EmailSendingException, MessagingException {
//	    try {
//	        // Prepare the email message
//	        MimeMessage message = mailSender.createMimeMessage();
//	        MimeMessageHelper helper = new MimeMessageHelper(message, true);
//	        helper.setTo(toEmail);
//	        helper.setSubject("Upcoming Loan Repayment Reminder - Loan ID: " + loanId);
//
//	        // Construct the email content with the appropriate templates
//	        String emailContent = GREETING_TEMPLATE + borrowerName + ",</h2>"
//	                + "<p>This is a friendly reminder that your upcoming loan repayment is due soon.</p>"
//	                + "<p><b>Loan ID:</b> " + loanId + "</p>"
//	                + "<p><b>Amount Due:</b> ₹" + amountDue + "</p>"
//	                + "<p><b>Due Date:</b> " + dueDate + "</p>"
//	                + "<p>Please ensure timely payment to avoid any penalties or late fees.</p>"
//	                + "<p>If you have already made the payment, please disregard this email.</p>"
//	                + "<br/><p>Best Regards,</p>"
//	                + "<p><b>VSPRINTS Bank Loan Department</b></p>"
//	                + "<p>📧 Customer Support: support@vsprintsbank.com</p>"
//	                + "<p>📞 Contact Number: +91-9876543210</p>";
//
//	        // Set the email content as HTML
//	        helper.setText(emailContent, true);
//
//	        // Send the email
//	        mailSender.send(message);
//
//	        // Log success
//	        logger.info("📩 Repayment reminder email sent to: {} for Loan ID: {}", toEmail, loanId);
//
//	    } catch (LoanRepaymentFailedException e) {
//	        // Log the error with contextual information and rethrow the custom exception
//	        logger.error("Failed to send repayment reminder email to: {} for Loan ID: {}. Error: {}", toEmail, loanId, e.getMessage());
//
//	        // Additional handling (optional): Notify the support team or initiate a fallback action.
//	        // e.g., fallback to another email provider, retry logic, or other measures
//
//	        // Rethrow a custom exception with meaningful context
//	        throw new EmailSendingException(String.format("Failed to send repayment reminder email to %s for Loan ID %d. Error: %s", toEmail, loanId, e.getMessage()), e);
//	    }
//	}
 
 
	
 
	public void sendLoanDueReminder(String toEmail, String firstName, int loanId, double dueAmount, Date dueDate)
			throws EmailSendingException {
		try {
			// Sending email reminder
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setTo(toEmail);
			helper.setSubject("Urgent: Loan Repayment Due - Loan ID: " + loanId);
 
			String emailContent = GREETING_TEMPLATE + firstName + ",</h2>"
					+ "<p>This is a reminder that your loan repayment is due. Please make the payment at the earliest to avoid penalties.</p>"
					+ "<p><b>Loan ID:</b> " + loanId + "</p>" + "<p><b>Due Amount:</b> ₹" + dueAmount + "</p>"
					+ "<p><b>Due Date:</b> " + dueDate + "</p>"
					+ "<p>Please ensure that you clear your dues immediately to prevent further penalties and negative impact on your credit score.</p>"
					+ "<br/><p>Best Regards,</p>" + "<p><b>VSPRINTS Bank Loan Department</b></p>"
					+ "<p>📧 Customer Support: support@vsprintsbank.com</p>"
					+ "<p>📞 Contact Number: +91-9876543210</p>";
 
			helper.setText(emailContent, true);
			mailSender.send(message);
 
		} catch (MessagingException e) {
			throw new EmailSendingException("Error sending loan due reminder email to " + toEmail, e);
		}
	}
 
}