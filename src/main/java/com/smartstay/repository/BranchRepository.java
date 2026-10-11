package com.smartstay.repository;

import com.smartstay.model.Branch;
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
public class BranchRepository {

    private final JdbcTemplate jdbcTemplate;

    public BranchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Branch> rowMapper = (rs, rowNum) -> {
        Branch branch = new Branch();
        branch.setBranchId(rs.getInt("branch_id"));
        branch.setName(rs.getString("name"));
        branch.setAddress(rs.getString("address"));
        branch.setContactNumber(rs.getString("contact_number"));
        return branch;
    };

    public List<Branch> findAll() {
        String sql = "SELECT branch_id, name, address, contact_number FROM Branch ORDER BY name ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Branch> findById(Integer id) {
        String sql = "SELECT branch_id, name, address, contact_number FROM Branch WHERE branch_id = ?";
        try {
            Branch branch = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(branch);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int save(Branch branch) {
        String sql = "INSERT INTO Branch (name, address, contact_number) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, branch.getName());
            ps.setString(2, branch.getAddress());
            ps.setString(3, branch.getContactNumber());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            branch.setBranchId(key.intValue());
            return key.intValue();
        }
        return 0;
    }

    public int update(Branch branch) {
        String sql = "UPDATE Branch SET name = ?, address = ?, contact_number = ? WHERE branch_id = ?";
        return jdbcTemplate.update(sql, branch.getName(), branch.getAddress(), branch.getContactNumber(), branch.getBranchId());
    }

    public int delete(Integer id) {
        String sql = "DELETE FROM Branch WHERE branch_id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
