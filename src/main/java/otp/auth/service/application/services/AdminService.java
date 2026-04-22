package otp.auth.service.application.services;

import otp.auth.service.application.interfaces.OtpRepository;
import otp.auth.service.application.interfaces.UserRepository;
import otp.auth.service.domain.User;
import java.util.List;

public class AdminService {
    private final UserRepository userRepository;
    private final OtpRepository otpRepository;

    public AdminService(UserRepository userRepository, OtpRepository otpRepository) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
    }

    public List<User> getUsers() {
        return userRepository.findAllNonAdmins();
    }

    public void removeUser(Integer userId) {
        otpRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }

    public void updateConfig(int lifeTime, int length) {
        otpRepository.updateConfig(lifeTime, length);
    }
}
