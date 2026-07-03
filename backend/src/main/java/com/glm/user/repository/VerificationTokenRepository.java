package com.glm.user.repository;

import com.glm.user.entity.VerificationToken;
import com.glm.user.entity.VerificationToken.Purpose;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByTokenHash(String tokenHash);
    void deleteByUserIdAndPurpose(Long userId, Purpose purpose);
}
