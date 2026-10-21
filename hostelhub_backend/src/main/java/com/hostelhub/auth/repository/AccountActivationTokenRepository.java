

package com.hostelhub.auth.repository;

import com.hostelhub.auth.entity.AccountActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountActivationTokenRepository
        extends JpaRepository<AccountActivationToken, Long> {

    Optional<AccountActivationToken> findByTokenHashAndUsedAtIsNull(
            String tokenHash
    );

    Optional<AccountActivationToken> findByTokenHash(String tokenHash);

    void deleteByUser_Id(Long userId);
}

