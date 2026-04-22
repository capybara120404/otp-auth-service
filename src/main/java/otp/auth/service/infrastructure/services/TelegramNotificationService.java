package otp.auth.service.infrastructure.services;

import otp.auth.service.application.interfaces.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class TelegramNotificationService implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationService.class);
    private final String telegramApiUrl;
    private final String chatId;
    private final HttpClient httpClient;

    public TelegramNotificationService(Properties properties) {
        String botToken = properties.getProperty("telegram.bot_token");
        this.telegramApiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        this.chatId = properties.getProperty("telegram.chat_id");
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public void send(String destination, String code) {
        String message = String.format("Your confirmation code is: %s", code);
        String url = String.format("%s?chat_id=%s&text=%s",
                telegramApiUrl,
                chatId,
                URLEncoder.encode(message, StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.error("Telegram API error. Status code: {}", response.statusCode());
                throw new RuntimeException("Telegram API returned status: " + response.statusCode());
            }
            logger.info("Telegram message sent successfully");
        } catch (Exception e) {
            logger.error("Error sending Telegram message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send Telegram message", e);
        }
    }

    @Override
    public String getChannelType() {
        return "TELEGRAM";
    }
}
