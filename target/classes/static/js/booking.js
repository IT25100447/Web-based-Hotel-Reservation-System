// SmartStay Hotel Reservation System - Dynamic Booking & Pricing Process Validation

document.addEventListener('DOMContentLoaded', () => {
    const checkInInput = document.getElementById('checkInDate');
    const checkOutInput = document.getElementById('checkOutDate');
    const nightsDisplay = document.getElementById('displayNights');
    const subtotalDisplay = document.getElementById('displaySubtotal');
    const discountDisplay = document.getElementById('displayDiscount');
    const grandTotalDisplay = document.getElementById('displayGrandTotal');
    const depositInput = document.getElementById('depositAmount');

    // Base price and seasonal discount embedded via dataset
    const bookingSummary = document.getElementById('bookingPricingSummary');
    if (!bookingSummary || !checkInInput || !checkOutInput) return;

    const pricePerNight = parseFloat(bookingSummary.dataset.price || '0');
    const discountPct = parseFloat(bookingSummary.dataset.discount || '0');

    // Process validation: Restrict check-in date so users cannot book dates in the past
    const todayStr = new Date().toISOString().split('T')[0];
    if (checkInInput.getAttribute('min') === null) {
        checkInInput.setAttribute('min', todayStr);
    }

    function updatePricing() {
        const inDateVal = checkInInput.value;
        const outDateVal = checkOutInput.value;

        // Process validation: Skip calculation if either date is empty
        if (!inDateVal || !outDateVal) return;

        const checkIn = new Date(inDateVal);
        const checkOut = new Date(outDateVal);

        // Process validation: Enforce that check-out date is strictly after check-in date
        // Automatically advance checkout date to the next day if guest selects invalid range
        if (checkOut <= checkIn) {
            const nextDay = new Date(checkIn);
            nextDay.setDate(nextDay.getDate() + 1);
            checkOutInput.value = nextDay.toISOString().split('T')[0];
            return updatePricing();
        }

        // Process validation: Calculate positive integer stay duration in nights
        const diffTime = Math.abs(checkOut - checkIn);
        const nights = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) || 1;

        // Process validation: Calculate financial totals (subtotal, discount, grand total)
        const subtotal = pricePerNight * nights;
        const discountAmount = subtotal * (discountPct / 100);
        const grandTotal = subtotal - discountAmount;

        if (nightsDisplay) nightsDisplay.textContent = nights + ' Night' + (nights > 1 ? 's' : '');
        if (subtotalDisplay) subtotalDisplay.textContent = '$' + subtotal.toFixed(2);
        if (discountDisplay) discountDisplay.textContent = '-$' + discountAmount.toFixed(2);
        if (grandTotalDisplay) grandTotalDisplay.textContent = '$' + grandTotal.toFixed(2);

        // Process validation: Ensure optional deposit amount cannot exceed total booking price
        if (depositInput) {
            depositInput.setAttribute('max', grandTotal.toFixed(2));
            if (depositInput.value && parseFloat(depositInput.value) > grandTotal) {
                depositInput.value = grandTotal.toFixed(2);
            }
        }
    }

    // Process validation: Adjust minimum check-out date dynamically when check-in date changes
    checkInInput.addEventListener('change', () => {
        const checkIn = new Date(checkInInput.value);
        if (!isNaN(checkIn.getTime())) {
            const minOut = new Date(checkIn);
            minOut.setDate(minOut.getDate() + 1);
            checkOutInput.setAttribute('min', minOut.toISOString().split('T')[0]);
            // If current checkout is on or before new check-in, advance it automatically
            if (new Date(checkOutInput.value) <= checkIn) {
                checkOutInput.value = minOut.toISOString().split('T')[0];
            }
        }
        updatePricing();
    });

    // Process validation: Recalculate invoice when check-out date changes
    checkOutInput.addEventListener('change', updatePricing);

    // Initial calculation on page load
    updatePricing();

    // ========================================================
    // PAYMENT METHOD CONDITIONAL FIELD VALIDATION
    // ========================================================
    const paymentMethodSelect = document.getElementById('paymentMethod');
    const bankDetailsSection = document.getElementById('bankTransferDetails');
    const cardDetailsSection = document.getElementById('cardDetailsSection');

    if (paymentMethodSelect) {
        paymentMethodSelect.addEventListener('change', (e) => {
            const val = e.target.value;
            // Process validation: Show bank account information only for direct transfer method
            if (bankDetailsSection) {
                bankDetailsSection.style.display = (val === 'BANK_TRANSFER') ? 'block' : 'none';
            }
            // Process validation: Show card details inputs only when card or online payment is selected
            if (cardDetailsSection) {
                cardDetailsSection.style.display = (val === 'CARD' || val === 'ONLINE') ? 'block' : 'none';
            }
        });
    }
});
