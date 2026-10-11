package com.smartstay.repository;

import com.smartstay.model.Staff;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class StaffRepository {

    private final JdbcTemplate jdbcTemplate;

    public StaffRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            jdbcTemplate.execute("ALTER TABLE Staff ALTER COLUMN branch_id INT NULL");
        } catch (Exception ignored) {
        }
    }

    private final RowMapper<Staff> rowMapper = (rs, rowNum) -> {
        Staff staff = new Staff();
        staff.setStaffId(rs.getInt("staff_id"));
        int branchId = rs.getInt("branch_id");
        if (!rs.wasNull() && branchId > 0) {
            staff.setBranchId(branchId);
        } else {
            staff.setBranchId(null);
        }
        int supId = rs.getInt("supervisor_id");
        if (!rs.wasNull()) {
            staff.setSupervisorId(supId);
        }
        staff.setName(rs.getString("name"));
        staff.setEmail(rs.getString("email"));
        staff.setPhone(rs.getString("phone"));
        staff.setPassword(rs.getString("password"));
        staff.setRole(rs.getString("role"));
        Date hireDate = rs.getDate("hire_date");
        if (hireDate != null) {
            staff.setHireDate(hireDate.toLocalDate());
        }
        staff.setStatus(rs.getString("status"));
        int target = rs.getInt("target_bookings");
        if (!rs.wasNull()) {
            staff.setTargetBookings(target);
        }
        staff.setAccessLevel(rs.getString("access_level"));
        staff.setApprovalLevel(rs.getString("approval_level"));
        int sla = rs.getInt("response_sla_hrs");
        if (!rs.wasNull()) {
            staff.setResponseSlaHrs(sla);
        }

        try {
            staff.setBranchName(rs.getString("branch_name"));
        } catch (Exception ignored) {}

        try {
            staff.setSupervisorName(rs.getString("supervisor_name"));
        } catch (Exception ignored) {}

        return staff;
    };

    public List<Staff> findAll() {
        String sql = """
            SELECT s.staff_id, s.branch_id, s.supervisor_id, s.name, s.email, s.phone, s.password,
                   s.role, s.hire_date, s.status, s.target_bookings, s.access_level, s.approval_level,
                   s.response_sla_hrs, b.name AS branch_name, sup.name AS supervisor_name
            FROM Staff s
            LEFT JOIN Branch b ON s.branch_id = b.branch_id
            LEFT JOIN Staff sup ON s.supervisor_id = sup.staff_id
            ORDER BY s.staff_id ASC
        """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Staff> findById(Integer id) {
        String sql = """
            SELECT s.staff_id, s.branch_id, s.supervisor_id, s.name, s.email, s.phone, s.password,
                   s.role, s.hire_date, s.status, s.target_bookings, s.access_level, s.approval_level,
                   s.response_sla_hrs, b.name AS branch_name, sup.name AS supervisor_name
            FROM Staff s
            LEFT JOIN Branch b ON s.branch_id = b.branch_id
            LEFT JOIN Staff sup ON s.supervisor_id = sup.staff_id
            WHERE s.staff_id = ?
        """;
        try {
            Staff staff = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(staff);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Staff> findByEmail(String email) {
        String sql = """
            SELECT s.staff_id, s.branch_id, s.supervisor_id, s.name, s.email, s.phone, s.password,
                   s.role, s.hire_date, s.status, s.target_bookings, s.access_level, s.approval_level,
                   s.response_sla_hrs, b.name AS branch_name, sup.name AS supervisor_name
            FROM Staff s
            LEFT JOIN Branch b ON s.branch_id = b.branch_id
            LEFT JOIN Staff sup ON s.supervisor_id = sup.staff_id
            WHERE LOWER(s.email) = LOWER(?)
        """;
        try {
            Staff staff = jdbcTemplate.queryForObject(sql, rowMapper, email);
            return Optional.ofNullable(staff);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int save(Staff s) {
        String sql = """
            INSERT INTO Staff (branch_id, supervisor_id, name, email, phone, password, role,
                               hire_date, status, target_bookings, access_level, approval_level, response_sla_hrs)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (s.getBranchId() != null && s.getBranchId() > 0) {
                ps.setInt(1, s.getBranchId());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            if (s.getSupervisorId() != null) {
                ps.setInt(2, s.getSupervisorId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, s.getName());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getPhone());
            ps.setString(6, s.getPassword());
            ps.setString(7, s.getRole());
            if (s.getHireDate() != null) {
                ps.setDate(8, Date.valueOf(s.getHireDate()));
            } else {
                ps.setNull(8, java.sql.Types.DATE);
            }
            ps.setString(9, s.getStatus() != null ? s.getStatus() : "ACTIVE");
            if (s.getTargetBookings() != null) {
                ps.setInt(10, s.getTargetBookings());
            } else {
                ps.setNull(10, java.sql.Types.INTEGER);
            }
            ps.setString(11, s.getAccessLevel());
            ps.setString(12, s.getApprovalLevel());
            if (s.getResponseSlaHrs() != null) {
                ps.setInt(13, s.getResponseSlaHrs());
            } else {
                ps.setNull(13, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            s.setStaffId(key.intValue());
            return key.intValue();
        }
        return 0;
    }

    public int update(Staff s) {
        String sql = """
            UPDATE Staff SET branch_id = ?, supervisor_id = ?, name = ?, email = ?, phone = ?, role = ?,
                             target_bookings = ?, access_level = ?, approval_level = ?, response_sla_hrs = ?
            WHERE staff_id = ?
        """;
        return jdbcTemplate.update(sql,
                (s.getBranchId() != null && s.getBranchId() > 0) ? s.getBranchId() : null,
                s.getSupervisorId(),
                s.getName(),
                s.getEmail(),
                s.getPhone(),
                s.getRole(),
                s.getTargetBookings(),
                s.getAccessLevel(),
                s.getApprovalLevel(),
                s.getResponseSlaHrs(),
                s.getStaffId());
    }

    public int updateStatus(Integer staffId, String status) {
        String sql = "UPDATE Staff SET status = ? WHERE staff_id = ?";
        return jdbcTemplate.update(sql, status, staffId);
    }

    public int updatePassword(Integer staffId, String hashedPassword) {
        String sql = "UPDATE Staff SET password = ? WHERE staff_id = ?";
        return jdbcTemplate.update(sql, hashedPassword, staffId);
    }

    public int delete(Integer staffId) {
        String sql = "DELETE FROM Staff WHERE staff_id = ?";
        return jdbcTemplate.update(sql, staffId);
    }

    public int countActiveStaff() {
        String sql = "SELECT COUNT(*) FROM Staff WHERE status = 'ACTIVE'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
