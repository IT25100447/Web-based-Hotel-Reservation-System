package com.smartstay.controller;

import com.smartstay.exception.BookingConflictException;
import com.smartstay.exception.ResourceNotFoundException;
import com.smartstay.exception.UnauthorizedException;
import com.smartstay.exception.ValidationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorCode", "404 - Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNoHandlerFound(NoHandlerFoundException ex, Model model) {
        model.addAttribute("errorCode", "404 - Page Not Found");
        model.addAttribute("errorMessage", "The page or resource you requested could not be found.");
        return "error/404";
    }

    @ExceptionHandler(BookingConflictException.class)
    public String handleBookingConflict(BookingConflictException ex, Model model) {
        model.addAttribute("errorCode", "Booking Conflict");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/500";
    }

    @ExceptionHandler(ValidationException.class)
    public String handleValidation(ValidationException ex, Model model) {
        model.addAttribute("errorCode", "Validation Error");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/500";
    }

    @ExceptionHandler(UnauthorizedException.class)
    public String handleUnauthorized(UnauthorizedException ex, Model model) {
        model.addAttribute("errorCode", "Access Denied");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        model.addAttribute("errorCode", "System Error");
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again or contact system support.");
        System.err.println("Unexpected error: " + ex.getMessage());
        ex.printStackTrace();
        return "error/500";
    }
}
