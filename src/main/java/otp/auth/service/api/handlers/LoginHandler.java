package otp.auth.service.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import otp.auth.service.api.dto.ErrorResponse;
import otp.auth.service.api.dto.LoginRequest;
import otp.auth.service.api.dto.TokenResponse;
import otp.auth.service.application.services.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class LoginHandler extends BaseHandler {
    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);
    private final AuthService authService;

    public LoginHandler(AuthService authService) {
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
            LoginRequest request = readJson(exchange, LoginRequest.class);
            validate(request.getUsername() != null && !request.getUsername().isBlank(), "Username is required");
            validate(request.getPassword() != null && !request.getPassword().isBlank(), "Password is required");

            String token = authService.login(request.getUsername(), request.getPassword());
            sendResponse(exchange, 200, new TokenResponse(token));
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error: {}", e.getMessage());
            sendResponse(exchange, 400, new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.warn("Login failed: {}", e.getMessage());
            sendResponse(exchange, 401, new ErrorResponse("Invalid credentials"));
        }
    }
}
