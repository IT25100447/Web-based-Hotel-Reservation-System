package com.smartstay.controller;

import com.smartstay.dto.RoomFilterDto;
import com.smartstay.model.Branch;
import com.smartstay.model.Promotion;
import com.smartstay.model.Room;
import com.smartstay.service.BranchService;
import com.smartstay.service.PromotionService;
import com.smartstay.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final RoomService roomService;
    private final BranchService branchService;
    private final PromotionService promotionService;

    public HomeController(RoomService roomService, BranchService branchService, PromotionService promotionService) {
        this.roomService = roomService;
        this.branchService = branchService;
        this.promotionService = promotionService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Branch> branches;
        List<Room> featuredRooms;
        List<Promotion> promotions;

        try {
            branches = branchService.getAllBranches();
        } catch (Exception e) {
            System.err.println("HomeController - branches load error: " + e.getMessage());
            branches = new java.util.ArrayList<>();
        }

        try {
            featuredRooms = roomService.getAllRooms();
            if (featuredRooms.size() > 6) {
                featuredRooms = featuredRooms.subList(0, 6);
            }
        } catch (Exception e) {
            System.err.println("HomeController - rooms load error: " + e.getMessage());
            featuredRooms = new java.util.ArrayList<>();
        }

        try {
            promotions = promotionService.getActivePromotions();
        } catch (Exception e) {
            System.err.println("HomeController - promotions load error: " + e.getMessage());
            promotions = new java.util.ArrayList<>();
        }

        model.addAttribute("branches", branches);
        model.addAttribute("featuredRooms", featuredRooms);
        model.addAttribute("promotions", promotions);
        model.addAttribute("filter", new RoomFilterDto());

        return "index";
    }
}
