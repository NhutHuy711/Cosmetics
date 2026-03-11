package com.cosmetics.search;

import com.cosmetics.brand.BrandService;
import com.cosmetics.category.CategoryService;
import com.cosmetics.common.entity.Brand;
import com.cosmetics.common.entity.Category;
import com.cosmetics.common.entity.product.ProductVariantDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController {
    @Autowired
    private ProductVariantSearchService searchService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    private static final int ITEMS_PER_PAGE = 12;

    @GetMapping("/search")
    public String searchFirstPage(@RequestParam(required = false) String keyword, Model model) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "redirect:/";
        }
        return searchByPage(keyword, 1, model);
    }

    @GetMapping("/search/page/{pageNum}")
    public String searchByPage(
            @RequestParam(required = false) String keyword,
            @PathVariable("pageNum") int pageNum,
            Model model) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return "redirect:/";
        }

        keyword = keyword.trim();

        try {
            // --- Call Elasticsearch ---
            List<ProductVariantDocument> fullResults = searchService.search(keyword);

            int totalItems = fullResults.size();
            int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);

            int start = (pageNum - 1) * ITEMS_PER_PAGE;
            int end = Math.min(start + ITEMS_PER_PAGE, totalItems);

            List<ProductVariantDocument> pageItems =
                    fullResults.subList(start, end);

            // --- Get categories & brand-category map ---
            List<Category> listCategories = categoryService.listHierarchicalCategories();
            List<Brand> listBrands = brandService.listAll();
            Map<Brand, List<Category>> brandCategoriesMap = new LinkedHashMap<>();

            for (Brand b : listBrands) {
                brandCategoriesMap.put(b, categoryService.listCategoriesByBrand(b.getId()));
            }

            // --- Render ---
            model.addAttribute("brandCategoriesMap", brandCategoriesMap);
            model.addAttribute("currentPage", pageNum);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalItems", totalItems);
            model.addAttribute("keyword", keyword);
            model.addAttribute("searchKeyword", keyword);
            model.addAttribute("listProducts", pageItems);  // LISTES VARIANT
            model.addAttribute("listCategories", listCategories);

            return "product/search_result";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "Error while searching");
            return "product/search_result";
        }
    }
}
