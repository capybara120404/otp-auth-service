package otp.auth.service.api.filters;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import otp.auth.service.application.interfaces.UserRepository;
import otp.auth.service.application.services.TokenService;
import java.io.IOException;

public class AuthFilter extends Filter {
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final boolean requireAdmin;

    public AuthFilter(TokenService tokenService, UserRepository userRepository, boolean requireAdmin) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.requireAdmin = requireAdmin;
    }

    @Override
    public String description() {
        return "Authentication and Role Filter";
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        String token = authHeader.substring(7);
        String username = tokenService.getUsernameFromToken(token);

        if (username == null) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        if (requireAdmin) {
            boolean isAdmin = userRepository.findByUsername(username)
                    .map(user -> "ADMIN".equals(user.getRole()))
                    .orElse(false);

            if (!isAdmin) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }
        }

        chain.doFilter(exchange);
    }
}
