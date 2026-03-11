package com.cosmetics.admin.product;

import com.cosmetics.common.entity.product.Product;
import com.cosmetics.common.exception.ProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ProductRestController {

    @Autowired
    private ProductService service;

    @PostMapping("/products/check_unique")
    public String checkUnique(Integer id, String name) {
        return service.checkUnique(id, name);
    }

//    @GetMapping("/products/get/{id}")
//    public ProductDTO getProductInfo(@PathVariable("id") Integer id)
//            throws ProductNotFoundException {
//        Product product = service.get(id);
//        return new ProductDTO(product.getName(), product.getMainImagePath(),
//                product.getDiscountPrice(), product.getCost(), product.getInStock());
//    }

    @GetMapping("/products/api/{id}")
    public Map<String, String> getProductInfo(@PathVariable Integer id) throws ProductNotFoundException {
        Product p = service.get(id); // đảm bảo ProductService có method get(id)
        return Map.of(
                "brand", p.getBrand() != null ? p.getBrand().getName() : "",
                "category", p.getCategory() != null ? p.getCategory().getName() : ""
        );
    }

}
