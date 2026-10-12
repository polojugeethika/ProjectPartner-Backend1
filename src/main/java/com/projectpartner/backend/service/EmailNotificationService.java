package com.projectpartner.backend.service;

import com.projectpartner.backend.entity.CollaborationRequest;
import com.projectpartner.backend.entity.EmailActionToken;
import com.projectpartner.backend.entity.Student;
import com.projectpartner.backend.repository.EmailActionTokenRepository;
import com.projectpartner.backend.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class EmailNotificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);
    private final JavaMailSender mailSender;
    private final StudentRepository students;
    private final EmailActionTokenRepository tokens;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;
    @Value("${app.email.from:}")
    private String fromAddress;
    @Value("${app.email.action-token-hours:24}")
    private long tokenHours;

    public EmailNotificationService(JavaMailSender mailSender, StudentRepository students,
                                    EmailActionTokenRepository tokens) {
        this.mailSender = mailSender;
        this.students = students;
        this.tokens = tokens;
    }

    public void sendCollaborationRequestEmail(CollaborationRequest request) {
        Student sender = students.findById(request.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Sender student not found"));
        Student receiver = students.findById(request.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("Receiver student not found"));

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String rawToken = HexFormat.of().formatHex(bytes);

        EmailActionToken token = new EmailActionToken();
        token.setTokenHash(sha256(rawToken));
        token.setRequestId(request.getId());
        token.setReceiverId(receiver.getId());
        token.setExpiresAt(LocalDateTime.now().plusHours(Math.max(1, tokenHours)));
        tokens.save(token);

        String base = publicBaseUrl == null ? "" : publicBaseUrl.trim().replaceAll("/+$", "");
        String actionUrl = base + "/email-actions/" + rawToken;
        SimpleMailMessage message = new SimpleMailMessage();
        if (fromAddress != null && !fromAddress.isBlank()) message.setFrom(fromAddress);
        message.setTo(receiver.getEmail());
        message.setSubject("New teammate request on ProjectPartner");
        message.setText("Hi " + receiver.getName() + ",\n\n"
                + sender.getName() + " has sent you a teammate request on ProjectPartner.\n"
                + "Skills: " + sender.getSkills() + "\n"
                + (request.getMessage() == null || request.getMessage().isBlank()
                    ? "" : "Message: " + request.getMessage() + "\n")
                + "\nReview the request and choose Accept or Decline here:\n"
                + actionUrl + "\n\n"
                + "This link expires in " + Math.max(1, tokenHours)
                + " hours and can be used once. If you did not expect this request, ignore this email.\n");

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            // Do not lose the saved request if SMTP is not configured. The application log
            // intentionally excludes the token and recipient's email address.
            log.warn("Could not send ProjectPartner request email for request id {}: {}",
                    request.getId(), ex.getClass().getSimpleName());
        }
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
