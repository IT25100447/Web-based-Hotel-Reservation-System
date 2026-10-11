package com.smartstay.config;

import com.smartstay.dto.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_USER_KEY = "userSession";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // Publicly accessible paths
        if (path.startsWith("/css") || path.startsWith("/js") || path.startsWith("/images")
                || path.equals("/") || path.startsWith("/rooms")
                || path.equals("/login") || path.equals("/register") || path.equals("/reset-password")
                || path.equals("/logout") || path.equals("/error") || path.startsWith("/api/public") || path.equals("/favicon.ico")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute(SESSION_USER_KEY) : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?message=Please log in to continue");
            return false;
        }

        // Role-based restrictions
        if (path.startsWith("/staff") || path.startsWith("/manager") || path.startsWith("/reports")) {
            if (!user.isStaff()) {
                response.sendRedirect(request.getContextPath() + "/dashboard?error=Access Denied: Staff role required");
                return false;
            }

            // Staff administration & system audit section (IT Coordinator & Manager only)
            if ((path.startsWith("/staff/it") || path.startsWith("/staff/logs")) && !user.isIt() && !user.isManager()) {
                response.sendRedirect(request.getContextPath() + "/dashboard?error=Access Denied: IT Coordinator role required");
                return false;
            }

            // Reservations & Check-in
            if (path.startsWith("/staff/reservations") && !user.isReservations() && !user.isFrontOffice()) {
                response.sendRedirect(request.getContextPath() + "/dashboard?error=Access Denied: Reservations or Front Office role required");
                return false;
            }

            // Room status & Readiness
            if (path.startsWith("/staff/rooms") && !user.isFrontOffice() && !user.isReservations()) {
                response.sendRedirect(request.getContextPath() + "/dashboard?error=Access Denied: Front Office role required");
                return false;
            }

            // Billing & Payments
            if (path.startsWith("/staff/payments") && !user.isFinance() && !user.isReservations()) {
                response.sendRedirect(request.getContextPath() + "/dashboard?error=Access Denied: Finance role required");
                return false;
            }

            // Complaints & Feedback
            if (path.startsWith("/staff/feedback") && !user.isGuestRelations()) {
                response.sendRedirect(request.getContextPath() + "/dashboard?error=Access Denied: Guest Relations role required");
                return false;
            }

            // Refunds
            if (path.startsWith("/staff/refunds") && !user.isFinance() && !user.isGuestRelations()) {
                response.sendRedirect(request.getContextPath() + "/dashboard?error=Access Denied: Finance or Guest Relations role required");
                return false;
            }

            // Manager only reports
            if (path.startsWith("/manager/reports") && !user.isManager()) {
                response.sendRedirect(request.getContextPath() + "/dashboard?error=Access Denied: General Manager role required");
                return false;
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        if (modelAndView != null) {
            String uri = request.getRequestURI();
            boolean isStaffPortal = uri.startsWith("/staff") || uri.startsWith("/manager") || uri.equals("/dashboard");
            modelAndView.addObject("isStaffPortal", isStaffPortal);
            modelAndView.addObject("currentUri", uri);

            HttpSession session = request.getSession(false);
            if (session != null) {
                UserSession user = (UserSession) session.getAttribute(SESSION_USER_KEY);
                if (user != null) {
                    modelAndView.addObject("currentUser", user);
                }
            }
        }
    }
}
