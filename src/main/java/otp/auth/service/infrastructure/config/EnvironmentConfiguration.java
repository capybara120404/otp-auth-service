package otp.auth.service.infrastructure.config;

import io.github.cdimascio.dotenv.Dotenv;
import otp.auth.service.application.interfaces.Configuration;

public class EnvironmentConfiguration implements Configuration {
    private final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    private static EnvironmentConfiguration instance;

    private EnvironmentConfiguration() {
    }

    public static synchronized EnvironmentConfiguration getInstance() {
        if (instance == null) {
            instance = new EnvironmentConfiguration();
        }
        return instance;
    }

    private String getRequired(String key) {
        String value = dotenv.get(key);
        if (value == null) {
            throw new RuntimeException("Missing environment variable: " + key);
        }
        return value;
    }

    @Override
    public String getDatabaseUrl() {
        return getRequired("DATABASE_URL");
    }

    @Override
    public int getOtpDefaultLifeTime() {
        return Integer.parseInt(dotenv.get("OTP_DEFAULT_LIFE_TIME", "300"));
    }

    @Override
    public int getOtpDefaultLength() {
        return Integer.parseInt(dotenv.get("OTP_DEFAULT_LENGTH", "6"));
    }

    @Override
    public String getDbUser() {
        return getRequired("DATABASE_USER");
    }

    @Override
    public String getDbPassword() {
        return getRequired("DATABASE_PASSWORD");
    }
}
