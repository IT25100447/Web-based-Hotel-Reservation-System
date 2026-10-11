package com.smartstay.dto;

import java.io.Serializable;

public class UserSession implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id; // guestId or staffId
    private String name;
    private String email;
    private String userType; // "GUEST" or "STAFF"
    private String role; // "GUEST", "MANAGER", "RESERVATIONS", "FRONT_OFFICE", "GUEST_RELATIONS", "FINANCE", "IT"
    private Integer branchId; // for staff
    private String branchName; // resolved branch name for display

    public UserSession() {
    }

    public UserSession(Integer id, String name, String email, String userType, String role, Integer branchId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.userType = userType;
        this.role = role;
        this.branchId = branchId;
    }

    /**
     * Returns a human-readable version of the role name.
     * Converts DB values like "GUEST_RELATIONS" to "Guest Relations",
     * "FRONT_OFFICE" to "Front Office", etc.
     */
    public String getDisplayRole() {
        if (role == null) return "";
        String[] parts = role.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(Character.toUpperCase(part.charAt(0)));
                sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    public boolean isGuest() {
        return "GUEST".equalsIgnoreCase(userType);
    }

    public boolean isStaff() {
        return "STAFF".equalsIgnoreCase(userType);
    }

    public boolean isManager() {
        return "MANAGER".equalsIgnoreCase(role);
    }

    public boolean isReservations() {
        return "RESERVATIONS".equalsIgnoreCase(role) || isManager();
    }

    public boolean isFrontOffice() {
        return "FRONT_OFFICE".equalsIgnoreCase(role) || isManager();
    }

    public boolean isGuestRelations() {
        return "GUEST_RELATIONS".equalsIgnoreCase(role) || isManager();
    }

    public boolean isFinance() {
        return "FINANCE".equalsIgnoreCase(role) || isManager();
    }

    public boolean isIt() {
        return "IT".equalsIgnoreCase(role) || "IT_COORDINATOR".equalsIgnoreCase(role) || "IT Coordinator".equalsIgnoreCase(role);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}
