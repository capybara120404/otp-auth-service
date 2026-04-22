package otp.auth.service.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import otp.auth.service.api.dto.ErrorResponse;
import otp.auth.service.api.dto.MessageResponse;
import otp.auth.service.api.dto.OtpConfigRequest;
import otp.auth.service.application.services.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class AdminHandler extends BaseHandler {
    private static final Logger logger = LoggerFactory.getLogger(AdminHandler.class);
    private final AdminService adminService;

    public AdminHandler(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("Admin request: {} {}", exchange.getRequestMethod(), exchange.getRequestURI());
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.equals("/api/admin/users") && "GET".equals(method)) {
                sendResponse(exchange, 200, adminService.getUsers());
                return;
            }

            if (path.startsWith("/api/admin/users/") && "DELETE".equals(method)) {
                String idPart = path.substring(path.lastIndexOf("/") + 1);
                Integer userId = Integer.parseInt(idPart);
                adminService.removeUser(userId);
                logger.info("Admin deleted user ID: {}", userId);
                sendResponse(exchange, 200, new MessageResponse("User deleted"));
                return;
            }

            if (path.equals("/api/admin/config") && "PUT".equals(method)) {
                OtpConfigRequest request = readJson(exchange, OtpConfigRequest.class);
                validate(request.lifeTimeSeconds() > 0 && request.codeLength() > 0, "Invalid config values");
                adminService.updateConfig(request.lifeTimeSeconds(), request.codeLength());
                sendResponse(exchange, 200, new MessageResponse("Config updated"));
                return;
            }

            sendResponse(exchange, 404, new ErrorResponse("Not found"));
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, new ErrorResponse("Invalid User ID format"));
        } catch (Exception e) {
            logger.error("Admin operation error: {}", e.getMessage());
            sendResponse(exchange, 500, new ErrorResponse("Internal server error"));
        }
    }
}
