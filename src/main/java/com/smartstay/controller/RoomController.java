package com.smartstay.controller;

import com.smartstay.config.AuthInterceptor;
import com.smartstay.dto.RoomFilterDto;
import com.smartstay.dto.UserSession;
import com.smartstay.model.Branch;
import com.smartstay.model.Room;
import com.smartstay.service.BranchService;
import com.smartstay.service.RoomService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
public class RoomController {

    private final RoomService roomService;
    private final BranchService branchService;

    public RoomController(RoomService roomService, BranchService branchService) {
        this.roomService = roomService;
        this.branchService = branchService;
    }

    @GetMapping("/rooms")
    public String listRooms(@ModelAttribute("filter") RoomFilterDto filter, Model model) {
        List<Room> rooms = roomService.searchRooms(filter);
        List<Branch> branches = branchService.getAllBranches();

        model.addAttribute("rooms", rooms);
        model.addAttribute("branches", branches);
        model.addAttribute("filter", filter);
        return "rooms/list";
    }

    @GetMapping("/rooms/detail/{id}")
    public String roomDetail(@PathVariable("id") Integer id, Model model) {
        Room room = roomService.getRoomById(id);
        model.addAttribute("room", room);
        return "rooms/detail";
    }

    // Staff Room Management
    @GetMapping("/staff/rooms")
    public String staffRoomList(HttpSession session, Model model) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        List<Room> rooms = roomService.getAllRooms();
        List<Branch> branches = branchService.getAllBranches();

        model.addAttribute("rooms", rooms);
        model.addAttribute("branches", branches);
        return "rooms/manage";
    }

    @PostMapping("/staff/rooms/status")
    public String updateRoomStatus(@RequestParam("roomId") Integer roomId,
                                   @RequestParam("status") String status,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        roomService.updateRoomStatus(roomId, status, currentUser.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Room status updated to " + status);
        return "redirect:/staff/rooms";
    }

    @PostMapping("/staff/rooms/create")
    public String createRoom(@ModelAttribute Room room,
                             @RequestParam(value = "imageUrl", required = false) String imageUrl,
                             @RequestParam(value = "facilityList", required = false) String facilityList,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        List<String> images = (imageUrl != null && !imageUrl.isBlank()) ? List.of(imageUrl) : List.of();
        List<String> facilities = (facilityList != null && !facilityList.isBlank())
                ? Arrays.asList(facilityList.split(","))
                : List.of();

        roomService.createRoom(room, images, facilities, currentUser.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Room created successfully!");
        return "redirect:/staff/rooms";
    }

    @PostMapping("/staff/rooms/add-image")
    public String addImage(@RequestParam("roomId") Integer roomId,
                           @RequestParam("imageUrl") String imageUrl,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        roomService.addRoomImage(roomId, imageUrl, currentUser.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Room photo added!");
        return "redirect:/staff/rooms";
    }

    @PostMapping("/staff/rooms/update")
    public String updateRoom(@ModelAttribute Room room,
                             @RequestParam(value = "facilityList", required = false) String facilityList,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        try {
            // Update facilities if provided
            if (facilityList != null && !facilityList.isBlank()) {
                roomService.getRoomById(room.getRoomId()); // validate exists
                // Clear old and add new
                Arrays.asList(facilityList.split(",")).forEach(f -> {
                    if (f != null && !f.isBlank()) {
                        roomService.addFacility(room.getRoomId(), f.trim(), currentUser.getId());
                    }
                });
            }
            roomService.updateRoom(room, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Room #" + room.getRoomNumber() + " updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/staff/rooms";
    }

    @PostMapping("/staff/rooms/delete/{id}")
    public String deleteRoom(@PathVariable("id") Integer id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        try {
            roomService.deleteRoom(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Room deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete room: it may have active reservations. Error: " + e.getMessage());
        }
        return "redirect:/staff/rooms";
    }

    @PostMapping("/staff/rooms/delete-image/{imageId}")
    public String deleteRoomImage(@PathVariable("imageId") Integer imageId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        roomService.deleteRoomImage(imageId, currentUser.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Room image removed.");
        return "redirect:/staff/rooms";
    }
}
