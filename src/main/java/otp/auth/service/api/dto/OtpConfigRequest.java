package otp.auth.service.api.dto;

public record OtpConfigRequest(int lifeTimeSeconds, int codeLength) {
}
