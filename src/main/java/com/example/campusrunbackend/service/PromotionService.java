package com.example.campusrunbackend.service;

import com.example.campusrunbackend.model.*;
import com.example.campusrunbackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProviderPromotionRepository providerPromotionRepository;
    private final PromoCodeRepository promoCodeRepository;

    @Autowired
    public PromotionService(PromotionRepository promotionRepository,
                            ProviderPromotionRepository providerPromotionRepository,
                            PromoCodeRepository promoCodeRepository) {
        this.promotionRepository = promotionRepository;
        this.providerPromotionRepository = providerPromotionRepository;
        this.promoCodeRepository = promoCodeRepository;
    }

    public List<Promotion> getAvailablePromotions() {
        return promotionRepository.findAll();
    }

    public List<ProviderPromotion> getActivePromotions(User provider) {
        return providerPromotionRepository.findAllByProviderAndActive(provider, true);
    }

    @Transactional
    public void activatePromoCode(User provider, String code) {
        PromoCode promoCode = promoCodeRepository.findByCodeAndActive(code, true)
                .orElseThrow(() -> new RuntimeException("Invalid or inactive promo code"));

        Promotion promotion = promoCode.getPromotion();

        List<ProviderPromotion> existing = providerPromotionRepository.findAllByProviderAndActive(provider, true);
        boolean alreadyHas = existing.stream().anyMatch(pp -> pp.getPromotion().getId().equals(promotion.getId()));

        if (alreadyHas) {
            throw new RuntimeException("Promotion already active");
        }

        ProviderPromotion pp = new ProviderPromotion();
        pp.setProvider(provider);
        pp.setPromotion(promotion);
        pp.setActivatedAt(LocalDateTime.now());
        pp.setExpiryDate(LocalDateTime.now().plusDays(7)); // Default duration
        pp.setActive(true);

        providerPromotionRepository.save(pp);
    }
}
