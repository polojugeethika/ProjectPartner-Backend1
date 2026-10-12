
package com.projectpartner.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "collaboration_requests")
public class CollaborationRequest {


@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false)
private Long senderId;

@Column(nullable = false)
private Long receiverId;

@Size(max = 1000)
@Column(length = 1000)
private String message;

@Column(nullable = false)
private String status = "PENDING";

@Column(nullable = false)
private LocalDateTime createdAt = LocalDateTime.now();

public CollaborationRequest() {}

public Long getId() {
    return id;
}

public Long getSenderId() {
    return senderId;
}

public void setSenderId(Long senderId) {
    this.senderId = senderId;
}

public Long getReceiverId() {
    return receiverId;
}

public void setReceiverId(Long receiverId) {
    this.receiverId = receiverId;
}

public String getMessage() {
    return message;
}

public void setMessage(String message) {
    this.message = message;
}

public String getStatus() {
    return status;
}

public void setStatus(String status) {
    this.status = status;
}

public LocalDateTime getCreatedAt() {
    return createdAt;
}


}
