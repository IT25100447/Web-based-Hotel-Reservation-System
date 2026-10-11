package com.smartstay.model;

import java.time.LocalDateTime;

public class ActivityLog {
    private Integer logId;
    private Integer staffId;
    private String action;
    private LocalDateTime timestamp;

    // Transient fields
    private String staffName;
    private String staffRole;

    public ActivityLog() {
    }

    public ActivityLog(Integer staffId, String action) {
        this.staffId = staffId;
        this.action = action;
        this.timestamp = LocalDateTime.now();
    }

    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public Integer getStaffId() {
        return staffId;
    }

    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getStaffRole() {
        return staffRole;
    }

    public void setStaffRole(String staffRole) {
        this.staffRole = staffRole;
    }
}
