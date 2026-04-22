package otp.auth.service.application.services;

import otp.auth.service.application.interfaces.UserRepository;
import otp.auth.service.domain.User;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Optional;

public class AuthService {
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    public void register(String username, String password, String role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        if ("ADMIN".equalsIgnoreCase(role)) {
            checkAdminExistence();
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        userRepository.save(new User(null, username, hashedPassword, role.toUpperCase()));
    }

    public String login(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent() && BCrypt.checkpw(password, userOptional.get().getPasswordHash())) {
            return tokenService.generateToken(username);
        }

        throw new RuntimeException("Invalid credentials");
    }

    private void checkAdminExistence() {
        if (userRepository.existsByRole("ADMIN")) {
            throw new RuntimeException("Administrator already exists");
        }
    }
}
