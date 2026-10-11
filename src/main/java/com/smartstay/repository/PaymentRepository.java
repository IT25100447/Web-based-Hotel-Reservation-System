package com.smartstay.repository;

import com.smartstay.model.Payment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Payment> rowMapper = (rs, rowNum) -> {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setReservationId(rs.getInt("reservation_id"));
        int staffId = rs.getInt("processed_by_staff_id");
        if (!rs.wasNull()) {
            payment.setProcessedByStaffId(staffId);
        }
        payment.setMethod(rs.getString("method"));
        payment.setAmount(rs.getBigDecimal("amount"));
        Timestamp ts = rs.getTimestamp("payment_date");
        if (ts != null) {
            payment.setPaymentDate(ts.toLocalDateTime());
        }
        payment.setReferenceNumber(rs.getString("reference_number"));
        payment.setStatus(rs.getString("status"));

        try {
            payment.setStaffName(rs.getString("staff_name"));
        } catch (Exception ignored) {}
        try {
            payment.setGuestName(rs.getString("guest_name"));
        } catch (Exception ignored) {}
        try {
            payment.setRoomNumber(rs.getString("room_number"));
        } catch (Exception ignored) {}

        return payment;
    };

    public List<Payment> findAll() {
        String sql = """
            SELECT p.payment_id, p.reservation_id, p.processed_by_staff_id, p.method, p.amount,
                   p.payment_date, p.reference_number, p.status,
                   s.name AS staff_name, g.name AS guest_name, r.room_number
            FROM Payment p
            LEFT JOIN Staff s ON p.processed_by_staff_id = s.staff_id
            JOIN Reservation res ON p.reservation_id = res.reservation_id
            JOIN Guest g ON res.guest_id = g.guest_id
            JOIN Room r ON res.room_id = r.room_id
            ORDER BY p.payment_date DESC
        """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Payment> findByReservationId(Integer reservationId) {
        String sql = """
            SELECT p.payment_id, p.reservation_id, p.processed_by_staff_id, p.method, p.amount,
                   p.payment_date, p.reference_number, p.status,
                   s.name AS staff_name, g.name AS guest_name, r.room_number
            FROM Payment p
            LEFT JOIN Staff s ON p.processed_by_staff_id = s.staff_id
            JOIN Reservation res ON p.reservation_id = res.reservation_id
            JOIN Guest g ON res.guest_id = g.guest_id
            JOIN Room r ON res.room_id = r.room_id
            WHERE p.reservation_id = ?
            ORDER BY p.payment_date DESC
        """;
        return jdbcTemplate.query(sql, rowMapper, reservationId);
    }

    public List<Payment> findByGuestId(Integer guestId) {
        String sql = """
            SELECT p.payment_id, p.reservation_id, p.processed_by_staff_id, p.method, p.amount,
                   p.payment_date, p.reference_number, p.status,
                   s.name AS staff_name, g.name AS guest_name, r.room_number
            FROM Payment p
            LEFT JOIN Staff s ON p.processed_by_staff_id = s.staff_id
            JOIN Reservation res ON p.reservation_id = res.reservation_id
            JOIN Guest g ON res.guest_id = g.guest_id
            JOIN Room r ON res.room_id = r.room_id
            WHERE res.guest_id = ?
            ORDER BY p.payment_date DESC
        """;
        return jdbcTemplate.query(sql, rowMapper, guestId);
    }

    public List<Payment> findPendingVerification() {
        String sql = """
            SELECT p.payment_id, p.reservation_id, p.processed_by_staff_id, p.method, p.amount,
                   p.payment_date, p.reference_number, p.status,
                   s.name AS staff_name, g.name AS guest_name, r.room_number
            FROM Payment p
            LEFT JOIN Staff s ON p.processed_by_staff_id = s.staff_id
            JOIN Reservation res ON p.reservation_id = res.reservation_id
            JOIN Guest g ON res.guest_id = g.guest_id
            JOIN Room r ON res.room_id = r.room_id
            WHERE p.status = 'PENDING'
            ORDER BY p.payment_date ASC
        """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Payment> findById(Integer paymentId) {
        String sql = """
            SELECT p.payment_id, p.reservation_id, p.processed_by_staff_id, p.method, p.amount,
                   p.payment_date, p.reference_number, p.status,
                   s.name AS staff_name, g.name AS guest_name, r.room_number
            FROM Payment p
            LEFT JOIN Staff s ON p.processed_by_staff_id = s.staff_id
            JOIN Reservation res ON p.reservation_id = res.reservation_id
            JOIN Guest g ON res.guest_id = g.guest_id
            JOIN Room r ON res.room_id = r.room_id
            WHERE p.payment_id = ?
        """;
        try {
            Payment payment = jdbcTemplate.queryForObject(sql, rowMapper, paymentId);
            return Optional.ofNullable(payment);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int save(Payment payment) {
        String sql = """
            INSERT INTO Payment (reservation_id, processed_by_staff_id, method, amount, payment_date, reference_number, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, payment.getReservationId());
            if (payment.getProcessedByStaffId() != null) {
                ps.setInt(2, payment.getProcessedByStaffId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, payment.getMethod());
            ps.setBigDecimal(4, payment.getAmount());
            ps.setTimestamp(5, payment.getPaymentDate() != null ? Timestamp.valueOf(payment.getPaymentDate()) : new Timestamp(System.currentTimeMillis()));
            ps.setString(6, payment.getReferenceNumber());
            ps.setString(7, payment.getStatus() != null ? payment.getStatus() : "PENDING");
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            payment.setPaymentId(key.intValue());
            return key.intValue();
        }
        return 0;
    }

    public int updateStatus(Integer paymentId, String status, Integer staffId) {
        String sql = "UPDATE Payment SET status = ?, processed_by_staff_id = ? WHERE payment_id = ?";
        return jdbcTemplate.update(sql, status, staffId, paymentId);
    }

    public int updateMethod(Integer paymentId, String method) {
        String sql = "UPDATE Payment SET method = ? WHERE payment_id = ?";
        return jdbcTemplate.update(sql, method, paymentId);
    }

    public int delete(Integer paymentId) {
        String sql = "DELETE FROM Payment WHERE payment_id = ?";
        return jdbcTemplate.update(sql, paymentId);
    }

    public BigDecimal getTotalRevenue() {
        String sql = "SELECT SUM(amount) FROM Payment WHERE status = 'VERIFIED'";
        BigDecimal sum = jdbcTemplate.queryForObject(sql, BigDecimal.class);
        return sum != null ? sum : BigDecimal.ZERO;
    }
}
