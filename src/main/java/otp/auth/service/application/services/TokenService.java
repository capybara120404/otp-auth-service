package otp.auth.service.application.services;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenService {
    private static final Map<String, TokenData> tokenStorage = new ConcurrentHashMap<>();
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final long TOKEN_TTL_SECONDS = 3600;

    private record TokenData(String username, Instant expiresAt) {
    }

    public String generateToken(String username) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        Instant expiresAt = Instant.now().plusSeconds(TOKEN_TTL_SECONDS);
        tokenStorage.put(token, new TokenData(username, expiresAt));

        return token;
    }

    public String getUsernameFromToken(String token) {
        TokenData data = tokenStorage.get(token);
        if (data == null) {
            return null;
        }

        if (Instant.now().isAfter(data.expiresAt())) {
            tokenStorage.remove(token);
            return null;
        }

        return data.username();
    }

    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        tokenStorage.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }
}
