// SmartStay Hotel Reservation System - Client-Side Process Validation (Vanilla JS)

document.addEventListener('DOMContentLoaded', () => {

    // ========================================================
    // 1. REGISTRATION FORM PROCESS VALIDATION
    // ========================================================
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', (e) => {
            const pwdInput = document.getElementById('password');
            const confirmPwdInput = document.getElementById('confirmPassword');
            const emailInput = document.querySelector('#registerForm input[type="email"]');
            const errorDiv = document.getElementById('clientValidationErrors');

            // Process validation: Ensure password matches password confirmation
            if (pwdInput && confirmPwdInput && pwdInput.value !== confirmPwdInput.value) {
                e.preventDefault();
                showClientError(errorDiv, 'Passwords do not match. Please verify both password fields.');
                return false;
            }

            // Process validation: Ensure password meets minimum length requirement (at least 6 characters)
            if (pwdInput && pwdInput.value.length < 6) {
                e.preventDefault();
                showClientError(errorDiv, 'Password must be at least 6 characters in length.');
                return false;
            }

            // Process validation: Verify valid email format pattern
            if (emailInput && !isValidEmail(emailInput.value)) {
                e.preventDefault();
                showClientError(errorDiv, 'Please provide a valid email address.');
                return false;
            }
        });
    }

    // ========================================================
    // 2. RESERVATION BOOKING FORM PROCESS VALIDATION
    // ========================================================
    const bookingForm = document.getElementById('reservationForm');
    if (bookingForm) {
        bookingForm.addEventListener('submit', (e) => {
            const checkInVal = document.getElementById('checkInDate')?.value;
            const checkOutVal = document.getElementById('checkOutDate')?.value;
            const errorDiv = document.getElementById('clientValidationErrors');

            // Process validation: Check that both check-in and check-out dates are entered
            if (!checkInVal || !checkOutVal) {
                e.preventDefault();
                showClientError(errorDiv, 'Please select both check-in and check-out dates.');
                return false;
            }

            const checkIn = new Date(checkInVal);
            const checkOut = new Date(checkOutVal);

            // Process validation: Ensure check-in and check-out dates are valid calendar dates
            if (isNaN(checkIn.getTime()) || isNaN(checkOut.getTime())) {
                e.preventDefault();
                showClientError(errorDiv, 'Please select valid dates from the calendar.');
                return false;
            }

            // Process validation: Verify that check-out date is strictly after check-in date
            if (checkOut <= checkIn) {
                e.preventDefault();
                showClientError(errorDiv, 'Check-out date must be strictly after the check-in date.');
                return false;
            }

            // Process validation: Ensure guest count is a positive number between 1 and 10
            const guestCountInput = bookingForm.querySelector('input[name="numGuests"]');
            if (guestCountInput) {
                const count = parseInt(guestCountInput.value, 10);
                if (isNaN(count) || count < 1 || count > 10) {
                    e.preventDefault();
                    showClientError(errorDiv, 'Number of guests must be between 1 and 10.');
                    return false;
                }
            }
        });
    }

    // ========================================================
    // 3. PASSWORD RESET FORM PROCESS VALIDATION
    // ========================================================
    const resetForm = document.querySelector('form[action*="reset-password"]');
    if (resetForm) {
        resetForm.addEventListener('submit', (e) => {
            const newPwd = resetForm.querySelector('input[name="newPassword"]')?.value;
            const confirmPwd = resetForm.querySelector('input[name="confirmPassword"]')?.value;

            // Process validation: Ensure new password confirmation matches
            if (newPwd && confirmPwd && newPwd !== confirmPwd) {
                e.preventDefault();
                alert('New passwords do not match. Please re-enter.');
                return false;
            }

            // Process validation: Enforce minimum password length of 6 characters
            if (newPwd && newPwd.length < 6) {
                e.preventDefault();
                alert('New password must be at least 6 characters in length.');
                return false;
            }
        });
    }

    // ========================================================
    // 4. FEEDBACK FORM PROCESS VALIDATION
    // ========================================================
    const feedbackForm = document.querySelector('form[action*="feedback/create"]');
    if (feedbackForm) {
        feedbackForm.addEventListener('submit', (e) => {
            const ratingSelect = feedbackForm.querySelector('select[name="rating"]');
            const commentsArea = feedbackForm.querySelector('textarea[name="comments"]');

            // Process validation: Confirm star rating is selected between 1 and 5
            if (ratingSelect) {
                const rating = parseInt(ratingSelect.value, 10);
                if (isNaN(rating) || rating < 1 || rating > 5) {
                    e.preventDefault();
                    alert('Please select a star rating from 1 to 5.');
                    return false;
                }
            }

            // Process validation: Ensure feedback comments are not blank or only whitespace
            if (commentsArea && commentsArea.value.trim().length === 0) {
                e.preventDefault();
                alert('Please provide feedback comments.');
                return false;
            }
        });
    }

    // ========================================================
    // HELPER FUNCTIONS FOR VALIDATION
    // ========================================================

    // Process validation helper: Check standard email pattern
    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    // Process validation helper: Display inline validation error container with auto-scroll
    function showClientError(container, msg) {
        if (!container) {
            alert(msg);
            return;
        }
        container.textContent = msg;
        container.style.display = 'block';
        container.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
});
