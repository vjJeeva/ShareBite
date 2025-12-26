package com.foodshare.app.sharebite.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private final String senderEmail = "noreply@sharebite.com";
    private final String recipientEmail = "user@example.com";
    private final String otpCode = "123456";

    @BeforeEach
    void setUp() {
        // Use ReflectionTestUtils to set the private @Value field
        ReflectionTestUtils.setField(emailService, "senderEmail", senderEmail);
    }

    @Test
    void sendVerificationOtp_Success() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendVerificationOtp(recipientEmail, otpCode);

        // Assert
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals(senderEmail, sentMessage.getFrom());
        assertEquals(recipientEmail, sentMessage.getTo()[0]);
        assertTrue(sentMessage.getSubject().contains("OTP"));
        assertTrue(sentMessage.getText().contains(otpCode));
    }

    @Test
    void sendVerificationOtp_HandlesExceptionGracefully() {
        // Arrange
        doThrow(new RuntimeException("SMTP Server Down")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        // We assert doesNotThrow because the service catches the exception and logs it
        assertDoesNotThrow(() -> {
            emailService.sendVerificationOtp(recipientEmail, otpCode);
        });

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}