package otp.auth.service.application.interfaces;

public interface NotificationService {
    void send(String destination, String code);

    String getChannelType();
}
