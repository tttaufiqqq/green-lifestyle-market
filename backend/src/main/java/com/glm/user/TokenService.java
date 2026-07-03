package com.glm.user;

import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.user.entity.User;
import com.glm.user.entity.VerificationToken;
import com.glm.user.entity.VerificationToken.Purpose;
import com.glm.user.repository.VerificationTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class TokenService {

    private static final int EXPIRY_HOURS = 1;

    private final VerificationTokenRepository tokenRepo;
    private final SecureRandom rng = new SecureRandom();

    public TokenService(VerificationTokenRepository tokenRepo) {
        this.tokenRepo = tokenRepo;
    }

    /** Generates a raw token, stores its SHA-256 hash, returns the raw value to send in email. */
    @Transactional
    public String createToken(User user, Purpose purpose) {
        byte[] bytes = new byte[32];
        rng.nextBytes(bytes);
        String rawToken = HexFormat.of().formatHex(bytes);

        tokenRepo.deleteByUserIdAndPurpose(user.getId(), purpose);

        VerificationToken vt = new VerificationToken();
        vt.setUser(user);
        vt.setTokenHash(sha256(rawToken));
        vt.setPurpose(purpose);
        vt.setExpiresAt(Instant.now().plusSeconds(EXPIRY_HOURS * 3600L));
        tokenRepo.save(vt);
        return rawToken;
    }

    /** Validates token, marks it used, returns the owning user. Single-use enforced. */
    @Transactional
    public User consumeToken(String rawToken, Purpose purpose) {
        VerificationToken vt = tokenRepo.findByTokenHash(sha256(rawToken))
                .orElseThrow(() -> new DomainException(ErrorCode.E_NOTFOUND, "Invalid or expired token", 404));

        if (!vt.getPurpose().equals(purpose) || vt.getUsedAt() != null
                || Instant.now().isAfter(vt.getExpiresAt()))
            throw new DomainException(ErrorCode.E_NOTFOUND, "Invalid or expired token", 404);

        vt.setUsedAt(Instant.now());
        return vt.getUser();
    }

    static String sha256(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }
}
