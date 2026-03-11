package com.cosmetics;

import com.cosmetics.brand.BrandService;
import com.cosmetics.category.CategoryService;
import com.cosmetics.common.entity.Brand;
import com.cosmetics.common.entity.Category;
import com.cosmetics.common.entity.product.ProductVariant;
import com.cosmetics.product.ProductService;
import com.cosmetics.productVariant.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ProductVariantService productVariantService;

    @GetMapping("")
    public String viewHomePage(Model model) {
        List<Category> listCategories = categoryService.listHierarchicalCategories();
        List<Brand> listBrands = brandService.listAll();
        Map<Brand, List<Category>> brandCategoriesMap = new LinkedHashMap<>();

        for (Brand brand : listBrands) {
            List<Category> categories = categoryService.listCategoriesByBrand(brand.getId());
            brandCategoriesMap.put(brand, categories);
        }
        
        // Thêm danh sách sản phẩm cho trang chủ
        List<ProductVariant> listNewProducts = productVariantService.listNewProducts();
        List<ProductVariant> listSpecialOffers = productVariantService.listSpecialOffers();
        List<ProductVariant> listBestSellers = productVariantService.listBestSellingProducts(10); // Lấy top 10 sản phẩm bán chạy

        model.addAttribute("brandCategoriesMap", brandCategoriesMap);
        model.addAttribute("listCategories", listCategories);
        model.addAttribute("listBrands", listBrands);
        model.addAttribute("listNewProducts", listNewProducts);
        model.addAttribute("listSpecialOffers", listSpecialOffers);
        model.addAttribute("listBestSellers", listBestSellers);
        
        return "index";
    }

    @GetMapping("/login")
    public String viewLoginPage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "login";
        }

        return "redirect:/";
    }

}
