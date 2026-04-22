package otp.auth.service.application.interfaces;

import otp.auth.service.domain.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    void save(User user);

    Optional<User> findByUsername(String username);

    void deleteById(Integer id);

    boolean existsByRole(String role);

    List<User> findAllNonAdmins();
}
