
package com.hostelhub.auth.repository;

import com.hostelhub.auth.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetOtpRepository
        extends JpaRepository<PasswordResetOtp, Long> {

    List<PasswordResetOtp> findByUser_IdAndConsumedAtIsNull(Long userId);

    Optional<PasswordResetOtp> findFirstByUser_IdAndConsumedAtIsNullOrderByCreatedAtDesc(
            Long userId
    );

    Optional<PasswordResetOtp> findByResetTokenHashAndConsumedAtIsNull(
            String resetTokenHash
    );
}


