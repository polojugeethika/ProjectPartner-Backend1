```java
package com.projectpartner.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.projectpartner.backend.entity.CollaborationRequest;
import com.projectpartner.backend.service.CollaborationRequestService;

@RestController
@RequestMapping("/api/collaboration-requests")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://127.0.0.1:5173",
    "https://projectpartner-frontend1.onrender.com"
})
public class CollaborationRequestController {

    private final CollaborationRequestService service;

    public CollaborationRequestController(
            CollaborationRequestService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CollaborationRequest sendRequest(
            @RequestBody Map<String, Object> body) {

        Long senderId = Long.valueOf(body.get("senderId").toString());
        Long receiverId = Long.valueOf(body.get("receiverId").toString());
        String message = body.get("message") == null
                ? "" : body.get("message").toString();

        return service.sendRequest(senderId, receiverId, message);
    }

    @GetMapping("/sent/{studentId}")
    public List<CollaborationRequest> getSentRequests(
            @PathVariable Long studentId) {
        return service.getSentRequests(studentId);
    }

    @GetMapping("/received/{studentId}")
    public List<CollaborationRequest> getReceivedRequests(
            @PathVariable Long studentId) {
        return service.getReceivedRequests(studentId);
    }

    @PutMapping("/{requestId}/respond")
    public CollaborationRequest respondToRequest(
            @PathVariable Long requestId,
            @RequestBody Map<String, Object> body) {

        Long studentId = Long.valueOf(body.get("studentId").toString());
        String status = body.get("status").toString().toUpperCase();

        return service.respondToRequest(requestId, studentId, status);
    }
}
```