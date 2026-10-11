package com.smartstay.service;

import com.smartstay.dto.RoomFilterDto;
import com.smartstay.exception.ResourceNotFoundException;
import com.smartstay.model.Room;
import com.smartstay.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final ActivityLogService activityLogService;

    public RoomService(RoomRepository roomRepository, ActivityLogService activityLogService) {
        this.roomRepository = roomRepository;
        this.activityLogService = activityLogService;
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(Integer id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + id));
    }

    public List<Room> searchRooms(RoomFilterDto filter) {
        return roomRepository.searchRooms(filter);
    }

    public boolean isRoomAvailable(Integer roomId, LocalDate checkIn, LocalDate checkOut, Integer excludeResId) {
        return roomRepository.isRoomAvailable(roomId, checkIn, checkOut, excludeResId);
    }

    @Transactional
    public void updateRoomStatus(Integer roomId, String status, Integer staffId) {
        Room room = getRoomById(roomId);
        roomRepository.updateStatus(roomId, status);
        activityLogService.log(staffId, "Updated status of Room #" + room.getRoomNumber() + " to " + status);
    }

    @Transactional
    public Room createRoom(Room room, List<String> imageUrls, List<String> facilities, Integer staffId) {
        roomRepository.save(room);
        if (imageUrls != null) {
            for (String url : imageUrls) {
                if (url != null && !url.isBlank()) {
                    roomRepository.addImage(room.getRoomId(), url.trim());
                }
            }
        }
        if (facilities != null) {
            for (String fac : facilities) {
                if (fac != null && !fac.isBlank()) {
                    roomRepository.addFacility(room.getRoomId(), fac.trim());
                }
            }
        }
        activityLogService.log(staffId, "Created new Room #" + room.getRoomNumber() + " (" + room.getType() + ")");
        return getRoomById(room.getRoomId());
    }

    @Transactional
    public Room updateRoom(Room room, Integer staffId) {
        roomRepository.update(room);
        activityLogService.log(staffId, "Updated Room #" + room.getRoomNumber());
        return getRoomById(room.getRoomId());
    }

    public void addRoomImage(Integer roomId, String imageUrl, Integer staffId) {
        roomRepository.addImage(roomId, imageUrl);
        activityLogService.log(staffId, "Added photo to Room ID: " + roomId);
    }

    public void deleteRoomImage(Integer imageId, Integer staffId) {
        roomRepository.deleteImage(imageId);
        activityLogService.log(staffId, "Removed photo ID: " + imageId);
    }

    public void addFacility(Integer roomId, String facilityName, Integer staffId) {
        roomRepository.addFacility(roomId, facilityName);
        activityLogService.log(staffId, "Added facility '" + facilityName + "' to Room ID: " + roomId);
    }

    @Transactional
    public void deleteRoom(Integer roomId, Integer staffId) {
        Room room = getRoomById(roomId);
        roomRepository.delete(roomId);
        activityLogService.log(staffId, "Deleted Room #" + room.getRoomNumber());
    }
}
