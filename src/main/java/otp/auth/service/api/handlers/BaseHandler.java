package otp.auth.service.api.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public abstract class BaseHandler implements HttpHandler {
    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected <T> T readJson(HttpExchange exchange, Class<T> clazz) throws IOException {
        return objectMapper.readValue(exchange.getRequestBody(), clazz);
    }

    protected void validate(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    protected void sendResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
        byte[] bytes = objectMapper.writeValueAsBytes(response);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }
}
