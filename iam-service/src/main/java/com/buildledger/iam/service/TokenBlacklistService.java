package com.buildledger.iam.service;

import com.buildledger.iam.entity.RevokedToken;
import com.buildledger.iam.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final RevokedTokenRepository revokedTokenRepository;

    /**
     * Add a token to the blacklist (called on logout).
     */
    @Transactional
    public void revokeToken(String tokenId, Long userId, Date expiryDate) {
        if (!revokedTokenRepository.existsByTokenId(tokenId)) {
            RevokedToken revoked = RevokedToken.builder()
                    .tokenId(tokenId)
                    .userId(userId)
                    .expiryDate(expiryDate.toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime())
                    .build();
            revokedTokenRepository.save(revoked);
            log.debug("Token revoked for userId={}, tokenId={}", userId, tokenId);
        }
    }

    /**
     * Check if a token ID is in the blacklist.
     */
    @Transactional(readOnly = true)
    public boolean isTokenRevoked(String tokenId) {
        return revokedTokenRepository.existsByTokenId(tokenId);
    }

    /**
     * Revoke all tokens for a user (e.g., on password change or account lock).
     */
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        revokedTokenRepository.deleteByUserId(userId);
        log.info("All tokens revoked for userId={}", userId);
    }

    /**
     * Scheduled cleanup: remove expired tokens from the blacklist daily.
     */
    @Scheduled(cron = "0 0 2 * * *") // 2 AM daily
    @Transactional
    public void cleanupExpiredTokens() {
        int count = revokedTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Cleaned up {} expired revoked tokens", count);
    }
}
