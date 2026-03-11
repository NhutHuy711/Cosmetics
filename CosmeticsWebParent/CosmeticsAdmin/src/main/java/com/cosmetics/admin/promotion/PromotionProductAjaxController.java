package com.cosmetics.admin.promotion;

import com.cosmetics.admin.productVariant.ProductVariantRepository;
import com.cosmetics.common.entity.product.ProductVariant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/promotions/variants")
public class PromotionProductAjaxController {

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @GetMapping("/search")
    public List<Select2Option> search(@RequestParam String term,
                                      @RequestParam(defaultValue = "1") int page) {

        String kw = term == null ? "" : term.trim();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), 20, Sort.by("name").ascending());

        Page<ProductVariant> variants;
        if (kw.isEmpty()) {
            variants = productVariantRepository.findAll(pageable);
        } else {
            variants = productVariantRepository.searchForPromotion(kw.toLowerCase(), pageable);
        }

        List<ProductVariant> results = new ArrayList<>(variants.getContent());

        if (kw.matches("\\d+")) {
            productVariantRepository.findById(Integer.parseInt(kw)).ifPresent(v -> {
                boolean exists = results.stream().anyMatch(x -> x.getId().equals(v.getId()));
                if (!exists) {
                    results.add(0, v);
                }
            });
        }

        return results.stream()
                .map(v -> new Select2Option(v.getId(), buildVariantLabel(v)))
                .collect(Collectors.toList());
    }

    private String buildVariantLabel(ProductVariant variant) {
        StringBuilder label = new StringBuilder();
        label.append(variant.getId());
        label.append(" - ");
        if (variant.getProduct() != null) {
            label.append(variant.getProduct().getName());
            label.append(" | ");
        }
        label.append(variant.getName());
        return label.toString();
    }

    // POJO thay cho record (Java 8/11 OK)
    public static class Select2Option {
        private Integer id;
        private String text;

        public Select2Option() {}
        public Select2Option(Integer id, String text) { this.id = id; this.text = text; }

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}
