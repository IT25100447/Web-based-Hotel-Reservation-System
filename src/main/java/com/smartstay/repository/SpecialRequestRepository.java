package com.smartstay.repository;

import com.smartstay.model.SpecialRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class SpecialRequestRepository {

    private final JdbcTemplate jdbcTemplate;

    public SpecialRequestRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SpecialRequest> rowMapper = (rs, rowNum) -> {
        SpecialRequest req = new SpecialRequest();
        req.setRequestId(rs.getInt("request_id"));
        req.setReservationId(rs.getInt("reservation_id"));
        req.setType(rs.getString("type"));
        req.setDescription(rs.getString("description"));
        return req;
    };

    public List<SpecialRequest> findByReservationId(Integer reservationId) {
        String sql = "SELECT request_id, reservation_id, type, description FROM SpecialRequest WHERE reservation_id = ?";
        return jdbcTemplate.query(sql, rowMapper, reservationId);
    }

    public int save(SpecialRequest request) {
        String sql = "INSERT INTO SpecialRequest (reservation_id, type, description) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, request.getReservationId());
            ps.setString(2, request.getType());
            ps.setString(3, request.getDescription());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            request.setRequestId(key.intValue());
            return key.intValue();
        }
        return 0;
    }

    public int delete(Integer requestId) {
        String sql = "DELETE FROM SpecialRequest WHERE request_id = ?";
        return jdbcTemplate.update(sql, requestId);
    }
}
