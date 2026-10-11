package com.smartstay.service;

import com.smartstay.exception.ResourceNotFoundException;
import com.smartstay.exception.ValidationException;
import com.smartstay.model.Staff;
import com.smartstay.repository.StaffRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class StaffService {

    private final StaffRepository staffRepository;
    private final ActivityLogService activityLogService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public StaffService(StaffRepository staffRepository, ActivityLogService activityLogService) {
        this.staffRepository = staffRepository;
        this.activityLogService = activityLogService;
    }

    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    public Staff getStaffById(Integer id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + id));
    }

    @Transactional
    public Staff createStaff(Staff staff, Integer sessionStaffId) {
        if (staffRepository.findByEmail(staff.getEmail()).isPresent()) {
            // Check if another staff member already uses this email address
            throw new ValidationException("Staff email already in use.");
        }

        if (staff.getPassword() == null || staff.getPassword().isBlank()) {
            staff.setPassword("Password123!");
        }
        staff.setPassword(passwordEncoder.encode(staff.getPassword()));

        if (staff.getHireDate() == null) {
            staff.setHireDate(LocalDate.now());
        }
        if (staff.getStatus() == null) {
            staff.setStatus("ACTIVE");
        }

        int id = staffRepository.save(staff);
        staff.setStaffId(id);
        activityLogService.log(sessionStaffId, "Created staff account for " + staff.getName() + " (Role: " + staff.getRole() + ")");
        return staff;
    }

    @Transactional
    public Staff updateStaff(Staff staff, Integer sessionStaffId) {
        Staff existing = getStaffById(staff.getStaffId());
        if (staff.getName() != null && !staff.getName().isBlank()) existing.setName(staff.getName());
        if (staff.getEmail() != null && !staff.getEmail().isBlank()) existing.setEmail(staff.getEmail());
        if (staff.getPhone() != null) existing.setPhone(staff.getPhone());
        if (staff.getRole() != null && !staff.getRole().isBlank()) existing.setRole(staff.getRole());
        existing.setBranchId(staff.getBranchId() != null && staff.getBranchId() > 0 ? staff.getBranchId() : null);
        if (staff.getAccessLevel() != null && !staff.getAccessLevel().isBlank()) existing.setAccessLevel(staff.getAccessLevel());
        if (staff.getApprovalLevel() != null) existing.setApprovalLevel(staff.getApprovalLevel());
        if (staff.getTargetBookings() != null) existing.setTargetBookings(staff.getTargetBookings());
        if (staff.getResponseSlaHrs() != null) existing.setResponseSlaHrs(staff.getResponseSlaHrs());
        if (staff.getSupervisorId() != null) existing.setSupervisorId(staff.getSupervisorId());

        staffRepository.update(existing);
        activityLogService.log(sessionStaffId, "Updated profile/access for staff ID: " + existing.getStaffId());
        return getStaffById(existing.getStaffId());
    }

    @Transactional
    public void toggleStaffStatus(Integer staffId, Integer sessionStaffId) {
        Staff staff = getStaffById(staffId);
        String newStatus = "ACTIVE".equalsIgnoreCase(staff.getStatus()) ? "DEACTIVATED" : "ACTIVE";
        staffRepository.updateStatus(staffId, newStatus);
        activityLogService.log(sessionStaffId, "Changed status of staff " + staff.getName() + " to " + newStatus);
    }

    @Transactional
    public void deleteStaff(Integer staffId, Integer sessionStaffId) {
        Staff staff = getStaffById(staffId);
        staffRepository.delete(staffId);
        activityLogService.log(sessionStaffId, "Deleted staff account for " + staff.getName());
    }

    @Transactional
    public void resetStaffPassword(Integer staffId, String newPassword, Integer sessionStaffId) {
        Staff staff = getStaffById(staffId);
        if (newPassword == null || newPassword.isBlank()) {
            newPassword = "Password123!";
        }
        staffRepository.updatePassword(staffId, passwordEncoder.encode(newPassword));
        activityLogService.log(sessionStaffId, "Reset password for staff: " + staff.getName());
    }
}
