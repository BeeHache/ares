package net.blackhacker.ares.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = EmailSenderService.class)
@ExtendWith(MockitoExtension.class)
class EmailSenderServiceTest {

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private TemplateEngine templateEngine;

    @MockitoBean
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailSenderService emailSenderService;

    @Test
    void sendEmail_shouldProcessTemplateAndSendMessage() {
        // Arrange
        String to = "test@example.com";
        String from = "noreply@ares.com";
        String subject = "Test Subject";
        String template = "test-template";
        String[] args = {"key1", "value1", "key2", "value2"};
        String processedContent = "<html>Processed Content</html>";

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(processedContent);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));

        // Act
        emailSenderService.sendEmail(to, from, subject, template, args);

        // Assert
        // Verify TemplateEngine interaction
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(anyString(), contextCaptor.capture());
        
        Context capturedContext = contextCaptor.getValue();
        assertEquals("value1", capturedContext.getVariable("key1"));
        assertEquals("value2", capturedContext.getVariable("key2"));

        // Verify JavaMailSender interaction
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender).send(any(MimeMessage.class));
    }
}
