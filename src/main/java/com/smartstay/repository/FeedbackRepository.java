package com.smartstay.repository;

import com.smartstay.model.Feedback;
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
public class FeedbackRepository {

    private final JdbcTemplate jdbcTemplate;

    public FeedbackRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Feedback> rowMapper = (rs, rowNum) -> {
        Feedback f = new Feedback();
        f.setFeedbackId(rs.getInt("feedback_id"));
        f.setGuestId(rs.getInt("guest_id"));
        f.setReservationId(rs.getInt("reservation_id"));
        int staffId = rs.getInt("handled_by_staff_id");
        if (!rs.wasNull()) {
            f.setHandledByStaffId(staffId);
        }
        int rating = rs.getInt("rating");
        if (!rs.wasNull()) {
            f.setRating(rating);
        }
        f.setComments(rs.getString("comments"));
        f.setCategory(rs.getString("category"));
        f.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("submitted_date");
        if (ts != null) {
            f.setSubmittedDate(ts.toLocalDateTime());
        }

        try {
            f.setGuestName(rs.getString("guest_name"));
            f.setGuestEmail(rs.getString("guest_email"));
        } catch (Exception ignored) {}
        try {
            f.setHandledByStaffName(rs.getString("staff_name"));
        } catch (Exception ignored) {}
        try {
            f.setRoomNumber(rs.getString("room_number"));
        } catch (Exception ignored) {}

        return f;
    };

    public List<Feedback> findAll() {
        String sql = """
            SELECT f.feedback_id, f.guest_id, f.reservation_id, f.handled_by_staff_id,
                   f.rating, f.comments, f.category, f.status, f.submitted_date,
                   g.name AS guest_name, g.email AS guest_email, s.name AS staff_name, r.room_number
            FROM Feedback f
            LEFT JOIN Guest g ON f.guest_id = g.guest_id
            LEFT JOIN Reservation res ON f.reservation_id = res.reservation_id
            LEFT JOIN Room r ON res.room_id = r.room_id
            LEFT JOIN Staff s ON f.handled_by_staff_id = s.staff_id
            ORDER BY f.submitted_date DESC
        """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Feedback> findByGuestId(Integer guestId) {
        String sql = """
            SELECT f.feedback_id, f.guest_id, f.reservation_id, f.handled_by_staff_id,
                   f.rating, f.comments, f.category, f.status, f.submitted_date,
                   g.name AS guest_name, g.email AS guest_email, s.name AS staff_name, r.room_number
            FROM Feedback f
            LEFT JOIN Guest g ON f.guest_id = g.guest_id
            LEFT JOIN Reservation res ON f.reservation_id = res.reservation_id
            LEFT JOIN Room r ON res.room_id = r.room_id
            LEFT JOIN Staff s ON f.handled_by_staff_id = s.staff_id
            WHERE f.guest_id = ?
            ORDER BY f.submitted_date DESC
        """;
        return jdbcTemplate.query(sql, rowMapper, guestId);
    }

    public Optional<Feedback> findById(Integer feedbackId) {
        String sql = """
            SELECT f.feedback_id, f.guest_id, f.reservation_id, f.handled_by_staff_id,
                   f.rating, f.comments, f.category, f.status, f.submitted_date,
                   g.name AS guest_name, g.email AS guest_email, s.name AS staff_name, r.room_number
            FROM Feedback f
            LEFT JOIN Guest g ON f.guest_id = g.guest_id
            LEFT JOIN Reservation res ON f.reservation_id = res.reservation_id
            LEFT JOIN Room r ON res.room_id = r.room_id
            LEFT JOIN Staff s ON f.handled_by_staff_id = s.staff_id
            WHERE f.feedback_id = ?
        """;
        try {
            Feedback f = jdbcTemplate.queryForObject(sql, rowMapper, feedbackId);
            return Optional.ofNullable(f);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int save(Feedback f) {
        String sql = """
            INSERT INTO Feedback (guest_id, reservation_id, handled_by_staff_id, rating, comments, category, status, submitted_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, f.getGuestId());
            ps.setInt(2, f.getReservationId());
            if (f.getHandledByStaffId() != null) {
                ps.setInt(3, f.getHandledByStaffId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            if (f.getRating() != null) {
                ps.setInt(4, f.getRating());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            ps.setString(5, f.getComments());
            ps.setString(6, f.getCategory() != null ? f.getCategory() : "SERVICE");
            ps.setString(7, f.getStatus() != null ? f.getStatus() : "OPEN");
            ps.setTimestamp(8, f.getSubmittedDate() != null ? Timestamp.valueOf(f.getSubmittedDate()) : new Timestamp(System.currentTimeMillis()));
            return ps;
        }, keyHolder);

        Number key = null;
        try {
            key = keyHolder.getKey();
        } catch (Exception e) {
            if (keyHolder.getKeyList() != null && !keyHolder.getKeyList().isEmpty()) {
                Object val = keyHolder.getKeyList().get(0).values().iterator().next();
                if (val instanceof Number n) key = n;
            }
        }
        if (key == null) {
            try {
                key = jdbcTemplate.queryForObject("SELECT MAX(feedback_id) FROM Feedback", Integer.class);
            } catch (Exception ignored) {}
        }
        if (key != null) {
            f.setFeedbackId(key.intValue());
            return key.intValue();
        }
        return 0;
    }

    public int updateStatus(Integer feedbackId, String status, Integer staffId) {
        String sql = "UPDATE Feedback SET status = ?, handled_by_staff_id = ? WHERE feedback_id = ?";
        return jdbcTemplate.update(sql, status, staffId, feedbackId);
    }

    public int updateResponse(Integer feedbackId, String staffResponse, Integer staffId) {
        String sql = "UPDATE Feedback SET comments = ?, handled_by_staff_id = ?, status = 'IN_PROGRESS' WHERE feedback_id = ?";
        return jdbcTemplate.update(sql, staffResponse, staffId, feedbackId);
    }

    public int update(Integer feedbackId, Integer rating, String category, String comments) {
        String sql = "UPDATE Feedback SET rating = ?, category = ?, comments = ? WHERE feedback_id = ?";
        return jdbcTemplate.update(sql, rating, category, comments, feedbackId);
    }

    public int delete(Integer feedbackId) {
        String sql = "DELETE FROM Feedback WHERE feedback_id = ?";
        return jdbcTemplate.update(sql, feedbackId);
    }

    public List<Feedback> searchByKeyword(String keyword) {
        String sql = """
            SELECT f.feedback_id, f.guest_id, f.reservation_id, f.handled_by_staff_id,
                   f.rating, f.comments, f.category, f.status, f.submitted_date,
                   g.name AS guest_name, g.email AS guest_email, s.name AS staff_name, r.room_number
            FROM Feedback f
            LEFT JOIN Guest g ON f.guest_id = g.guest_id
            LEFT JOIN Reservation res ON f.reservation_id = res.reservation_id
            LEFT JOIN Room r ON res.room_id = r.room_id
            LEFT JOIN Staff s ON f.handled_by_staff_id = s.staff_id
            WHERE LOWER(f.comments) LIKE ? OR LOWER(f.category) LIKE ? OR LOWER(g.name) LIKE ?
            ORDER BY f.submitted_date DESC
        """;
        String pattern = "%" + keyword.toLowerCase().trim() + "%";
        return jdbcTemplate.query(sql, rowMapper, pattern, pattern, pattern);
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM Feedback WHERE status = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, status);
        return count != null ? count : 0;
    }

    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM Feedback";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
