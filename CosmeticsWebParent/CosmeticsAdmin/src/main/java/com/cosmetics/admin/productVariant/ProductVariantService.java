package com.cosmetics.admin.productVariant;

import com.cosmetics.admin.option.OptionValueRepository;
import com.cosmetics.admin.paging.PagingAndSortingHelper;
import com.cosmetics.admin.search.ProductVariantSearchService;
import com.cosmetics.common.entity.product.OptionValue;
import com.cosmetics.common.entity.product.Product;
import com.cosmetics.common.entity.product.ProductVariant;
import com.cosmetics.common.entity.product.ProductVariantDocument;
import com.cosmetics.common.exception.ProductVariantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductVariantService {
    public static final int PRODUCTS_PER_PAGE = 5;

    @Autowired
    private ProductVariantRepository repo;

    @Autowired
    private OptionValueRepository optionValueRepository;

    @Autowired
    private ProductVariantSearchService esSearchService;


    public void saveProductPrice(ProductVariant productVariantInForm) {
        ProductVariant productVariantInDB = repo.findById(productVariantInForm.getId()).get();
        productVariantInDB.setCost(productVariantInForm.getCost());
        productVariantInDB.setPrice(productVariantInForm.getPrice());
        //productVariantInDB.setDiscountPercent(productVariantInForm.getDiscountPercent());

        repo.save(productVariantInDB);
    }

//    public ProductVariant save(ProductVariant productVariant, Integer[] optionValueIds) {
//        if (productVariant.getId() == null) {
//            productVariant.setCreatedAt(LocalDateTime.now());
//        }
//
//        productVariant.setUpdatedAt(LocalDateTime.now());
//
//        if (optionValueIds != null && optionValueIds.length > 0) {
//            Set<OptionValue> selectedValues = new HashSet<>();
//            for (Integer id : optionValueIds) {
//                if (id != null) {
//                    optionValueRepository.findById(id).ifPresent(selectedValues::add);
//                }
//            }
//            productVariant.setOptionValues(selectedValues);
//        }
//
//        return repo.save(productVariant);
//    }
    public ProductVariant save(ProductVariant productVariant, Integer[] optionValueIds) {

        if (productVariant.getId() == null) {
            productVariant.setCreatedAt(LocalDateTime.now());
        }

        productVariant.setUpdatedAt(LocalDateTime.now());

        if (optionValueIds != null && optionValueIds.length > 0) {
            Set<OptionValue> selectedValues = new HashSet<>();
            for (Integer id : optionValueIds) {
                if (id != null) {
                    optionValueRepository.findById(id).ifPresent(selectedValues::add);
                }
            }
            productVariant.setOptionValues(selectedValues);
        }

        ProductVariant saved = repo.save(productVariant);

        // QUAN TRỌNG: reload để @Formula được populate từ SELECT
        ProductVariant reloaded = repo.findById(saved.getId()).orElseThrow();
        //  TỰ ĐỘNG INDEX LÊN ELASTICSEARCH
        indexToElastic(reloaded);

        return saved;
    }

    private void indexToElastic(ProductVariant variant) {
        try {
            Product product = variant.getProduct();
            if (product == null) {
                System.err.println("⚠ Bỏ qua variant " + variant.getId() + " vì thiếu liên kết Product");
                return;
            }

            ProductVariantDocument doc = new ProductVariantDocument();

            doc.setId(String.valueOf(variant.getId()));
            doc.setProductId(String.valueOf(product.getId()));
            doc.setName(variant.getName());
            doc.setAlias(variant.getAlias());
            doc.setProductName(product.getName());
            doc.setBrand(product.getBrand() != null ? product.getBrand().getName() : null);
            doc.setCategory(product.getCategory() != null ? product.getCategory().getName() : null);
            doc.setDescription(product.getShortDescription());

            doc.setPrice(variant.getPrice());
            doc.setFinalPrice(variant.getFinalPrice());
            doc.setDiscountPercent(variant.getDiscountPercent());

            doc.setAverageRating(variant.getAverageRating());
            doc.setReviewCount(variant.getReviewCount());
            doc.setMainImage(variant.getMainImage());
            doc.setStock(variant.getStock());

            // NEW: index options (để filter/search theo option types/values)
            if (variant.getOptionValues() == null || variant.getOptionValues().isEmpty()) {
                doc.setOptions(Collections.emptyList());
            } else {
                doc.setOptions(
                        variant.getOptionValues().stream()
                                .filter(ov -> ov != null && ov.getOptionType() != null)
                                .map(ov -> {
                                    ProductVariantDocument.OptionKV kv = new ProductVariantDocument.OptionKV();
                                    kv.setType(ov.getOptionType().getName());
                                    kv.setValue(ov.getValue());
                                    kv.setTypeId(ov.getOptionType().getId());
                                    kv.setValueId(ov.getId());
                                    return kv;
                                })
                                .collect(Collectors.toList())
                );
            }

            esSearchService.indexVariant(doc);

        } catch (Exception e) {
            System.err.println("⚠ Không thể index Elasticsearch cho variant " + variant.getId());
            e.printStackTrace();
        }
    }





    public void listByPage(int pageNum, PagingAndSortingHelper helper, Integer categoryId) {
        Pageable pageable = helper.createPageableForVariant(PRODUCTS_PER_PAGE, pageNum);
        String keyword = helper.getKeyword();
        Page<ProductVariant> page = null;

        if (keyword != null && !keyword.isEmpty()) {
            if (categoryId != null && categoryId > 0) {
                String categoryIdMatch = "-" + String.valueOf(categoryId) + "-";
                page = repo.searchInCategory(categoryId, categoryIdMatch, keyword, pageable);
            } else {
                page = repo.findAll(keyword, pageable);
            }
        } else {
            if (categoryId != null && categoryId > 0) {
                String categoryIdMatch = "-" + String.valueOf(categoryId) + "-";
                page = repo.findAllInCategory(categoryId, categoryIdMatch, pageable);
            } else {
                page = repo.findAll(pageable);
            }
        }

        helper.updateModelAttributes(pageNum, page);
    }

    public void updateProductEnabledStatus(Integer id, boolean enabled) {
        repo.updateEnabledStatus(id, enabled);
    }

    public ProductVariant get(Integer id) throws ProductVariantNotFoundException {
        try {
            return repo.findById(id).get();
        } catch (NoSuchElementException ex) {
            throw new ProductVariantNotFoundException("Could not find any product with ID " + id);
        }
    }

    public List<ProductVariant> listAll() {
        return (List<ProductVariant>) repo.findAll();
    }

    public List<ProductVariant> findAll() {
        List<ProductVariant> list = new ArrayList<>();
        repo.findAll().forEach(list::add); // convert Iterable -> List
        return list;
    }

    public List<ProductVariant> findByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return java.util.Collections.emptyList();
        return repo.findByIdIn(ids);
    }

    public void reindexAllVariantsToElastic() {
        List<ProductVariant> variants = repo.findAllWithProductBrandCategory();  // <-- QUAN TRỌNG

        variants.forEach(variant -> {
            indexToElastic(variant);
        });
    }




}
