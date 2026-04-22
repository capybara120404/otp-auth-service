package otp.auth.service;

import com.sun.net.httpserver.HttpServer;
import otp.auth.service.api.filters.AuthFilter;
import otp.auth.service.api.handlers.*;
import otp.auth.service.application.interfaces.Configuration;
import otp.auth.service.application.interfaces.NotificationService;
import otp.auth.service.application.services.*;
import otp.auth.service.infrastructure.config.EnvironmentConfiguration;
import otp.auth.service.infrastructure.persistence.DatabaseContext;
import otp.auth.service.infrastructure.repositories.JdbcOtpRepository;
import otp.auth.service.infrastructure.repositories.JdbcUserRepository;
import otp.auth.service.infrastructure.services.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException {
        Configuration configuration = EnvironmentConfiguration.getInstance();
        DatabaseContext databaseContext = new DatabaseContext(configuration);

        JdbcUserRepository userRepository = new JdbcUserRepository(databaseContext);
        TokenService tokenService = new TokenService();
        AuthService authService = new AuthService(userRepository, tokenService);

        JdbcOtpRepository otpRepository = new JdbcOtpRepository(databaseContext);

        List<NotificationService> notificationServices = loadNotificationServices();

        OtpService otpService = new OtpService(otpRepository, notificationServices);
        AdminService adminService = new AdminService(userRepository, otpRepository);

        AdminHandler adminHandler = new AdminHandler(adminService);
        OtpHandler otpHandler = new OtpHandler(otpService);

        AuthFilter userAuthFilter = new AuthFilter(tokenService, userRepository, false);
        AuthFilter adminAuthFilter = new AuthFilter(tokenService, userRepository, true);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/register", new RegisterHandler(authService));
        server.createContext("/api/login", new LoginHandler(authService));

        var otpContext = server.createContext("/api/otp", otpHandler);
        otpContext.getFilters().add(userAuthFilter);

        var adminContext = server.createContext("/api/admin", adminHandler);
        adminContext.getFilters().add(adminAuthFilter);

        startSchedulers(databaseContext, tokenService);

        server.setExecutor(null);
        server.start();
    }

    private static List<NotificationService> loadNotificationServices() {
        return List.of(
                new FileNotificationService(),
                new EmailNotificationService(loadProperties("email.properties")),
                new SmppNotificationService(loadProperties("sms.properties")),
                new TelegramNotificationService(loadProperties("telegram.properties")));
    }

    private static Properties loadProperties(String fileName) {
        Properties props = new Properties();
        try (InputStream stream = Main.class.getClassLoader().getResourceAsStream(fileName)) {
            if (stream != null) {
                props.load(stream);
            }
        } catch (IOException e) {
            System.err.println("Could not load " + fileName);
        }

        return props;
    }

    private static void startSchedulers(DatabaseContext databaseContext, TokenService tokenService) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try (Connection connection = databaseContext.getConnection();
                    Statement statement = connection.createStatement()) {
                statement.executeUpdate(
                        "UPDATE otp_codes SET status = 'EXPIRED' WHERE status = 'ACTIVE' AND expires_at < NOW()");
            } catch (Exception e) {
                System.err.println("Scheduler error: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.MINUTES);

        scheduler.scheduleAtFixedRate(() -> {
            tokenService.cleanupExpiredTokens();
        }, 1, 1, TimeUnit.HOURS);
    }
}
