package otp.auth.service.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import otp.auth.service.api.dto.*;
import otp.auth.service.application.services.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class OtpHandler extends BaseHandler {
    private static final Logger logger = LoggerFactory.getLogger(OtpHandler.class);
    private final OtpService otpService;

    public OtpHandler(OtpService otpService) {
        this.otpService = otpService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        logger.info("Request: {} {}", exchange.getRequestMethod(), path);

        try {
            if (path.endsWith("/generate")) {
                handleGenerate(exchange);
            } else if (path.endsWith("/verify")) {
                handleVerify(exchange);
            } else {
                sendResponse(exchange, 404, new ErrorResponse("Not found"));
            }
        } catch (Exception e) {
            logger.error("Detailed error: ", e); 
            sendResponse(exchange, 500, new ErrorResponse(e.getMessage()));
        }
    }

    private void handleGenerate(HttpExchange exchange) throws IOException {
        OtpRequest request = readJson(exchange, OtpRequest.class);
        otpService.generateAndSendCode(request.getUserId(), request.getChannel(), request.getDestination());
        sendResponse(exchange, 200, new MessageResponse("Code sent via " + request.getChannel()));
    }

    private void handleVerify(HttpExchange exchange) throws IOException {
        OtpRequest request = readJson(exchange, OtpRequest.class);
        if (otpService.verifyCode(request.getUserId(), request.getCode())) {
            sendResponse(exchange, 200, new MessageResponse("Verified"));
        } else {
            sendResponse(exchange, 400, new ErrorResponse("Invalid or expired code"));
        }
    }
}
