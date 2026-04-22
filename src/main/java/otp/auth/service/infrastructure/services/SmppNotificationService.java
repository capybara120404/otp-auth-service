package otp.auth.service.infrastructure.services;

import otp.auth.service.application.interfaces.NotificationService;
import org.jsmpp.bean.*;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class SmppNotificationService implements NotificationService {
    private final String host;
    private final int port;
    private final String systemId;
    private final String password;
    private final String systemType;
    private final String sourceAddress;

    public SmppNotificationService(Properties properties) {
        this.host = properties.getProperty("smpp.host");
        this.port = Integer.parseInt(properties.getProperty("smpp.port"));
        this.systemId = properties.getProperty("smpp.system_id");
        this.password = properties.getProperty("smpp.password");
        this.systemType = properties.getProperty("smpp.system_type");
        this.sourceAddress = properties.getProperty("smpp.source_addr");
    }

    @Override
    public void send(String destination, String code) {
        try (SMPPSession session = new SMPPSession()) {
            session.connectAndBind(host, port, new BindParameter(
                    BindType.BIND_TX,
                    systemId,
                    password,
                    systemType,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    sourceAddress));

            session.submitShortMessage(
                    systemType,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    sourceAddress,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    destination,
                    new ESMClass(),
                    (byte) 0,
                    (byte) 1,
                    null,
                    null,
                    new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT),
                    (byte) 0,
                    new GeneralDataCoding(Alphabet.ALPHA_DEFAULT),
                    (byte) 0,
                    ("Your code: " + code).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS via SMPP", e);
        }
    }

    @Override
    public String getChannelType() {
        return "SMS";
    }
}
