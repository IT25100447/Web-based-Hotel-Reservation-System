package com.smartstay.repository;

import com.smartstay.model.Guest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class GuestRepository {

    private final JdbcTemplate jdbcTemplate;

    public GuestRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Guest> rowMapper = (rs, rowNum) -> {
        Guest guest = new Guest();
        guest.setGuestId(rs.getInt("guest_id"));
        guest.setName(rs.getString("name"));
        guest.setEmail(rs.getString("email"));
        guest.setPhone(rs.getString("phone"));
        guest.setAddress(rs.getString("address"));
        guest.setPassword(rs.getString("password"));
        return guest;
    };

    public List<Guest> findAll() {
        String sql = "SELECT guest_id, name, email, phone, address, password FROM Guest ORDER BY name ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Guest> findById(Integer id) {
        String sql = "SELECT guest_id, name, email, phone, address, password FROM Guest WHERE guest_id = ?";
        try {
            Guest guest = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(guest);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Guest> findByEmail(String email) {
        String sql = "SELECT guest_id, name, email, phone, address, password FROM Guest WHERE LOWER(email) = LOWER(?)";
        try {
            Guest guest = jdbcTemplate.queryForObject(sql, rowMapper, email);
            return Optional.ofNullable(guest);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int save(Guest guest) {
        String sql = "INSERT INTO Guest (name, email, phone, address, password) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, guest.getName());
            ps.setString(2, guest.getEmail());
            ps.setString(3, guest.getPhone());
            ps.setString(4, guest.getAddress());
            ps.setString(5, guest.getPassword());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            guest.setGuestId(key.intValue());
            return key.intValue();
        }
        return 0;
    }

    public int update(Guest guest) {
        String sql = "UPDATE Guest SET name = ?, phone = ?, address = ? WHERE guest_id = ?";
        return jdbcTemplate.update(sql, guest.getName(), guest.getPhone(), guest.getAddress(), guest.getGuestId());
    }

    public int updatePassword(Integer guestId, String hashedPassword) {
        String sql = "UPDATE Guest SET password = ? WHERE guest_id = ?";
        return jdbcTemplate.update(sql, hashedPassword, guestId);
    }

    public int delete(Integer guestId) {
        String sql = "DELETE FROM Guest WHERE guest_id = ?";
        return jdbcTemplate.update(sql, guestId);
    }

    public List<Guest> searchGuests(String query) {
        String sql = "SELECT guest_id, name, email, phone, address, password FROM Guest WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ? ORDER BY name ASC";
        String pattern = "%" + query.toLowerCase().trim() + "%";
        return jdbcTemplate.query(sql, rowMapper, pattern, pattern);
    }

    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM Guest";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
