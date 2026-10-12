package com.projectpartner.backend.repository;

import com.projectpartner.backend.entity.EmailActionToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailActionTokenRepository extends JpaRepository<EmailActionToken, Long> {
    Optional<EmailActionToken> findByTokenHash(String hash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from EmailActionToken t where t.tokenHash = :hash")
    Optional<EmailActionToken> findByTokenHashForUpdate(@Param("hash") String hash);
}
