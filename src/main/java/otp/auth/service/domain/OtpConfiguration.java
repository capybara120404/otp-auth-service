package otp.auth.service.domain;

public class OtpConfiguration {
    private final int lifeTimeSeconds;
    private final int codeLength;

    public OtpConfiguration(int lifeTimeSeconds, int codeLength) {
        this.lifeTimeSeconds = lifeTimeSeconds;
        this.codeLength = codeLength;
    }

    public int getLifeTimeSeconds() {
        return lifeTimeSeconds;
    }

    public int getCodeLength() {
        return codeLength;
    }
}
