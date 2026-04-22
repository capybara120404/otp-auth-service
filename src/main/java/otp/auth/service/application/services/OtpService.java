package otp.auth.service.application.services;

import otp.auth.service.application.interfaces.NotificationService;
import otp.auth.service.application.interfaces.OtpRepository;
import otp.auth.service.domain.OtpCode;
import otp.auth.service.domain.OtpConfiguration;
import otp.auth.service.domain.OtpStatus;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

public class OtpService {
    private final OtpRepository otpRepository;
    private final List<NotificationService> notificationServices;
    private final SecureRandom random = new SecureRandom();

    public OtpService(OtpRepository otpRepository, List<NotificationService> notificationServices) {
        this.otpRepository = otpRepository;
        this.notificationServices = notificationServices;
    }

    public void generateAndSendCode(int userId, String channel, String destination) {
        OtpConfiguration config = otpRepository.getConfig();
        String code = generateCode(config.getCodeLength());
        OtpCode otpCode = new OtpCode(null, userId, code, OtpStatus.ACTIVE,
                LocalDateTime.now().plusSeconds(config.getLifeTimeSeconds()));
        otpRepository.save(otpCode);

        notificationServices.stream()
                .filter(service -> service.getChannelType().equalsIgnoreCase(channel))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Channel not supported: " + channel))
                .send(destination, code);
    }

    private String generateCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    public boolean verifyCode(int userId, String code) {
        return otpRepository.findActiveByUserId(userId)
                .filter(otp -> otp.getCode().equals(code) && otp.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(otp -> {
                    otpRepository.updateStatus(otp.getId(), OtpStatus.USED);
                    return true;
                })
                .orElse(false);
    }
}
