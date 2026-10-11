package com.smartstay.service;

import com.smartstay.model.ActivityLog;
import com.smartstay.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public void log(Integer staffId, String action) {
        if (staffId != null) {
            try {
                activityLogRepository.save(new ActivityLog(staffId, action));
            } catch (Exception e) {
                // Log and don't fail main business flow
                System.err.println("Failed to write activity log: " + e.getMessage());
            }
        }
    }

    public List<ActivityLog> getRecentLogs(int limit) {
        return activityLogRepository.findRecent(limit);
    }

    public List<ActivityLog> getLogsByStaff(Integer staffId) {
        return activityLogRepository.findByStaffId(staffId);
    }
}
