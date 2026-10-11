package com.smartstay.repository;

import com.smartstay.model.ActivityLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class ActivityLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public ActivityLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ActivityLog> rowMapper = (rs, rowNum) -> {
        ActivityLog log = new ActivityLog();
        log.setLogId(rs.getInt("log_id"));
        log.setStaffId(rs.getInt("staff_id"));
        log.setAction(rs.getString("action"));
        Timestamp ts = rs.getTimestamp("timestamp");
        if (ts != null) {
            log.setTimestamp(ts.toLocalDateTime());
        }

        try {
            log.setStaffName(rs.getString("staff_name"));
            log.setStaffRole(rs.getString("staff_role"));
        } catch (Exception ignored) {}

        return log;
    };

    public List<ActivityLog> findRecent(int limit) {
        String sql = """
            SELECT TOP (?) a.log_id, a.staff_id, a.action, a.timestamp, s.name AS staff_name, s.role AS staff_role
            FROM ActivityLog a
            JOIN Staff s ON a.staff_id = s.staff_id
            ORDER BY a.timestamp DESC
        """;
        return jdbcTemplate.query(sql, rowMapper, limit);
    }

    public List<ActivityLog> findByStaffId(Integer staffId) {
        String sql = """
            SELECT a.log_id, a.staff_id, a.action, a.timestamp, s.name AS staff_name, s.role AS staff_role
            FROM ActivityLog a
            JOIN Staff s ON a.staff_id = s.staff_id
            WHERE a.staff_id = ?
            ORDER BY a.timestamp DESC
        """;
        return jdbcTemplate.query(sql, rowMapper, staffId);
    }

    public int save(ActivityLog log) {
        String sql = "INSERT INTO ActivityLog (staff_id, action, timestamp) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, log.getStaffId());
            ps.setString(2, log.getAction());
            ps.setTimestamp(3, log.getTimestamp() != null ? Timestamp.valueOf(log.getTimestamp()) : new Timestamp(System.currentTimeMillis()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            log.setLogId(key.intValue());
            return key.intValue();
        }
        return 0;
    }
}
