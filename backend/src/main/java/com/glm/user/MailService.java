package com.glm.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailer;
    private final String baseUrl;

    public MailService(JavaMailSender mailer, @Value("${app.base-url}") String baseUrl) {
        this.mailer  = mailer;
        this.baseUrl = baseUrl;
    }

    @Async
    public void sendVerifyEmail(String to, String token) {
        String link = baseUrl + "/verify-email?token=" + token;
        send(to, "Verify your Green Lifestyle Market email",
             "Click the link below to verify your email address:\n\n" + link
             + "\n\nLink expires in 1 hour.");
    }

    @Async
    public void sendPasswordReset(String to, String token) {
        String link = baseUrl + "/reset-password?token=" + token;
        send(to, "Reset your Green Lifestyle Market password",
             "Click the link below to reset your password:\n\n" + link
             + "\n\nLink expires in 1 hour. If you did not request this, ignore this email.");
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            msg.setFrom("noreply@glm.my");
            mailer.send(msg);
        } catch (MailException e) {
            log.error("Failed to send mail to {}: {}", to, e.getMessage());
        }
    }
}
