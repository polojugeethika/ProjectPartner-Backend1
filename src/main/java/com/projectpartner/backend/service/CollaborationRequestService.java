
package com.projectpartner.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.projectpartner.backend.entity.CollaborationRequest;
import com.projectpartner.backend.repository.CollaborationRequestRepository;

@Service
public class CollaborationRequestService {

    private final CollaborationRequestRepository repository;
    private final EmailNotificationService emailService;

    public CollaborationRequestService(
            CollaborationRequestRepository repository,
            EmailNotificationService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    public CollaborationRequest sendRequest(
            Long senderId, Long receiverId, String message) {

        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException(
                    "Sender and receiver IDs are required");
        }

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException(
                    "You cannot send a request to yourself");
        }

        CollaborationRequest request = new CollaborationRequest();
        request.setSenderId(senderId);
        request.setReceiverId(receiverId);
        request.setMessage(message);
        request.setStatus("PENDING");

        // First save the request to generate its ID
        CollaborationRequest savedRequest = repository.save(request);

        // Then send the notification email
        emailService.sendCollaborationRequestEmail(savedRequest);

        return savedRequest;
    }

    public List<CollaborationRequest> getSentRequests(Long studentId) {
        return repository.findBySenderId(studentId);
    }

    public List<CollaborationRequest> getReceivedRequests(Long studentId) {
        return repository.findByReceiverId(studentId);
    }

    public CollaborationRequest respondToRequest(
            Long requestId, Long studentId, String status) {

        CollaborationRequest request = repository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Collaboration request not found"));

        if (studentId == null
                || !request.getReceiverId().equals(studentId)) {
            throw new IllegalArgumentException(
                    "Only the recipient can respond");
        }

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalArgumentException(
                    "This request has already been answered");
        }

        if (!"ACCEPTED".equals(status)
                && !"DECLINED".equals(status)) {
            throw new IllegalArgumentException(
                    "Status must be ACCEPTED or DECLINED");
        }

        request.setStatus(status);
        return repository.save(request);
    }
}
