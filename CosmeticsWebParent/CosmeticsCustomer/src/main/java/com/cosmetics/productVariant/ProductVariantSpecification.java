package com.cosmetics.productVariant;

import com.cosmetics.common.entity.Brand;
import com.cosmetics.common.entity.Brand_;
import com.cosmetics.common.entity.Category;
import com.cosmetics.common.entity.Category_;
import com.cosmetics.common.entity.product.Product;
import com.cosmetics.common.entity.product.ProductVariant;
import com.cosmetics.common.entity.product.ProductVariant_;
import com.cosmetics.common.entity.product.Product_;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class ProductVariantSpecification {
    public static Specification<ProductVariant> isBrandAndCategoryEnabled() {
        return (root, query, cb) -> {
            Join<ProductVariant, Product> productJoin = root.join(ProductVariant_.product);
            Join<Product, Brand> brandJoin = productJoin.join(Product_.brand);
            Join<Product, Category> categoryJoin = productJoin.join(Product_.category);

            return cb.and(
                    cb.isTrue(brandJoin.get(Brand_.enabled)),
                    cb.isTrue(categoryJoin.get(Category_.enabled))
            );
        };
    }

    public static Specification<ProductVariant> hasCategory(Category category) {
        return (root, query, cb) -> {
            if (category == null) return null;

            Join<ProductVariant, Product> productJoin = root.join(ProductVariant_.product);
            Join<Product, Brand> brandJoin = productJoin.join(Product_.brand);
            Join<Product, Category> categoryJoin = productJoin.join(Product_.category);

            Predicate brandEnabled = cb.isTrue(brandJoin.get(Brand_.enabled));

            // Nếu là category con (không có subcategories)
            if (category.getChildren() == null || category.getChildren().isEmpty()) {
                return cb.and(
                        brandEnabled,
                        cb.equal(categoryJoin, category)
                );
            }

            String categoryIdMatch = "-" + category.getId() + "-";
            return cb.and(
                    brandEnabled,
                    cb.like(categoryJoin.get(Category_.allParentIDs), "%" + categoryIdMatch + "%")
            );
        };
    }


    public static Specification<ProductVariant> hasBrand(String brandName) {
        return (root, query, cb) -> {
            if (brandName == null || brandName.trim().isEmpty()) return null;

            Join<ProductVariant, Product> productJoin = root.join(ProductVariant_.product);
            Join<Product, Brand> brandJoin = productJoin.join(Product_.brand);

            return cb.equal(cb.lower(brandJoin.get(Brand_.name)), brandName.toLowerCase());
        };
    }


    public static Specification<ProductVariant> hasBrands(List<String> brandNames) {
        return (root, query, cb) -> {
            if (brandNames == null || brandNames.isEmpty()) return null;

            Join<ProductVariant, Product> productJoin = root.join(ProductVariant_.product);
            Join<Product, Brand> brandJoin = productJoin.join(Product_.brand);

            CriteriaBuilder.In<String> inClause = cb.in(cb.lower(brandJoin.get(Brand_.name)));
            for (String name : brandNames) {
                inClause.value(name.toLowerCase());
            }

            return inClause;
        };
    }


    public static Specification<ProductVariant> hasRating(Integer rating) {
        return (root, query, cb) -> {
            if (rating == null) return null;

            var avg = root.get(ProductVariant_.averageRating);

            if (rating >= 5) {
                return cb.greaterThanOrEqualTo(avg, 5.0f);
            }

            return cb.and(
                    cb.greaterThanOrEqualTo(avg, rating.floatValue()),
                    cb.lessThan(avg, rating.floatValue() + 1.0f)
            );
        };
    }


    // Lọc theo khoảng giá tùy chỉnh
    public static Specification<ProductVariant> hasPriceBetween(Float minPrice, Float maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) {
                return null;
            }

            // Tính giá sau khuyến mãi: price - (price * discountPercent / 100)
            var discountedPrice = cb.diff(
                    root.get(ProductVariant_.price),
                    cb.prod(
                            root.get(ProductVariant_.price),
                            cb.quot(
                                    root.get(ProductVariant_.discountPercent),
                                    100.0
                            )
                    )
            );

            // Lấy giá cuối cùng (finalPrice)
            var finalPrice = cb.<Number>selectCase()
                    .when(cb.gt(root.get(ProductVariant_.discountPercent), 0), discountedPrice)
                    .otherwise(root.get(ProductVariant_.price));

            // Áp dụng điều kiện lọc
            if (minPrice == null) {
                return cb.le(finalPrice.as(Float.class), cb.literal(maxPrice));
            } else if (maxPrice == null) {
                return cb.ge(finalPrice.as(Float.class), cb.literal(minPrice));
            } else {
                return cb.between(finalPrice.as(Float.class), cb.literal(minPrice), cb.literal(maxPrice));
            }
        };
    }

    // Lọc theo khoảng giá định sẵn
    public static Specification<ProductVariant> hasPriceRange(String priceRange) {
        return (root, query, cb) -> {
            if (priceRange == null) {
                return null;
            }

            // Tính giá sau khuyến mãi
            var discountedPrice = cb.diff(
                    root.get(ProductVariant_.price),
                    cb.prod(
                            root.get(ProductVariant_.price),
                            cb.quot(
                                    root.get(ProductVariant_.discountPercent),
                                    100.0
                            )
                    )
            );

            // Lấy giá cuối cùng (finalPrice)
            var finalPrice = cb.<Number>selectCase()
                    .when(cb.gt(root.get(ProductVariant_.discountPercent), 0), discountedPrice)
                    .otherwise(root.get(ProductVariant_.price));

            // Áp dụng điều kiện lọc theo khoảng giá
            switch (priceRange) {
                case "UNDER_50":
                    return cb.lt(finalPrice.as(Float.class), cb.literal(50f));
                case "50_TO_100":
                    return cb.between(finalPrice.as(Float.class), cb.literal(50f), cb.literal(100f));
                case "100_TO_200":
                    return cb.between(finalPrice.as(Float.class), cb.literal(100f), cb.literal(200f));
                case "OVER_200":
                    return cb.gt(finalPrice.as(Float.class), cb.literal(200f));
                default:
                    return null;
            }
        };
    }

    public static Sort getSort(String sortOption) {
        switch (sortOption) {
            case "HIGH_TO_LOW":
                return Sort.by(Sort.Direction.DESC, "finalPrice");
            case "MOST_SOLD":
                return Sort.by("id");
            case "HIGH_RATING":
                return Sort.by(Sort.Direction.DESC, "averageRating");
            case "LOW_TO_HIGH":
            default:
                return Sort.by(Sort.Direction.ASC, "finalPrice");
        }
    }

    public static Specification<ProductVariant> searchProduct(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }

            String searchTerm = "%" + keyword.trim().toLowerCase() + "%";

            return cb.and(
                cb.isTrue(root.get(ProductVariant_.enabled)),
                cb.like(cb.lower(root.get(ProductVariant_.name)), searchTerm)
            );
        };
    }

    public static Specification<ProductVariant> hasBrandsAndCategory(List<String> brandNames, Category category) {
        return (root, query, cb) -> {
            if ((brandNames == null || brandNames.isEmpty()) && category == null) return null;

            Join<ProductVariant, Product> productJoin = root.join(ProductVariant_.product);
            Join<Product, Brand> brandJoin = productJoin.join(Product_.brand);
            Join<Product, Category> categoryJoin = productJoin.join(Product_.category);

            Predicate brandEnabled = cb.isTrue(brandJoin.get(Brand_.enabled));

            Predicate categoryPredicate = null;
            if (category != null) {
                if (category.getChildren() == null || category.getChildren().isEmpty()) {
                    categoryPredicate = cb.equal(categoryJoin, category);
                } else {
                    String categoryIdMatch = "-" + category.getId() + "-";
                    categoryPredicate = cb.like(categoryJoin.get(Category_.allParentIDs), "%" + categoryIdMatch + "%");
                }
            }

            Predicate brandPredicate = null;
            if (brandNames != null && !brandNames.isEmpty()) {
                CriteriaBuilder.In<String> inClause = cb.in(cb.lower(brandJoin.get(Brand_.name)));
                for (String name : brandNames) {
                    inClause.value(name.toLowerCase());
                }
                brandPredicate = inClause;
            }

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(brandEnabled);
            if (categoryPredicate != null) predicates.add(categoryPredicate);
            if (brandPredicate != null) predicates.add(brandPredicate);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }



}
