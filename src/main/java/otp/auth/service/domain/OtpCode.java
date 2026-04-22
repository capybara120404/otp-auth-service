package otp.auth.service.domain;

import java.time.LocalDateTime;

public class OtpCode {
    private final Integer id;
    private final Integer userId;
    private final String code;
    private final OtpStatus status;
    private final LocalDateTime expiresAt;

    public OtpCode(Integer id, Integer userId, String code, OtpStatus status, LocalDateTime expiresAt) {
        this.id = id;
        this.userId = userId;
        this.code = code;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public Integer getId() {
        return id;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getCode() {
        return code;
    }

    public OtpStatus getStatus() {
        return status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
