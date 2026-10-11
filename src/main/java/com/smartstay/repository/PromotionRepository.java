package com.smartstay.repository;

import com.smartstay.model.Promotion;
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
public class PromotionRepository {

    private final JdbcTemplate jdbcTemplate;

    public PromotionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Promotion> rowMapper = (rs, rowNum) -> {
        Promotion p = new Promotion();
        p.setPromoId(rs.getInt("promo_id"));
        p.setName(rs.getString("name"));
        p.setDiscountPct(rs.getBigDecimal("discount_pct"));
        Date sDate = rs.getDate("start_date");
        if (sDate != null) p.setStartDate(sDate.toLocalDate());
        Date eDate = rs.getDate("end_date");
        if (eDate != null) p.setEndDate(eDate.toLocalDate());
        return p;
    };

    public List<Promotion> findAll() {
        String sql = "SELECT promo_id, name, discount_pct, start_date, end_date FROM Promotion ORDER BY start_date DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Promotion> findActivePromotions() {
        String sql = """
            SELECT promo_id, name, discount_pct, start_date, end_date
            FROM Promotion
            WHERE CAST(GETDATE() AS DATE) BETWEEN start_date AND end_date
            ORDER BY discount_pct DESC
        """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Promotion> findById(Integer promoId) {
        String sql = "SELECT promo_id, name, discount_pct, start_date, end_date FROM Promotion WHERE promo_id = ?";
        try {
            Promotion p = jdbcTemplate.queryForObject(sql, rowMapper, promoId);
            return Optional.ofNullable(p);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int save(Promotion promo) {
        String sql = "INSERT INTO Promotion (name, discount_pct, start_date, end_date) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, promo.getName());
            ps.setBigDecimal(2, promo.getDiscountPct());
            ps.setDate(3, Date.valueOf(promo.getStartDate()));
            ps.setDate(4, Date.valueOf(promo.getEndDate()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            promo.setPromoId(key.intValue());
            return key.intValue();
        }
        return 0;
    }

    public void linkRoomPromotion(Integer roomId, Integer promoId) {
        String sql = "INSERT INTO Room_Promotion (room_id, promo_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(sql, roomId, promoId);
        } catch (Exception ignored) {}
    }

    public void unlinkRoomPromotion(Integer roomId, Integer promoId) {
        String sql = "DELETE FROM Room_Promotion WHERE room_id = ? AND promo_id = ?";
        jdbcTemplate.update(sql, roomId, promoId);
    }

    public void delete(Integer promoId) {
        // First unlink from any rooms
        jdbcTemplate.update("DELETE FROM Room_Promotion WHERE promo_id = ?", promoId);
        jdbcTemplate.update("DELETE FROM Promotion WHERE promo_id = ?", promoId);
    }
}
