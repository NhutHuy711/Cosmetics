package com.cosmetics.productVariant;

import com.cosmetics.common.entity.product.ProductVariant;
import com.cosmetics.common.exception.ProductVariantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProductVariantService {
    public static final int SEARCH_RESULTS_PER_PAGE = 10;

    @Autowired
    private ProductVariantRepository repo;

    public List<ProductVariant> listNewProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductVariant> page = repo.findNewVariants(pageable);
        List<ProductVariant> products = page.getContent();
        System.out.println("Number of new products: " + products.size());
        return products;
    }

    public List<ProductVariant> listSpecialOffers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductVariant> page = repo.findSpecialOffers(pageable);
        return page.getContent();
    }

    public List<ProductVariant> listBestSellingProducts(int limit) {
        Sort sort = Sort.by("id").ascending();
        Pageable pageable = PageRequest.of(0, limit, sort);
        Page<ProductVariant> page = repo.findBestSellingVariants(pageable);
        return page.getContent();
    }

    public ProductVariant getProductVariant(Integer id) throws ProductVariantNotFoundException {
        try {
            ProductVariant productVariant = repo.findById(id).get();
            if (!productVariant.isEnabled()
                    || !productVariant.getProduct().getBrand().isEnabled()
                    || !productVariant.getProduct().getCategory().isEnabled()) {
                throw new ProductVariantNotFoundException("Product not found or unavailable.");
            }
            return productVariant;
        } catch (NoSuchElementException ex) {
            throw new ProductVariantNotFoundException("Could not find any product with ID " + id);
        }
    }

    public Page<ProductVariant> search(String keyword, int pageNum) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(pageNum - 1, SEARCH_RESULTS_PER_PAGE);
        Specification<ProductVariant> spec = Specification.where(ProductVariantSpecification.searchProduct(keyword))
                .and(ProductVariantSpecification.isBrandAndCategoryEnabled());
        return repo.findAll(spec, pageable);
    }

    public Page<ProductVariant> listByBrand(Specification<ProductVariant> spec, Pageable pageable, Integer brandId) {
        // Nếu đang sắp xếp theo bán chạy
        if (pageable.getSort().equals(Sort.by("id"))) {
            return repo.findAllOrderByMostSoldByBrand(brandId, pageable);
        }

        // Ngược lại thì lọc bằng Specification
        return repo.findAll(spec, pageable);
    }

    public Page<ProductVariant> listByPage(Specification<ProductVariant> spec, Pageable pageable, Integer categoryId) {
        if (pageable.getSort().equals(Sort.by("id"))) {
            String categoryIDMatch = "-" + String.valueOf(categoryId) + "-";
            return repo.findAllOrderByMostSold(categoryId, categoryIDMatch, pageable);
        }
        return repo.findAll(spec, pageable);
    }

    public ProductVariant getProductVariant(String alias) throws ProductVariantNotFoundException {
        ProductVariant productVariant = repo.findByAlias(alias);
        if (productVariant == null || !productVariant.isEnabled()
                || !productVariant.getProduct().getBrand().isEnabled()
                || !productVariant.getProduct().getCategory().isEnabled()) {
            throw new ProductVariantNotFoundException("Product not found or unavailable.");
        }
        return productVariant;
    }

    public List<ProductVariant> listByProduct(Integer productId) {
        return repo.findByProductId(productId);
    }

    public ProductVariant getProductVariantWithOptions(String alias) throws ProductVariantNotFoundException {
        ProductVariant pv = repo.findByAliasWithOptions(alias);
        if (pv == null) {
            throw new ProductVariantNotFoundException("Product not found");
        }
        return pv;
    }

    public List<ProductVariant> getVariantsWithOptionsByProduct(Integer productId) {
        return repo.findVariantsWithOptionsByProductId(productId);
    }
}
