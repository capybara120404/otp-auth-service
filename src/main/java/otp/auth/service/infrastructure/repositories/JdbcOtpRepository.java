package otp.auth.service.infrastructure.repositories;

import otp.auth.service.application.interfaces.OtpRepository;
import otp.auth.service.domain.OtpCode;
import otp.auth.service.domain.OtpConfiguration;
import otp.auth.service.domain.OtpStatus;
import otp.auth.service.infrastructure.persistence.DatabaseContext;

import java.sql.*;
import java.util.Optional;

public class JdbcOtpRepository implements OtpRepository {
    private final DatabaseContext dbContext;

    public JdbcOtpRepository(DatabaseContext dbContext) {
        this.dbContext = dbContext;
    }

    @Override
    public void save(OtpCode code) {
        String sql = "INSERT INTO otp_codes (user_id, code, status, expires_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, code.getUserId());
            ps.setString(2, code.getCode());
            ps.setString(3, code.getStatus().name());
            ps.setTimestamp(4, Timestamp.valueOf(code.getExpiresAt()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving OTP code", e);
        }
    }

    @Override
    public Optional<OtpCode> findActiveByUserId(Integer userId) {
        String sql = "SELECT * FROM otp_codes WHERE user_id = ? AND status = 'ACTIVE' ORDER BY expires_at DESC LIMIT 1";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new OtpCode(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("code"),
                            OtpStatus.valueOf(rs.getString("status")),
                            rs.getTimestamp("expires_at").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding active OTP", e);
        }
        return Optional.empty();
    }

    @Override
    public void updateStatus(Integer id, OtpStatus status) {
        String sql = "UPDATE otp_codes SET status = ? WHERE id = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating OTP status", e);
        }
    }

    @Override
    public void deleteByUserId(Integer userId) {
        String sql = "DELETE FROM otp_codes WHERE user_id = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting OTPs", e);
        }
    }

    @Override
    public void updateConfig(int lifeTime, int length) {
        String sql = "UPDATE otp_configuration SET life_time_seconds = ?, code_length = ? WHERE id = 1";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lifeTime);
            ps.setInt(2, length);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating OTP config", e);
        }
    }

    @Override
    public OtpConfiguration getConfig() {
        String sql = "SELECT life_time_seconds, code_length FROM otp_configuration WHERE id = 1";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new OtpConfiguration(rs.getInt("life_time_seconds"), rs.getInt("code_length"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching OTP config", e);
        }
        return new OtpConfiguration(300, 6);
    }
}
