package otp.auth.service.infrastructure.repositories;

import otp.auth.service.application.interfaces.UserRepository;
import otp.auth.service.domain.User;
import otp.auth.service.infrastructure.persistence.DatabaseContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {
    private final DatabaseContext dbContext;

    public JdbcUserRepository(DatabaseContext dbContext) {
        this.dbContext = dbContext;
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user", e);
        }
        return Optional.empty();
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    @Override
    public boolean existsByRole(String role) {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE role = ?)";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking role existence", e);
        }
        return false;
    }

    @Override
    public List<User> findAllNonAdmins() {
        String sql = "SELECT * FROM users WHERE role != 'ADMIN'";
        List<User> users = new ArrayList<>();
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching users", e);
        }
        return users;
    }

    private User mapToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("role"));
    }
}
