package com.smartstay.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardStatsDto {
    private int totalRooms;
    private int availableRooms;
    private int occupiedRooms;
    private int maintenanceRooms;
    private double occupancyRate;

    private int totalReservations;
    private int activeReservations;
    private int cancelledReservations;

    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal totalOutstanding = BigDecimal.ZERO;

    private int totalComplaints;
    private int openComplaints;
    private int resolvedComplaints;

    private int totalStaff;
    private List<Map<String, Object>> branchPerformance = new ArrayList<>();
    private List<Map<String, Object>> recentBookings = new ArrayList<>();

    public DashboardStatsDto() {
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(int totalRooms) {
        this.totalRooms = totalRooms;
    }

    public int getAvailableRooms() {
        return availableRooms;
    }

    public void setAvailableRooms(int availableRooms) {
        this.availableRooms = availableRooms;
    }

    public int getOccupiedRooms() {
        return occupiedRooms;
    }

    public void setOccupiedRooms(int occupiedRooms) {
        this.occupiedRooms = occupiedRooms;
    }

    public int getMaintenanceRooms() {
        return maintenanceRooms;
    }

    public void setMaintenanceRooms(int maintenanceRooms) {
        this.maintenanceRooms = maintenanceRooms;
    }

    public double getOccupancyRate() {
        return occupancyRate;
    }

    public void setOccupancyRate(double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }

    public int getTotalReservations() {
        return totalReservations;
    }

    public void setTotalReservations(int totalReservations) {
        this.totalReservations = totalReservations;
    }

    public int getActiveReservations() {
        return activeReservations;
    }

    public void setActiveReservations(int activeReservations) {
        this.activeReservations = activeReservations;
    }

    public int getCancelledReservations() {
        return cancelledReservations;
    }

    public void setCancelledReservations(int cancelledReservations) {
        this.cancelledReservations = cancelledReservations;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalOutstanding() {
        return totalOutstanding != null ? totalOutstanding : BigDecimal.ZERO;
    }

    public void setTotalOutstanding(BigDecimal totalOutstanding) {
        this.totalOutstanding = totalOutstanding;
    }

    public int getTotalComplaints() {
        return totalComplaints;
    }

    public void setTotalComplaints(int totalComplaints) {
        this.totalComplaints = totalComplaints;
    }

    public int getOpenComplaints() {
        return openComplaints;
    }

    public void setOpenComplaints(int openComplaints) {
        this.openComplaints = openComplaints;
    }

    public int getResolvedComplaints() {
        return resolvedComplaints;
    }

    public void setResolvedComplaints(int resolvedComplaints) {
        this.resolvedComplaints = resolvedComplaints;
    }

    public int getTotalStaff() {
        return totalStaff;
    }

    public void setTotalStaff(int totalStaff) {
        this.totalStaff = totalStaff;
    }

    public List<Map<String, Object>> getBranchPerformance() {
        return branchPerformance;
    }

    public void setBranchPerformance(List<Map<String, Object>> branchPerformance) {
        this.branchPerformance = branchPerformance;
    }

    public List<Map<String, Object>> getRecentBookings() {
        return recentBookings;
    }

    public void setRecentBookings(List<Map<String, Object>> recentBookings) {
        this.recentBookings = recentBookings;
    }
}
