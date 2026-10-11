package com.smartstay.repository;

import com.smartstay.model.Invoice;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class InvoiceRepository {

    private final JdbcTemplate jdbcTemplate;

    public InvoiceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Invoice> rowMapper = (rs, rowNum) -> {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(rs.getInt("invoice_id"));
        invoice.setReservationId(rs.getInt("reservation_id"));
        invoice.setTotalAmount(rs.getBigDecimal("total_amount"));
        invoice.setOutstandingBalance(rs.getBigDecimal("outstanding_balance"));
        Timestamp ts = rs.getTimestamp("issue_date");
        if (ts != null) {
            invoice.setIssueDate(ts.toLocalDateTime());
        }

        try {
            invoice.setGuestName(rs.getString("guest_name"));
            invoice.setGuestEmail(rs.getString("guest_email"));
            invoice.setRoomNumber(rs.getString("room_number"));
            invoice.setRoomType(rs.getString("room_type"));
            invoice.setBranchName(rs.getString("branch_name"));
            Date inDate = rs.getDate("check_in_date");
            if (inDate != null) invoice.setCheckInDate(inDate.toLocalDate());
            Date outDate = rs.getDate("check_out_date");
            if (outDate != null) invoice.setCheckOutDate(outDate.toLocalDate());
        } catch (Exception ignored) {}

        return invoice;
    };

    public Optional<Invoice> findByReservationId(Integer reservationId) {
        String sql = """
            SELECT inv.invoice_id, inv.reservation_id, inv.total_amount, inv.outstanding_balance, inv.issue_date,
                   g.name AS guest_name, g.email AS guest_email,
                   r.room_number, r.type AS room_type, b.name AS branch_name,
                   res.check_in_date, res.check_out_date
            FROM Invoice inv
            JOIN Reservation res ON inv.reservation_id = res.reservation_id
            JOIN Guest g ON res.guest_id = g.guest_id
            JOIN Room r ON res.room_id = r.room_id
            JOIN Branch b ON r.branch_id = b.branch_id
            WHERE inv.reservation_id = ?
        """;
        try {
            Invoice inv = jdbcTemplate.queryForObject(sql, rowMapper, reservationId);
            return Optional.ofNullable(inv);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Invoice> findById(Integer invoiceId) {
        String sql = """
            SELECT inv.invoice_id, inv.reservation_id, inv.total_amount, inv.outstanding_balance, inv.issue_date,
                   g.name AS guest_name, g.email AS guest_email,
                   r.room_number, r.type AS room_type, b.name AS branch_name,
                   res.check_in_date, res.check_out_date
            FROM Invoice inv
            JOIN Reservation res ON inv.reservation_id = res.reservation_id
            JOIN Guest g ON res.guest_id = g.guest_id
            JOIN Room r ON res.room_id = r.room_id
            JOIN Branch b ON r.branch_id = b.branch_id
            WHERE inv.invoice_id = ?
        """;
        try {
            Invoice inv = jdbcTemplate.queryForObject(sql, rowMapper, invoiceId);
            return Optional.ofNullable(inv);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int save(Invoice invoice) {
        String sql = "INSERT INTO Invoice (reservation_id, total_amount, outstanding_balance, issue_date) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, invoice.getReservationId());
            ps.setBigDecimal(2, invoice.getTotalAmount());
            ps.setBigDecimal(3, invoice.getOutstandingBalance());
            ps.setTimestamp(4, invoice.getIssueDate() != null ? Timestamp.valueOf(invoice.getIssueDate()) : new Timestamp(System.currentTimeMillis()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            invoice.setInvoiceId(key.intValue());
            return key.intValue();
        }
        return 0;
    }

    public int reduceOutstandingBalance(Integer reservationId, BigDecimal amountPaid) {
        String sql = """
            UPDATE Invoice
            SET outstanding_balance = CASE
                WHEN outstanding_balance - ? < 0 THEN 0
                ELSE outstanding_balance - ?
            END
            WHERE reservation_id = ?
        """;
        return jdbcTemplate.update(sql, amountPaid, amountPaid, reservationId);
    }

    public int updateTotalAndBalance(Integer reservationId, BigDecimal newTotal, BigDecimal newBalance) {
        String sql = "UPDATE Invoice SET total_amount = ?, outstanding_balance = ? WHERE reservation_id = ?";
        return jdbcTemplate.update(sql, newTotal, newBalance, reservationId);
    }

    public int update(Integer invoiceId, BigDecimal totalAmount, BigDecimal outstandingBalance) {
        String sql = "UPDATE Invoice SET total_amount = ?, outstanding_balance = ? WHERE invoice_id = ?";
        return jdbcTemplate.update(sql, totalAmount, outstandingBalance, invoiceId);
    }

    public int delete(Integer invoiceId) {
        String sql = "DELETE FROM Invoice WHERE invoice_id = ?";
        return jdbcTemplate.update(sql, invoiceId);
    }

    public List<Invoice> findAll() {
        String sql = """
            SELECT inv.invoice_id, inv.reservation_id, inv.total_amount, inv.outstanding_balance, inv.issue_date,
                   g.name AS guest_name, g.email AS guest_email,
                   r.room_number, r.type AS room_type, b.name AS branch_name,
                   res.check_in_date, res.check_out_date
            FROM Invoice inv
            JOIN Reservation res ON inv.reservation_id = res.reservation_id
            JOIN Guest g ON res.guest_id = g.guest_id
            JOIN Room r ON res.room_id = r.room_id
            JOIN Branch b ON r.branch_id = b.branch_id
            ORDER BY inv.issue_date DESC
        """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    public BigDecimal getTotalOutstandingBalance() {
        String sql = "SELECT SUM(outstanding_balance) FROM Invoice";
        BigDecimal sum = jdbcTemplate.queryForObject(sql, BigDecimal.class);
        return sum != null ? sum : BigDecimal.ZERO;
    }
}
