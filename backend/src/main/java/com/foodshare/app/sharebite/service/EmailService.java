package com.foodshare.app.sharebite.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendVerificationOtp(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(to);
            message.setSubject("ShareBite: Email Verification Code (OTP)");
            message.setText("Dear User,\n\n"
                    + "Thank you for registering with ShareBite! \n\n"
                    + "Your One-Time Verification Code (OTP) is: " + otp + "\n\n"
                    + "This code is valid for 5 minutes. Please use it immediately to complete your registration.\n\n"
                    + "If you did not request this code, please ignore this email.\n\n"
                    + "Happy Sharing,\n"
                    + "The ShareBite Team");

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error sending OTP email to " + to + ": " + e.getMessage());
        }
    }
}