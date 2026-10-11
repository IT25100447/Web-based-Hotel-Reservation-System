package com.smartstay.service;

import com.smartstay.model.Promotion;
import com.smartstay.repository.PromotionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final ActivityLogService activityLogService;

    public PromotionService(PromotionRepository promotionRepository, ActivityLogService activityLogService) {
        this.promotionRepository = promotionRepository;
        this.activityLogService = activityLogService;
    }

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    public List<Promotion> getActivePromotions() {
        return promotionRepository.findActivePromotions();
    }

    public Promotion createPromotion(Promotion promotion, Integer staffId) {
        promotionRepository.save(promotion);
        activityLogService.log(staffId, "Created promotional campaign: " + promotion.getName() + " (" + promotion.getDiscountPct() + "%)");
        return promotion;
    }

    public void applyPromotionToRoom(Integer roomId, Integer promoId, Integer staffId) {
        promotionRepository.linkRoomPromotion(roomId, promoId);
        activityLogService.log(staffId, "Assigned promo ID #" + promoId + " to Room ID #" + roomId);
    }

    public void removePromotionFromRoom(Integer roomId, Integer promoId, Integer staffId) {
        promotionRepository.unlinkRoomPromotion(roomId, promoId);
        activityLogService.log(staffId, "Removed promo ID #" + promoId + " from Room ID #" + roomId);
    }

    public void deletePromotion(Integer promoId, Integer staffId) {
        promotionRepository.delete(promoId);
        activityLogService.log(staffId, "Deleted promotion ID #" + promoId);
    }
}
