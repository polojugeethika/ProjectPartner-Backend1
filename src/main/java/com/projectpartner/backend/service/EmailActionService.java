
package com.projectpartner.backend.service;

import com.projectpartner.backend.entity.CollaborationRequest;
import com.projectpartner.backend.entity.EmailActionToken;
import com.projectpartner.backend.repository.CollaborationRequestRepository;
import com.projectpartner.backend.repository.EmailActionTokenRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class EmailActionService {

    private final EmailActionTokenRepository tokens;
    private final CollaborationRequestRepository requests;

    public EmailActionService(
            EmailActionTokenRepository tokens,
            CollaborationRequestRepository requests) {
        this.tokens = tokens;
        this.requests = requests;
    }

    @Transactional
    public String respond(String rawToken, String decision) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email action token is required.");
        }

        String status = decision == null
                ? ""
                : decision.trim().toUpperCase();

        if (!"ACCEPTED".equals(status) && !"DECLINED".equals(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Choose Accept or Decline.");
        }

        EmailActionToken token = tokens
                .findByTokenHashForUpdate(sha256(rawToken))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.GONE,
                        "This email action link is invalid or has expired."));

        LocalDateTime now = LocalDateTime.now();

        if (token.getUsedAt() != null
                || !token.getExpiresAt().isAfter(now)) {
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "This email action link has already been used or has expired.");
        }

        CollaborationRequest request = requests
                .findById(token.getRequestId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "The teammate request no longer exists."));

        if (!token.getReceiverId().equals(request.getReceiverId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This link is not valid for this request.");
        }

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This request has already been answered.");
        }

        request.setStatus(status);
        token.setUsedAt(now);

        requests.save(request);
        tokens.save(token);

        return status;
    }

    public boolean isActionAvailable(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        return tokens.findByTokenHash(sha256(rawToken))
                .filter(token -> token.getUsedAt() == null)
                .filter(token -> token.getExpiresAt().isAfter(now))
                .filter(token -> requests.findById(token.getRequestId())
                        .map(request ->
                                "PENDING".equalsIgnoreCase(request.getStatus())
                                && token.getReceiverId()
                                        .equals(request.getReceiverId()))
                        .orElse(false))
                .isPresent();
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "SHA-256 is not available", ex);
        }
    }
}
