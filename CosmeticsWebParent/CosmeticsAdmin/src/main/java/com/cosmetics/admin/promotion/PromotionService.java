package com.cosmetics.admin.promotion;

import java.math.BigDecimal;
import java.util.*;

import javax.transaction.Transactional;

import com.cosmetics.admin.productVariant.ProductVariantRepository;
import com.cosmetics.common.entity.promotion.Promotion;
import com.cosmetics.common.entity.product.ProductVariant;
import com.cosmetics.common.entity.promotion.PromotionProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cosmetics.admin.paging.PagingAndSortingHelper;
import com.cosmetics.common.exception.PromotionNotFoundException;

@Service
@Transactional
public class PromotionService {

    public static final int PROMOTIONS_PER_PAGE = 5;

    @Autowired
    private PromotionRepository promotionRepo;

    @Autowired
    private ProductVariantRepository productVariantRepo;

    public void listByPage(int pageNum, PagingAndSortingHelper helper) {
        helper.listEntities(pageNum, PROMOTIONS_PER_PAGE, promotionRepo);
    }

    public Promotion get(Integer id) throws PromotionNotFoundException {
        return promotionRepo.findByIdWithVariants(id)
                .orElseThrow(() -> new PromotionNotFoundException("Could not find any promotions with ID " + id));
    }

    public void save(Promotion form) {
        Promotion p = (form.getId() != null)
                ? promotionRepo.findByIdWithVariants(form.getId())
                .orElseThrow(() -> new IllegalArgumentException("Promotion not found: " + form.getId()))
                : new Promotion();

        p.setName(form.getName());
        p.setStartAt(form.getStartAt());
        p.setEndAt(form.getEndAt());
        p.setEnabled(form.isEnabled());

        List<PromotionProduct> incoming = form.getPromotionProducts();
        if (incoming == null || incoming.isEmpty())
            throw new IllegalArgumentException("Please select at least 1 product variant.");

        // Map existing by variantId
        Map<Integer, PromotionProduct> existingByVid = new HashMap<>();
        for (PromotionProduct old : p.getPromotionProducts()) {
            existingByVid.put(old.getVariant().getId(), old);
        }

        // Keep track incoming ids (và tự khử trùng nếu UI gửi duplicate)
        Map<Integer, BigDecimal> incomingMap = new LinkedHashMap<>();
        for (PromotionProduct row : incoming) {
            Integer vid = row.getVariant().getId();
            BigDecimal off = row.getPercentOff();
            incomingMap.put(vid, off); // duplicate variant -> lấy lần cuối
        }

        // Update or add
        for (var e : incomingMap.entrySet()) {
            Integer vid = e.getKey();
            BigDecimal off = e.getValue();

            PromotionProduct target = existingByVid.get(vid);
            if (target != null) {
                target.setPercentOff(off); // UPDATE
            } else {
                ProductVariant variant = productVariantRepo.findById(vid)
                        .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + vid));

                PromotionProduct np = new PromotionProduct();
                np.setPromotion(p);
                np.setVariant(variant);
                np.setPercentOff(off);
                p.getPromotionProducts().add(np); // INSERT mới
            }
        }

        // Remove rows not in incoming (orphanRemoval=true sẽ DELETE)
        p.getPromotionProducts().removeIf(pp -> !incomingMap.containsKey(pp.getVariant().getId()));

        promotionRepo.save(p);
    }



    public void delete(Integer id) throws PromotionNotFoundException {
        if (!promotionRepo.existsById(id)) {
            throw new PromotionNotFoundException("Could not find any promotions with ID " + id);
        }
        promotionRepo.deleteById(id);
    }

    public void updatePromotionEnabledStatus(Integer id, boolean enabled) {
        promotionRepo.updateEnabledStatus(id, enabled);
    }
}
