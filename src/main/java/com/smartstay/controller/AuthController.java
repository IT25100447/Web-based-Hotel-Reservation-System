package com.smartstay.controller;

import com.smartstay.config.AuthInterceptor;
import com.smartstay.dto.LoginRequestDto;
import com.smartstay.dto.RegisterGuestDto;
import com.smartstay.dto.UserSession;
import com.smartstay.exception.UnauthorizedException;
import com.smartstay.exception.ValidationException;
import com.smartstay.model.Guest;
import com.smartstay.model.Staff;
import com.smartstay.repository.GuestRepository;
import com.smartstay.repository.StaffRepository;
import com.smartstay.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthService authService;
    private final GuestRepository guestRepository;
    private final StaffRepository staffRepository;

    public AuthController(AuthService authService, GuestRepository guestRepository, StaffRepository staffRepository) {
        this.authService = authService;
        this.guestRepository = guestRepository;
        this.staffRepository = staffRepository;
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "message", required = false) String message,
                                @RequestParam(value = "error", required = false) String error,
                                Model model) {
        if (!model.containsAttribute("loginDto")) {
            model.addAttribute("loginDto", new LoginRequestDto());
        }
        if (message != null) model.addAttribute("infoMessage", message);
        if (error != null) model.addAttribute("errorMessage", error);
        return "auth/login";
    }

    @PostMapping("/login")
    public String handleLogin(@Valid @ModelAttribute("loginDto") LoginRequestDto loginDto,
                              BindingResult bindingResult,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        try {
            UserSession userSession = authService.login(loginDto);
            session.setAttribute(AuthInterceptor.SESSION_USER_KEY, userSession);
            redirectAttributes.addFlashAttribute("successMessage", "Welcome back, " + userSession.getName() + "!");

            if (userSession.isStaff()) {
                return "redirect:/dashboard";
            } else {
                return "redirect:/";
            }
        } catch (UnauthorizedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to connect to the database. Please ensure the system is configured correctly and try again.");
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        if (!model.containsAttribute("registerDto")) {
            model.addAttribute("registerDto", new RegisterGuestDto());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("registerDto") RegisterGuestDto registerDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.registerGuest(registerDto);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! You can now log in.");
            return "redirect:/login";
        } catch (ValidationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        } catch (Exception e) {
            System.err.println("Register error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed. Please ensure all fields are valid and try again. If the problem persists, please contact support.");
            return "redirect:/register";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm() {
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("email") String email,
                                      @RequestParam(value = "phone", required = false) String phone,
                                      @RequestParam("newPassword") String newPassword,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match.");
            return "redirect:/reset-password";
        }
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters long.");
            return "redirect:/reset-password";
        }

        boolean success = authService.resetPassword(email, phone, newPassword);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Password has been successfully reset. Please log in.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "No account found matching this email and phone verification.");
            return "redirect:/reset-password";
        }
    }

    @GetMapping("/profile")
    public String viewProfile(HttpSession session, Model model) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";

        if (currentUser.isGuest()) {
            guestRepository.findById(currentUser.getId()).ifPresent(g -> model.addAttribute("guest", g));
        } else {
            staffRepository.findById(currentUser.getId()).ifPresent(s -> model.addAttribute("staff", s));
        }

        return "auth/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam("name") String name,
                                @RequestParam(value = "phone", required = false) String phone,
                                @RequestParam(value = "address", required = false) String address,
                                @RequestParam(value = "newPassword", required = false) String newPassword,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";

        if (currentUser.isGuest()) {
            Guest g = guestRepository.findById(currentUser.getId()).orElse(null);
            if (g != null) {
                g.setName(name);
                g.setPhone(phone);
                g.setAddress(address);
                guestRepository.update(g);
                if (newPassword != null && !newPassword.isBlank()) {
                    guestRepository.updatePassword(g.getGuestId(), authService.hashPassword(newPassword));
                }
                currentUser.setName(name);
                session.setAttribute(AuthInterceptor.SESSION_USER_KEY, currentUser);
            }
        } else {
            Staff s = staffRepository.findById(currentUser.getId()).orElse(null);
            if (s != null) {
                s.setName(name);
                s.setPhone(phone);
                staffRepository.update(s);
                if (newPassword != null && !newPassword.isBlank()) {
                    staffRepository.updatePassword(s.getStaffId(), authService.hashPassword(newPassword));
                }
                currentUser.setName(name);
                session.setAttribute(AuthInterceptor.SESSION_USER_KEY, currentUser);
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/profile";
    }

    @GetMapping("/logout")
    public String handleLogout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("infoMessage", "You have been logged out successfully.");
        return "redirect:/login";
    }
}
