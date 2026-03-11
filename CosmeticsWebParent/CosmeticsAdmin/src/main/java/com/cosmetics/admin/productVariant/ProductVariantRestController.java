package com.cosmetics.admin.productVariant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ProductVariantRestController {

    @Autowired
    private ProductVariantRepository variantRepo;

    @GetMapping("/productVariants/api/check-duplicate")
    public Map<String, Object> checkDuplicate(
            @RequestParam Integer productId,
            @RequestParam List<Integer> optionValueIds,
            @RequestParam(required = false) Integer variantId // khi edit thì loại trừ chính nó
    ) {
        // basic validate
        if (productId == null || optionValueIds == null || optionValueIds.isEmpty()) {
            return Map.of("duplicate", false);
        }

        boolean exists = variantRepo.existsDuplicateExactOptions(
                productId, optionValueIds, optionValueIds.size()
        );

        return Map.of("duplicate", exists);
    }
}

