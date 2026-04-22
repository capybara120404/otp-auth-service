package otp.auth.service.infrastructure.services;

import otp.auth.service.application.interfaces.NotificationService;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileNotificationService implements NotificationService {
    @Override
    public void send(String destination, String code) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("otp_codes.txt", true))) {
            writer.println("Destination: " + destination + ", Code: " + code);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save OTP to file", e);
        }
    }

    @Override
    public String getChannelType() {
        return "FILE";
    }
}
