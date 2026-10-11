package com.smartstay.repository;

import com.smartstay.model.Refund;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class RefundRepository {

    private final JdbcTemplate jdbcTemplate;

    public RefundRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Refund> rowMapper = (rs, rowNum) -> {
        Refund ref = new Refund();
        ref.setRefundId(rs.getInt("refund_id"));
        ref.setFeedbackId(rs.getInt("feedback_id"));
        ref.setAmount(rs.getBigDecimal("amount"));
        ref.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("refund_date");
        if (ts != null) {
            ref.setRefundDate(ts.toLocalDateTime());
        }
        ref.setReason(rs.getString("reason"));

        try {
            ref.setReservationId(rs.getInt("reservation_id"));
            ref.setGuestName(rs.getString("guest_name"));
        } catch (Exception ignored) {}

        return ref;
    };

    public List<Refund> findAll() {
        String sql = """
            SELECT r.refund_id, r.feedback_id, r.amount, r.status, r.refund_date, r.reason,
                   f.reservation_id, g.name AS guest_name
            FROM Refund r
            JOIN Feedback f ON r.feedback_id = f.feedback_id
            JOIN Guest g ON f.guest_id = g.guest_id
            ORDER BY r.refund_id DESC
        """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Refund> findByFeedbackId(Integer feedbackId) {
        String sql = """
            SELECT r.refund_id, r.feedback_id, r.amount, r.status, r.refund_date, r.reason,
                   f.reservation_id, g.name AS guest_name
            FROM Refund r
            JOIN Feedback f ON r.feedback_id = f.feedback_id
            JOIN Guest g ON f.guest_id = g.guest_id
            WHERE r.feedback_id = ?
        """;
        try {
            Refund ref = jdbcTemplate.queryForObject(sql, rowMapper, feedbackId);
            return Optional.ofNullable(ref);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Refund> findById(Integer refundId) {
        String sql = """
            SELECT r.refund_id, r.feedback_id, r.amount, r.status, r.refund_date, r.reason,
                   f.reservation_id, g.name AS guest_name
            FROM Refund r
            JOIN Feedback f ON r.feedback_id = f.feedback_id
            JOIN Guest g ON f.guest_id = g.guest_id
            WHERE r.refund_id = ?
        """;
        try {
            Refund ref = jdbcTemplate.queryForObject(sql, rowMapper, refundId);
            return Optional.ofNullable(ref);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int save(Refund refund) {
        String sql = "INSERT INTO Refund (feedback_id, amount, status, refund_date, reason) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, refund.getFeedbackId());
            ps.setBigDecimal(2, refund.getAmount());
            ps.setString(3, refund.getStatus() != null ? refund.getStatus() : "PENDING");
            ps.setTimestamp(4, refund.getRefundDate() != null ? Timestamp.valueOf(refund.getRefundDate()) : null);
            ps.setString(5, refund.getReason());
            return ps;
        }, keyHolder);

        Number key = null;
        try {
            key = keyHolder.getKey();
        } catch (Exception ignored) {}

        if (key != null) {
            refund.setRefundId(key.intValue());
            return key.intValue();
        } else {
            try {
                Integer maxId = jdbcTemplate.queryForObject("SELECT MAX(refund_id) FROM Refund", Integer.class);
                if (maxId != null) {
                    refund.setRefundId(maxId);
                    return maxId;
                }
            } catch (Exception ignored) {}
        }
        return 0;
    }

    public int updateStatus(Integer refundId, String status) {
        String sql = "UPDATE Refund SET status = ?, refund_date = GETDATE() WHERE refund_id = ?";
        return jdbcTemplate.update(sql, status, refundId);
    }

    public int deleteByFeedbackId(Integer feedbackId) {
        String sql = "DELETE FROM Refund WHERE feedback_id = ?";
        return jdbcTemplate.update(sql, feedbackId);
    }

    public int deleteById(Integer refundId) {
        String sql = "DELETE FROM Refund WHERE refund_id = ?";
        return jdbcTemplate.update(sql, refundId);
    }
}
