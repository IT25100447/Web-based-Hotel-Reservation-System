package com.smartstay.service;

import com.smartstay.exception.ResourceNotFoundException;
import com.smartstay.model.Guest;
import com.smartstay.repository.GuestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GuestService {

    private final GuestRepository guestRepository;
    private final ActivityLogService activityLogService;

    public GuestService(GuestRepository guestRepository, ActivityLogService activityLogService) {
        this.guestRepository = guestRepository;
        this.activityLogService = activityLogService;
    }

    public List<Guest> getAllGuests() {
        return guestRepository.findAll();
    }

    public List<Guest> searchGuests(String query) {
        if (query == null || query.isBlank()) {
            return guestRepository.findAll();
        }
        return guestRepository.searchGuests(query);
    }

    public Guest getGuestById(Integer id) {
        return guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with ID: " + id));
    }

    @Transactional
    public void deleteGuest(Integer guestId, Integer staffId) {
        Guest guest = getGuestById(guestId);
        guestRepository.delete(guestId);
        activityLogService.log(staffId, "Deleted guest account for: " + guest.getName() + " (" + guest.getEmail() + ")");
    }
}
