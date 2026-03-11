package com.cosmetics.productVariant;

import com.cosmetics.ControllerHelper;
import com.cosmetics.brand.BrandService;
import com.cosmetics.category.CategoryService;
import com.cosmetics.common.entity.Brand;
import com.cosmetics.common.entity.Category;
import com.cosmetics.common.entity.Customer;
import com.cosmetics.common.entity.Review;
import com.cosmetics.common.entity.product.OptionValue;
import com.cosmetics.common.entity.product.ProductVariant;
import com.cosmetics.common.exception.BrandNotFoundException;
import com.cosmetics.common.exception.CategoryNotFoundException;
import com.cosmetics.common.exception.ProductVariantNotFoundException;
import com.cosmetics.product.ProductService;
import com.cosmetics.review.ReviewService;
import com.cosmetics.review.vote.ReviewVoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class    ProductVariantController {
    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewVoteService voteService;

    @Autowired
    private ControllerHelper controllerHelper;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ProductVariantService productVariantService;

    private static final int PRODUCTS_PER_PAGE = 18; // Cố định số sản phẩm mỗi trang

    @GetMapping("/category/{category_alias}")
    public String viewCategory(@PathVariable("category_alias") String alias,
                               @RequestParam(defaultValue = "1") int pageNum,
                               @RequestParam(required = false) String brands,
                               @RequestParam(required = false) Integer rating,
                               @RequestParam(required = false) Float minPrice,
                               @RequestParam(required = false) Float maxPrice,
                               @RequestParam(required = false) String priceRange,
                               @RequestParam(defaultValue = "LOW_TO_HIGH") String sort,
                               Model model,
                               HttpServletRequest request) {
        try {
            alias = normalizeUrl(alias);
            Category category = categoryService.getCategory(alias);
            List<Category> listParents = categoryService.getCategoryParents(category);
            List<Brand> listBrands = brandService.listByCategory(category);
            List<Category> listCategories = categoryService.listHierarchicalCategories();

            // Xử lý category hierarchy
            if (listParents.size() > 0) {
                model.addAttribute("mainCategory", listParents.get(0));
                if (listParents.size() > 1) {
                    model.addAttribute("subCategory", listParents.get(1));
                }
            }

            // Xử lý filters
            List<String> brandNames = brands != null ?
                    Arrays.asList(brands.split(",")) : new ArrayList<>();

            Specification<ProductVariant> spec = ProductVariantSpecification.hasCategory(category);

            if (!brandNames.isEmpty()) {
                spec = spec.and(ProductVariantSpecification.hasBrands(brandNames));
            }

            if (rating != null) {
                spec = spec.and(ProductVariantSpecification.hasRating(rating));
            }

            if (priceRange != null) {
                spec = spec.and(ProductVariantSpecification.hasPriceRange(priceRange));
            } else if (minPrice != null || maxPrice != null) {
                spec = spec.and(ProductVariantSpecification.hasPriceBetween(minPrice, maxPrice));
            }

            // Xử lý phân trang và sắp xếp
            Sort sortOption = ProductVariantSpecification.getSort(sort);
            Pageable pageable = PageRequest.of(pageNum - 1, PRODUCTS_PER_PAGE, sortOption);
            Page<ProductVariant> pageProducts = productVariantService.listByPage(spec, pageable, category.getId());

            List<Brand> listB = brandService.listAll();
            Map<Brand, List<Category>> brandCategoriesMap = new LinkedHashMap<>();

            for (Brand b : listB) {
                List<Category> categories = categoryService.listCategoriesByBrand(b.getId());
                brandCategoriesMap.put(b, categories);
            }

            // Add attributes to model
            model.addAttribute("brandCategoriesMap", brandCategoriesMap);
            model.addAttribute("totalPages", pageProducts.getTotalPages());
            model.addAttribute("totalItems", pageProducts.getTotalElements());
            model.addAttribute("currentPage", pageNum);
            model.addAttribute("listProducts", pageProducts.getContent());
            model.addAttribute("listBrands", listBrands);
            model.addAttribute("category", category);
            model.addAttribute("listCategoryParents", listParents);
            model.addAttribute("selectedBrands", brandNames);
            model.addAttribute("pageTitle", category.getName());
            model.addAttribute("currentSort", sort);
            model.addAttribute("listCategories", listCategories);

            if (request.getHeader("X-Requested-With") != null) {
                return "product/product_fragment :: productList";
            }

            return "product/products_by_category";
        } catch (CategoryNotFoundException e) {
            return "error/404";
        }
    }

    @GetMapping("/p/{product_alias}")
    public String viewProductDetail(@PathVariable("product_alias") String alias, Model model,
                                    HttpServletRequest request) {
        try {
            ProductVariant productVariant = productVariantService.getProductVariantWithOptions(alias);
            List<ProductVariant> allVariants = productVariantService.getVariantsWithOptionsByProduct(productVariant.getProduct().getId());
            List<Category> listCategoryParents = categoryService.getCategoryParents(productVariant.getProduct().getCategory());
            Page<Review> listReviews = reviewService.list3MostVotedReviewsByProduct(productVariant);
            List<Category> listCategories = categoryService.listHierarchicalCategories();

            Customer customer = controllerHelper.getAuthenticatedCustomer(request);
            if (customer != null) {
                boolean customerReviewed = reviewService.didCustomerReviewProduct(customer, productVariant.getId());
                voteService.markReviewsVotedForProductByCustomer(listReviews.getContent(), productVariant.getId(), customer.getId());

                if (customerReviewed) {
                    model.addAttribute("customerReviewed", customerReviewed);
                } else {
                    boolean customerCanReview = reviewService.canCustomerReviewProduct(customer, productVariant.getId());
                    model.addAttribute("customerCanReview", customerCanReview);
                }
            }

            // === NHÓM VARIANTS THEO OPTION TYPE ===
            Map<String, Set<OptionValue>> optionTypeMap = new LinkedHashMap<>();

            System.out.println("DEBUG variants count = " + allVariants.size());
            for (ProductVariant v : allVariants) {
                System.out.println("  Variant: " + v.getName() + " - OptionValues: " + v.getOptionValues().size());
                for (OptionValue ov : v.getOptionValues()) {
                    System.out.println("     -> " + ov.getOptionType().getName() + " = " + ov.getValue());
                    optionTypeMap
                            .computeIfAbsent(ov.getOptionType().getName(), k -> new LinkedHashSet<>())
                            .add(ov);
                }
            }
            System.out.println("OptionTypeMap size: " + optionTypeMap.size());
            optionTypeMap.forEach((k,v) -> System.out.println("   " + k + ": " + v.size() + " values"));


            // === BRAND - CATEGORY MAP ===
            List<Brand> listBrands = brandService.listAll();
            Map<Brand, List<Category>> brandCategoriesMap = new LinkedHashMap<>();
            for (Brand b : listBrands) {
                List<Category> categories = categoryService.listCategoriesByBrand(b.getId());
                brandCategoriesMap.put(b, categories);
            }

            Map<Integer, ProductVariant> optionValueVariantMap = new HashMap<>();
            for (ProductVariant v : allVariants) {
                for (OptionValue ov : v.getOptionValues()) {
                    if (ov != null && ov.getId() != null) {
                        optionValueVariantMap.putIfAbsent(ov.getId(), v); // khóa là ID
                    }
                }
            }
            model.addAttribute("optionValueVariantMap", optionValueVariantMap);

            Set<Integer> selectedOptionValueIds =
                    productVariant.getOptionValues().stream()
                            .filter(Objects::nonNull)
                            .map(OptionValue::getId)
                            .collect(Collectors.toSet());
            model.addAttribute("selectedOptionValueIds", selectedOptionValueIds);


            model.addAttribute("brandCategoriesMap", brandCategoriesMap);
            model.addAttribute("listCategoryParents", listCategoryParents);
            model.addAttribute("productVariant", productVariant);
            model.addAttribute("productVariants", productVariant.getProduct().getVariants());
            model.addAttribute("optionTypeMap", optionTypeMap);
            model.addAttribute("listReviews", listReviews);
            model.addAttribute("pageTitle", productVariant.getName());
            model.addAttribute("listCategories", listCategories);

            return "product/product_detail";
        } catch (ProductVariantNotFoundException e) {
            return "error/404";
        }
    }



    @GetMapping("/brand/{name}")
    public String viewBrand(@PathVariable("name") String encodedName,
                            @RequestParam(defaultValue = "1") int pageNum,
                            @RequestParam(required = false) String brands,
                            @RequestParam(required = false) Integer rating,
                            @RequestParam(required = false) Float minPrice,
                            @RequestParam(required = false) Float maxPrice,
                            @RequestParam(required = false) String priceRange,
                            @RequestParam(defaultValue = "LOW_TO_HIGH") String sort,
                            Model model,
                            HttpServletRequest request) {
        try {
            // Lấy brand theo name
            String name = URLDecoder.decode(encodedName, StandardCharsets.UTF_8);
            Brand brand = brandService.getBrand(name);
            List<Category> listCategories = categoryService.listHierarchicalCategories();

            // Tách tên brand filter (nếu có)
            List<String> brandNames = brands != null ?
                    Arrays.asList(brands.split(",")) : new ArrayList<>();

            // Tạo spec tìm theo brand
            Specification<ProductVariant> spec = ProductVariantSpecification.hasBrand(brand.getName());

            // Bổ sung filter rating
            if (rating != null) {
                spec = spec.and(ProductVariantSpecification.hasRating(rating));
            }

            // Bổ sung filter giá
            if (priceRange != null) {
                spec = spec.and(ProductVariantSpecification.hasPriceRange(priceRange));
            } else if (minPrice != null || maxPrice != null) {
                spec = spec.and(ProductVariantSpecification.hasPriceBetween(minPrice, maxPrice));
            }

            // Xử lý phân trang
            Sort sortOption = ProductVariantSpecification.getSort(sort);
            Pageable pageable = PageRequest.of(pageNum - 1, PRODUCTS_PER_PAGE, sortOption);
            Page<ProductVariant> pageProducts = productVariantService.listByBrand(spec, pageable, brand.getId());

            // Truy xuất các thương hiệu cùng phân khúc
            List<Brand> listBrands = brandService.listAll();

            Map<Brand, List<Category>> brandCategoriesMap = new LinkedHashMap<>();

            for (Brand b : listBrands) {
                List<Category> categories = categoryService.listCategoriesByBrand(b.getId());
                brandCategoriesMap.put(b, categories);
            }

            // Gửi dữ liệu về view
            model.addAttribute("brandCategoriesMap", brandCategoriesMap);
            model.addAttribute("totalPages", pageProducts.getTotalPages());
            model.addAttribute("totalItems", pageProducts.getTotalElements());
            model.addAttribute("currentPage", pageNum);
            model.addAttribute("listProducts", pageProducts.getContent());
            model.addAttribute("listBrands", listBrands);
            model.addAttribute("brand", brand);
            model.addAttribute("selectedBrands", brand.getName());
            model.addAttribute("pageTitle", brand.getName());
            model.addAttribute("currentSort", sort);
            model.addAttribute("listCategories", listCategories);

            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                return "product/product_fragment :: productList";
            }

            return "product/products_by_brand";

        } catch (BrandNotFoundException e) {
            return "error/404";
        }
    }


    @GetMapping("/new-products")
    public String viewNewProducts(Model model) {
        List<ProductVariant> listProductVariants = productVariantService.listNewProducts();
        List<Category> listCategories = categoryService.listHierarchicalCategories();
        List<Brand> listB = brandService.listAll();
        Map<Brand, List<Category>> brandCategoriesMap = new LinkedHashMap<>();

        for (Brand b : listB) {
            List<Category> categories = categoryService.listCategoriesByBrand(b.getId());
            brandCategoriesMap.put(b, categories);
        }

        // Add attributes to model
        model.addAttribute("brandCategoriesMap", brandCategoriesMap);
        model.addAttribute("listProducts", listProductVariants);
        model.addAttribute("listCategories", listCategories);
        return "new_products";
    }

    @GetMapping("/promotions")
    public String viewPromotions(Model model) {
        List<ProductVariant> listProductVariants = productVariantService.listSpecialOffers();
        List<Category> listCategories = categoryService.listHierarchicalCategories();
        List<Brand> listB = brandService.listAll();
        Map<Brand, List<Category>> brandCategoriesMap = new LinkedHashMap<>();

        for (Brand b : listB) {
            List<Category> categories = categoryService.listCategoriesByBrand(b.getId());
            brandCategoriesMap.put(b, categories);
        }

        // Add attributes to model
        model.addAttribute("brandCategoriesMap", brandCategoriesMap);
        model.addAttribute("listProducts", listProductVariants);
        model.addAttribute("listCategories", listCategories);
        return "promotions";
    }

    @GetMapping("/best-sellers")
    public String viewBestSellers(Model model) {
        List<ProductVariant> listProducts = productVariantService.listBestSellingProducts(34);
        List<Category> listCategories = categoryService.listHierarchicalCategories();
        List<Brand> listB = brandService.listAll();
        Map<Brand, List<Category>> brandCategoriesMap = new LinkedHashMap<>();

        for (Brand b : listB) {
            List<Category> categories = categoryService.listCategoriesByBrand(b.getId());
            brandCategoriesMap.put(b, categories);
        }

        // Add attributes to model
        model.addAttribute("brandCategoriesMap", brandCategoriesMap);
        model.addAttribute("listProducts", listProducts);
        model.addAttribute("listCategories", listCategories);
        return "best_sellers";
    }


    private String normalizeUrl(String url) {
        if (url == null) return null;
        return url.replaceAll("//+", "/")
                .replaceAll("^/+|/+$", "")
                .replaceAll("[^a-zA-Z0-9-_/]", "");
    }
}
