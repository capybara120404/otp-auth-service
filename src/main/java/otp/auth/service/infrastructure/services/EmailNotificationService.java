package otp.auth.service.infrastructure.services;

import otp.auth.service.application.interfaces.NotificationService;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailNotificationService implements NotificationService {
    private final Session session;
    private final String fromEmail;

    public EmailNotificationService(Properties props) {
        this.fromEmail = props.getProperty("email.from");
        this.session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(props.getProperty("email.username"),
                        props.getProperty("email.password"));
            }
        });
    }

    @Override
    public void send(String toEmail, String code) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Your OTP Code");
            message.setText("Your verification code is: " + code);
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public String getChannelType() {
        return "EMAIL";
    }
}
