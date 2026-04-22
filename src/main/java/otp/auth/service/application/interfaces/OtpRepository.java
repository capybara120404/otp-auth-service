package otp.auth.service.application.interfaces;

import java.util.Optional;

import otp.auth.service.domain.OtpCode;
import otp.auth.service.domain.OtpConfiguration;
import otp.auth.service.domain.OtpStatus;

public interface OtpRepository {
    void save(OtpCode otp);

    Optional<OtpCode> findActiveByUserId(Integer userId);

    void updateStatus(Integer id, OtpStatus status);

    void deleteByUserId(Integer userId);

    void updateConfig(int lifeTime, int length);

    OtpConfiguration getConfig();
}
