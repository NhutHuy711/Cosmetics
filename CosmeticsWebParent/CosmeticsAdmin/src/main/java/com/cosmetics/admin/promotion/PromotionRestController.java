package com.cosmetics.admin.promotion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/promotions")
public class PromotionRestController {
    @Autowired
    private PromotionRepository promotionRepository;

    @PostMapping("/overlaps")
    public Map<String, Object> checkOverlaps(@RequestBody Map<String, Object> body) {
        // Đọc input từ Map (không tạo DTO)
        Integer excludeId = toInteger(body.get("promotionId")); // null nếu tạo mới
        LocalDateTime startAt = toLocalDateTime(body.get("startAt"));
        LocalDateTime endAt   = toLocalDateTime(body.get("endAt"));
        @SuppressWarnings("unchecked")
        List<Object> rawIds   = (List<Object>) body.get("variantIds");

        List<Integer> variantIds = new ArrayList<>();
        if (rawIds != null) {
            for (Object o : rawIds) {
                Integer vid = toInteger(o);
                if (vid != null) variantIds.add(vid);
            }
        }

        Map<String, Object> res = new LinkedHashMap<>();
        if (startAt == null || endAt == null || variantIds.isEmpty()) {
            res.put("conflicts", Collections.emptyMap());
            res.put("count", 0);
            res.put("message", "Missing startAt/endAt/variantIds");
            return res;
        }

        List<Object[]> found = promotionRepository.findOverlapsRaw(
                variantIds, startAt, endAt, excludeId
        );

        // Group theo variantId
        Map<Integer, List<Map<String, Object>>> byVariant = new LinkedHashMap<>();
        for (Object[] row : found) {
            Integer variantId    = (Integer) row[0];
            Integer promotionId  = (Integer) row[1];
            String  promotionName= (String)  row[2];
            LocalDateTime s      = (LocalDateTime) row[3];
            LocalDateTime e      = (LocalDateTime) row[4];

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("variantId", variantId);
            m.put("promotionId", promotionId);
            m.put("promotionName", promotionName);
            m.put("startAt", s != null ? s.toString() : null);
            m.put("endAt",   e != null ? e.toString() : null);

            byVariant.computeIfAbsent(variantId, k -> new ArrayList<>()).add(m);
        }

        res.put("conflicts", byVariant); // { 12: [ {...}, {...} ], 45: [...] }
        res.put("count", found.size());
        return res;
    }

    private Integer toInteger(Object o) {
        if (o == null) return null;
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Number)  return ((Number) o).intValue();
        try { return Integer.valueOf(String.valueOf(o)); } catch (Exception ignored) { return null; }
    }

    private LocalDateTime toLocalDateTime(Object o) {
        if (o == null) return null;
        try {
            // từ input dạng "yyyy-MM-dd'T'HH:mm"
            return LocalDateTime.parse(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }
}
