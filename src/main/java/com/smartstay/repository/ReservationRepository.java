package com.smartstay.repository;

import com.smartstay.model.Reservation;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Reservation> rowMapper = (rs, rowNum) -> {
        Reservation res = new Reservation();
        res.setReservationId(rs.getInt("reservation_id"));
        res.setGuestId(rs.getInt("guest_id"));
        res.setRoomId(rs.getInt("room_id"));
        int staffId = rs.getInt("entered_by_staff_id");
        if (!rs.wasNull()) {
            res.setEnteredByStaffId(staffId);
        }
        Timestamp bookingTs = rs.getTimestamp("booking_date");
        if (bookingTs != null) {
            res.setBookingDate(bookingTs.toLocalDateTime());
        }
        Date inDate = rs.getDate("check_in_date");
        if (inDate != null) {
            res.setCheckInDate(inDate.toLocalDate());
        }
        Date outDate = rs.getDate("check_out_date");
        if (outDate != null) {
            res.setCheckOutDate(outDate.toLocalDate());
        }
        res.setNumGuests(rs.getInt("num_guests"));
        res.setStatus(rs.getString("status"));

        try {
            res.setGuestName(rs.getString("guest_name"));
            res.setGuestEmail(rs.getString("guest_email"));
            res.setGuestPhone(rs.getString("guest_phone"));
            res.setRoomNumber(rs.getString("room_number"));
            res.setRoomType(rs.getString("room_type"));
            res.setBranchName(rs.getString("branch_name"));
            res.setRoomPrice(rs.getBigDecimal("room_price"));
        } catch (Exception ignored) {}

        try {
            res.setStaffName(rs.getString("staff_name"));
        } catch (Exception ignored) {}

        return res;
    };

    public List<Reservation> findAll() {
        String sql = """
            SELECT res.reservation_id, res.guest_id, res.room_id, res.entered_by_staff_id,
                   res.booking_date, res.check_in_date, res.check_out_date, res.num_guests, res.status,
                   g.name AS guest_name, g.email AS guest_email, g.phone AS guest_phone,
                   r.room_number, r.type AS room_type, r.price AS room_price,
                   b.name AS branch_name, s.name AS staff_name
            FROM Reservation res
            JOIN Guest g ON res.guest_id = g.guest_id
            JOIN Room r ON res.room_id = r.room_id
            JOIN Branch b ON r.branch_id = b.branch_id
            LEFT JOIN Staff s ON res.entered_by_staff_id = s.staff_id
            ORDER BY res.booking_date DESC
        """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Reservation> findByGuestId(Integer guestId) {
        String sql = """
            SELECT res.reservation_id, res.guest_id, res.room_id, res.entered_by_staff_id,
                   res.booking_date, res.check_in_date, res.check_out_date, res.num_guests, res.status,
                   g.name AS guest_name, g.email AS guest_email, g.phone AS guest_phone,
                   r.room_number, r.type AS room_type, r.price AS room_price,
                   b.name AS branch_name, s.name AS staff_name
            FROM Reservation res
            JOIN Guest g ON res.guest_id = g.guest_id
            JOIN Room r ON res.room_id = r.room_id
            JOIN Branch b ON r.branch_id = b.branch_id
            LEFT JOIN Staff s ON res.entered_by_staff_id = s.staff_id
            WHERE res.guest_id = ?
            ORDER BY res.check_in_date DESC
        """;
        return jdbcTemplate.query(sql, rowMapper, guestId);
    }

    public Optional<Reservation> findById(Integer reservationId) {
        String sql = """
            SELECT res.reservation_id, res.guest_id, res.room_id, res.entered_by_staff_id,
                   res.booking_date, res.check_in_date, res.check_out_date, res.num_guests, res.status,
                   g.name AS guest_name, g.email AS guest_email, g.phone AS guest_phone,
                   r.room_number, r.type AS room_type, r.price AS room_price,
                   b.name AS branch_name, s.name AS staff_name
            FROM Reservation res
            JOIN Guest g ON res.guest_id = g.guest_id
            JOIN Room r ON res.room_id = r.room_id
            JOIN Branch b ON r.branch_id = b.branch_id
            LEFT JOIN Staff s ON res.entered_by_staff_id = s.staff_id
            WHERE res.reservation_id = ?
        """;
        try {
            Reservation res = jdbcTemplate.queryForObject(sql, rowMapper, reservationId);
            return Optional.ofNullable(res);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int save(Reservation res) {
        String sql = """
            INSERT INTO Reservation (guest_id, room_id, entered_by_staff_id, booking_date, check_in_date, check_out_date, num_guests, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, res.getGuestId());
            ps.setInt(2, res.getRoomId());
            if (res.getEnteredByStaffId() != null) {
                ps.setInt(3, res.getEnteredByStaffId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setTimestamp(4, res.getBookingDate() != null ? Timestamp.valueOf(res.getBookingDate()) : new Timestamp(System.currentTimeMillis()));
            ps.setDate(5, Date.valueOf(res.getCheckInDate()));
            ps.setDate(6, Date.valueOf(res.getCheckOutDate()));
            ps.setInt(7, res.getNumGuests() != null ? res.getNumGuests() : 1);
            ps.setString(8, res.getStatus() != null ? res.getStatus() : "CONFIRMED");
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            res.setReservationId(key.intValue());
            return key.intValue();
        }
        return 0;
    }

    public int updateDatesAndGuests(Integer reservationId, Date checkIn, Date checkOut, int numGuests) {
        String sql = "UPDATE Reservation SET check_in_date = ?, check_out_date = ?, num_guests = ?, status = 'MODIFIED' WHERE reservation_id = ?";
        return jdbcTemplate.update(sql, checkIn, checkOut, numGuests, reservationId);
    }

    public int updateStatus(Integer reservationId, String status) {
        String sql = "UPDATE Reservation SET status = ? WHERE reservation_id = ?";
        return jdbcTemplate.update(sql, status, reservationId);
    }

    public int delete(Integer reservationId) {
        String sql = "DELETE FROM Reservation WHERE reservation_id = ?";
        return jdbcTemplate.update(sql, reservationId);
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM Reservation WHERE status = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, status);
        return count != null ? count : 0;
    }

    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM Reservation";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
