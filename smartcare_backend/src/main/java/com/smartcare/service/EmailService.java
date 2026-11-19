package com.smartcare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("SmartCare Hospital <noreply@smartcare.com>");
        message.setTo(toEmail);
        message.setSubject("Password Reset OTP - SmartCare Hospital");
        message.setText("""
            Dear User,
            
            You have requested to reset your password for your SmartCare Hospital account.
            
            Your One-Time Password (OTP) is: %s
            
            This OTP is valid for 10 minutes. Please do not share this code with anyone.
            
            If you did not request this password reset, please ignore this email.
            
            Best regards,
            SmartCare Hospital Team
            """.formatted(otp));
        
        mailSender.send(message);
    }
}
