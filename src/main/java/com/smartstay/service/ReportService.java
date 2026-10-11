package com.smartstay.service;

import com.smartstay.dto.DashboardStatsDto;
import com.smartstay.repository.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final JdbcTemplate jdbcTemplate;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final FeedbackRepository feedbackRepository;
    private final StaffRepository staffRepository;

    public ReportService(JdbcTemplate jdbcTemplate,
                         RoomRepository roomRepository,
                         ReservationRepository reservationRepository,
                         PaymentRepository paymentRepository,
                         InvoiceRepository invoiceRepository,
                         FeedbackRepository feedbackRepository,
                         StaffRepository staffRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.feedbackRepository = feedbackRepository;
        this.staffRepository = staffRepository;
    }

    public DashboardStatsDto getManagerDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        int totalRooms = roomRepository.countTotal();
        int availableRooms = roomRepository.countByStatus("AVAILABLE");
        int occupiedRooms = roomRepository.countByStatus("OCCUPIED");
        int maintenanceRooms = roomRepository.countByStatus("MAINTENANCE");

        stats.setTotalRooms(totalRooms);
        stats.setAvailableRooms(availableRooms);
        stats.setOccupiedRooms(occupiedRooms);
        stats.setMaintenanceRooms(maintenanceRooms);

        if (totalRooms > 0) {
            double rate = ((double) occupiedRooms / totalRooms) * 100.0;
            stats.setOccupancyRate(BigDecimal.valueOf(rate).setScale(1, RoundingMode.HALF_UP).doubleValue());
        }

        int totalRes = reservationRepository.countTotal();
        int cancelledRes = reservationRepository.countByStatus("CANCELLED");
        stats.setTotalReservations(totalRes);
        stats.setCancelledReservations(cancelledRes);
        stats.setActiveReservations(totalRes - cancelledRes);

        stats.setTotalRevenue(paymentRepository.getTotalRevenue());
        stats.setTotalOutstanding(invoiceRepository.getTotalOutstandingBalance());

        int totalFb = feedbackRepository.countTotal();
        int openFb = feedbackRepository.countByStatus("OPEN");
        int resFb = feedbackRepository.countByStatus("RESOLVED");
        stats.setTotalComplaints(totalFb);
        stats.setOpenComplaints(openFb);
        stats.setResolvedComplaints(resFb);

        stats.setTotalStaff(staffRepository.countActiveStaff());

        // Branch performance calculation
        String branchSql = """
            SELECT b.name AS branch_name,
                   COUNT(DISTINCT res.reservation_id) AS booking_count,
                   ISNULL(SUM(CASE WHEN p.status = 'VERIFIED' THEN p.amount ELSE 0 END), 0) AS revenue
            FROM Branch b
            LEFT JOIN Room r ON b.branch_id = r.branch_id
            LEFT JOIN Reservation res ON r.room_id = res.room_id
            LEFT JOIN Payment p ON res.reservation_id = p.reservation_id
            GROUP BY b.branch_id, b.name
            ORDER BY revenue DESC
        """;
        List<Map<String, Object>> branchPerformance = jdbcTemplate.queryForList(branchSql);
        stats.setBranchPerformance(branchPerformance);

        // Recent bookings
        String recentSql = """
            SELECT TOP 5 res.reservation_id, res.booking_date, res.check_in_date, res.status,
                         g.name AS guest_name, r.room_number, b.name AS branch_name
            FROM Reservation res
            JOIN Guest g ON res.guest_id = g.guest_id
            JOIN Room r ON res.room_id = r.room_id
            JOIN Branch b ON r.branch_id = b.branch_id
            ORDER BY res.booking_date DESC
        """;
        stats.setRecentBookings(jdbcTemplate.queryForList(recentSql));

        return stats;
    }
}
