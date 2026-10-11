package com.smartstay.repository;

import com.smartstay.dto.RoomFilterDto;
import com.smartstay.model.Room;
import com.smartstay.model.RoomFacility;
import com.smartstay.model.RoomImage;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class RoomRepository {

    private final JdbcTemplate jdbcTemplate;

    public RoomRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Room> rowMapper = (rs, rowNum) -> {
        Room room = new Room();
        room.setRoomId(rs.getInt("room_id"));
        room.setBranchId(rs.getInt("branch_id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setType(rs.getString("type"));
        room.setPrice(rs.getBigDecimal("price"));
        room.setStatus(rs.getString("status"));

        try {
            room.setBranchName(rs.getString("branch_name"));
        } catch (Exception ignored) {}

        return room;
    };

    public List<Room> findAll() {
        String sql = """
            SELECT r.room_id, r.branch_id, r.room_number, r.type, r.price, r.status, b.name AS branch_name
            FROM Room r
            JOIN Branch b ON r.branch_id = b.branch_id
            ORDER BY r.room_number ASC
        """;
        List<Room> rooms = jdbcTemplate.query(sql, rowMapper);
        populateRoomDetails(rooms);
        return rooms;
    }

    public Optional<Room> findById(Integer id) {
        String sql = """
            SELECT r.room_id, r.branch_id, r.room_number, r.type, r.price, r.status, b.name AS branch_name
            FROM Room r
            JOIN Branch b ON r.branch_id = b.branch_id
            WHERE r.room_id = ?
        """;
        try {
            Room room = jdbcTemplate.queryForObject(sql, rowMapper, id);
            if (room != null) {
                populateSingleRoomDetails(room);
            }
            return Optional.ofNullable(room);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Room> searchRooms(RoomFilterDto filter) {
        StringBuilder sql = new StringBuilder("""
            SELECT r.room_id, r.branch_id, r.room_number, r.type, r.price, r.status, b.name AS branch_name
            FROM Room r
            JOIN Branch b ON r.branch_id = b.branch_id
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();

        if (filter.getBranchId() != null) {
            sql.append(" AND r.branch_id = ?");
            params.add(filter.getBranchId());
        }
        if (filter.getType() != null && !filter.getType().isBlank()) {
            sql.append(" AND LOWER(r.type) LIKE ?");
            params.add("%" + filter.getType().toLowerCase().trim() + "%");
        }
        if (filter.getMaxPrice() != null) {
            sql.append(" AND r.price <= ?");
            params.add(filter.getMaxPrice());
        }

        // Availability filter if dates are provided
        if (filter.getCheckInDate() != null && filter.getCheckOutDate() != null) {
            sql.append(" AND r.status = 'AVAILABLE'");
            sql.append("""
                AND r.room_id NOT IN (
                    SELECT res.room_id FROM Reservation res
                    WHERE res.status NOT IN ('CANCELLED')
                      AND (res.check_in_date < ? AND res.check_out_date > ?)
                )
            """);
            params.add(Date.valueOf(filter.getCheckOutDate()));
            params.add(Date.valueOf(filter.getCheckInDate()));
        }

        sql.append(" ORDER BY r.price ASC");
        List<Room> rooms = jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
        populateRoomDetails(rooms);
        return rooms;
    }

    public boolean isRoomAvailable(Integer roomId, LocalDate checkInDate, LocalDate checkOutDate, Integer excludeReservationId) {
        String sql = """
            SELECT COUNT(*) FROM Reservation
            WHERE room_id = ?
              AND status NOT IN ('CANCELLED')
              AND (? IS NULL OR reservation_id <> ?)
              AND (check_in_date < ? AND check_out_date > ?)
        """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                roomId,
                excludeReservationId,
                excludeReservationId,
                Date.valueOf(checkOutDate),
                Date.valueOf(checkInDate));
        return count != null && count == 0;
    }

    public int save(Room room) {
        String sql = "INSERT INTO Room (branch_id, room_number, type, price, status) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, room.getBranchId());
            ps.setString(2, room.getRoomNumber());
            ps.setString(3, room.getType());
            ps.setBigDecimal(4, room.getPrice());
            ps.setString(5, room.getStatus() != null ? room.getStatus() : "AVAILABLE");
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            room.setRoomId(key.intValue());
            return key.intValue();
        }
        return 0;
    }

    public int update(Room room) {
        String sql = "UPDATE Room SET branch_id = ?, room_number = ?, type = ?, price = ?, status = ? WHERE room_id = ?";
        return jdbcTemplate.update(sql,
                room.getBranchId(),
                room.getRoomNumber(),
                room.getType(),
                room.getPrice(),
                room.getStatus(),
                room.getRoomId());
    }

    public int updateStatus(Integer roomId, String status) {
        String sql = "UPDATE Room SET status = ? WHERE room_id = ?";
        return jdbcTemplate.update(sql, status, roomId);
    }

    public int delete(Integer roomId) {
        String sql = "DELETE FROM Room WHERE room_id = ?";
        return jdbcTemplate.update(sql, roomId);
    }

    // Images
    // Images
    public List<RoomImage> getImagesForRoom(Integer roomId) {
        try {
            String sql = "SELECT image_id, room_id, image_url FROM RoomImage WHERE room_id = ?";
            return jdbcTemplate.query(sql, (rs, rowNum) ->
                    new RoomImage(rs.getInt("image_id"), rs.getInt("room_id"), rs.getString("image_url")), roomId);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void addImage(Integer roomId, String imageUrl) {
        String sql = "INSERT INTO RoomImage (room_id, image_url) VALUES (?, ?)";
        jdbcTemplate.update(sql, roomId, imageUrl);
    }

    public void deleteImage(Integer imageId) {
        String sql = "DELETE FROM RoomImage WHERE image_id = ?";
        jdbcTemplate.update(sql, imageId);
    }

    // Facilities
    public List<String> getFacilitiesForRoom(Integer roomId) {
        try {
            String sql = "SELECT facility_name FROM RoomFacility WHERE room_id = ?";
            return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("facility_name"), roomId);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void addFacility(Integer roomId, String facilityName) {
        String sql = "INSERT INTO RoomFacility (room_id, facility_name) VALUES (?, ?)";
        jdbcTemplate.update(sql, roomId, facilityName);
    }

    public void deleteFacilitiesByRoom(Integer roomId) {
        String sql = "DELETE FROM RoomFacility WHERE room_id = ?";
        jdbcTemplate.update(sql, roomId);
    }

    // Promotions
    public BigDecimal getActiveDiscountForRoom(Integer roomId) {
        String sql = """
            SELECT TOP 1 p.discount_pct
            FROM Promotion p
            JOIN Room_Promotion rp ON p.promo_id = rp.promo_id
            WHERE rp.room_id = ?
              AND CAST(GETDATE() AS DATE) BETWEEN p.start_date AND p.end_date
            ORDER BY p.discount_pct DESC
        """;
        try {
            BigDecimal discount = jdbcTemplate.queryForObject(sql, BigDecimal.class, roomId);
            return discount != null ? discount : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private void populateSingleRoomDetails(Room room) {
        room.setImages(getImagesForRoom(room.getRoomId()));
        room.setFacilities(getFacilitiesForRoom(room.getRoomId()));
        room.setActiveDiscountPct(getActiveDiscountForRoom(room.getRoomId()));
    }

    private void populateRoomDetails(List<Room> rooms) {
        for (Room r : rooms) {
            populateSingleRoomDetails(r);
        }
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM Room WHERE status = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, status);
        return count != null ? count : 0;
    }

    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM Room";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
