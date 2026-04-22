package otp.auth.service.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import otp.auth.service.api.dto.ErrorResponse;
import otp.auth.service.api.dto.MessageResponse;
import otp.auth.service.api.dto.RegisterRequest;
import otp.auth.service.application.services.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class RegisterHandler extends BaseHandler {
    private static final Logger logger = LoggerFactory.getLogger(RegisterHandler.class);
    private final AuthService authService;

    public RegisterHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("Request: {} {}", exchange.getRequestMethod(), exchange.getRequestURI());
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, new ErrorResponse("Method not allowed"));
            return;
        }

        try {
            RegisterRequest request = readJson(exchange, RegisterRequest.class);
            validate(request.getUsername() != null && !request.getUsername().isBlank(), "Username is required");
            validate(request.getPassword() != null && request.getPassword().length() >= 6,
                    "Password must be at least 6 characters");
            validate(request.getRole() != null, "Role is required");

            authService.register(request.getUsername(), request.getPassword(), request.getRole());
            sendResponse(exchange, 201, new MessageResponse("User registered"));
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error: {}", e.getMessage());
            sendResponse(exchange, 400, new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Registration error: {}", e.getMessage());
            sendResponse(exchange, 500, new ErrorResponse("Internal server error"));
        }
    }
}
