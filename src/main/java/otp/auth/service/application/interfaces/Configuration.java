package otp.auth.service.application.interfaces;

public interface Configuration {
    String getDatabaseUrl();

    int getOtpDefaultLifeTime();

    int getOtpDefaultLength();

    String getDbUser();

    String getDbPassword();
}
