package com.smartstay.controller;

import com.smartstay.config.AuthInterceptor;
import com.smartstay.dto.UserSession;
import com.smartstay.model.ActivityLog;
import com.smartstay.model.Branch;
import com.smartstay.model.Guest;
import com.smartstay.model.Staff;
import com.smartstay.repository.GuestRepository;
import com.smartstay.service.ActivityLogService;
import com.smartstay.service.BranchService;
import com.smartstay.service.GuestService;
import com.smartstay.service.StaffService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class StaffController {

    private final StaffService staffService;
    private final BranchService branchService;
    private final ActivityLogService activityLogService;
    private final GuestRepository guestRepository;
    private final GuestService guestService;

    public StaffController(StaffService staffService, BranchService branchService, ActivityLogService activityLogService, GuestRepository guestRepository, GuestService guestService) {
        this.staffService = staffService;
        this.branchService = branchService;
        this.activityLogService = activityLogService;
        this.guestRepository = guestRepository;
        this.guestService = guestService;
    }

    @GetMapping("/staff/it/users")
    public String staffList(HttpSession session, Model model) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isIt() && !currentUser.isManager()) {
            return "redirect:/dashboard?error=Access Denied: IT Coordinator role required";
        }

        List<Staff> staffList = staffService.getAllStaff();
        List<Branch> branches = branchService.getAllBranches();

        long activeStaffCount = staffList != null ? staffList.stream().filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus())).count() : 0;
        long deactivatedStaffCount = staffList != null ? staffList.stream().filter(s -> "DEACTIVATED".equalsIgnoreCase(s.getStatus())).count() : 0;

        model.addAttribute("staffList", staffList);
        model.addAttribute("branches", branches);
        model.addAttribute("activeStaffCount", activeStaffCount);
        model.addAttribute("deactivatedStaffCount", deactivatedStaffCount);
        model.addAttribute("newStaff", new Staff());
        return "staff/manage";
    }

    @PostMapping("/staff/it/users/create")
    public String createStaff(@ModelAttribute("newStaff") Staff staff,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isIt() && !currentUser.isManager()) {
            return "redirect:/dashboard?error=Access Denied: IT Coordinator role required";
        }

        try {
            staffService.createStaff(staff, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Staff account for " + staff.getName() + " created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/staff/it/users";
    }

    @PostMapping("/staff/it/users/toggle/{id}")
    public String toggleStatus(@PathVariable("id") Integer id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isIt() && !currentUser.isManager()) {
            return "redirect:/dashboard?error=Access Denied: IT Coordinator role required";
        }

        staffService.toggleStaffStatus(id, currentUser.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Staff account status updated.");
        return "redirect:/staff/it/users";
    }

    @PostMapping("/staff/it/users/update")
    public String updateStaff(@ModelAttribute Staff staff,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isIt() && !currentUser.isManager()) {
            return "redirect:/dashboard?error=Access Denied: IT Coordinator role required";
        }

        try {
            staffService.updateStaff(staff, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Staff details updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/staff/it/users";
    }

    @GetMapping("/staff/logs")
    public String viewLogs(HttpSession session, Model model) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isIt() && !currentUser.isManager()) return "redirect:/dashboard?error=Access Denied: IT Coordinator role required";

        List<ActivityLog> logs = activityLogService.getRecentLogs(100);
        model.addAttribute("logs", logs);
        return "staff/logs";
    }

    @GetMapping("/staff/it/guests")
    public String guestList(@RequestParam(value = "search", required = false) String search,
                            HttpSession session, Model model) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null || (!currentUser.isIt() && !currentUser.isManager())) {
            return "redirect:/dashboard";
        }
        List<Guest> guests = guestService.searchGuests(search);
        model.addAttribute("guests", guests);
        model.addAttribute("search", search);
        return "staff/guests";
    }

    @PostMapping("/staff/it/users/delete/{id}")
    public String deleteStaff(@PathVariable("id") Integer id,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isIt() && !currentUser.isManager()) {
            return "redirect:/dashboard?error=Access Denied: IT Coordinator role required";
        }
        if (currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot delete your own account.");
            return "redirect:/staff/it/users";
        }

        try {
            staffService.deleteStaff(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Staff account deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete staff: " + e.getMessage());
        }
        return "redirect:/staff/it/users";
    }

    @PostMapping("/staff/it/users/reset-password/{id}")
    public String resetStaffPassword(@PathVariable("id") Integer id,
                                     @RequestParam(value = "newPassword", required = false) String newPassword,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isIt() && !currentUser.isManager()) {
            return "redirect:/dashboard?error=Access Denied: IT Coordinator role required";
        }

        try {
            staffService.resetStaffPassword(id, newPassword, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Password reset successfully. Default: Password123! (if blank)");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/staff/it/users";
    }

    @PostMapping("/staff/it/guests/delete/{guestId}")
    public String deleteGuest(@PathVariable("guestId") Integer guestId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isIt() && !currentUser.isManager()) {
            return "redirect:/dashboard?error=Access Denied: IT Coordinator role required";
        }

        try {
            guestService.deleteGuest(guestId, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Guest account deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete guest: " + e.getMessage());
        }
        return "redirect:/staff/it/guests";
    }
}
