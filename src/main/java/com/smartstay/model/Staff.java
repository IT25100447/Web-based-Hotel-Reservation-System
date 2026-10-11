package com.smartstay.model;

import java.time.LocalDate;

public class Staff {
    private Integer staffId;
    private Integer branchId;
    private Integer supervisorId;
    private String name;
    private String email;
    private String phone;
    private String password;
    private String role; // 'MANAGER','RESERVATIONS','FRONT_OFFICE','GUEST_RELATIONS','FINANCE','IT'
    private LocalDate hireDate;
    private String status; // 'ACTIVE','DEACTIVATED'
    private Integer targetBookings;
    private String accessLevel;
    private String approvalLevel;
    private Integer responseSlaHrs;

    // Transient fields for joins
    private String branchName;
    private String supervisorName;

    public Staff() {
    }

    public Integer getStaffId() {
        return staffId;
    }

    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }

    public Integer getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(Integer supervisorId) {
        this.supervisorId = supervisorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTargetBookings() {
        return targetBookings;
    }

    public void setTargetBookings(Integer targetBookings) {
        this.targetBookings = targetBookings;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getApprovalLevel() {
        return approvalLevel;
    }

    public void setApprovalLevel(String approvalLevel) {
        this.approvalLevel = approvalLevel;
    }

    public Integer getResponseSlaHrs() {
        return responseSlaHrs;
    }

    public void setResponseSlaHrs(Integer responseSlaHrs) {
        this.responseSlaHrs = responseSlaHrs;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }
}
