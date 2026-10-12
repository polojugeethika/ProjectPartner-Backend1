package com.projectpartner.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projectpartner.backend.entity.CollaborationRequest;

public interface CollaborationRequestRepository
extends JpaRepository<CollaborationRequest, Long> {

List<CollaborationRequest> findBySenderId(Long senderId);

List<CollaborationRequest> findByReceiverId(Long receiverId);


}

