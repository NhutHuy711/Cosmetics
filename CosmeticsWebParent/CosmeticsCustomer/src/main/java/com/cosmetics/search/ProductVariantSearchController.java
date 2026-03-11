package com.cosmetics.search;

import com.cosmetics.common.entity.product.ProductVariantDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search/variant")
public class ProductVariantSearchController {

    @Autowired
    private ProductVariantSearchService searchService;

    @GetMapping
    public List<ProductVariantDocument> search(@RequestParam String keyword) throws Exception {
        return searchService.search(keyword);
    }
}

