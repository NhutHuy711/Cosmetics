package com.cosmetics.shoppingcart;

import java.util.List;

import javax.transaction.Transactional;

import com.cosmetics.common.entity.product.ProductVariant;
import com.cosmetics.productVariant.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cosmetics.common.entity.CartItem;
import com.cosmetics.common.entity.Customer;
import com.cosmetics.product.ProductRepository;

@Service
@Transactional
public class ShoppingCartService {

    @Autowired
    private CartItemRepository cartRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private ProductVariantRepository productVariantRepo;

    public Integer addProduct(Integer productVariantId, Integer quantity, Customer customer)
            throws ShoppingCartException {
        Integer updatedQuantity = quantity;
        ProductVariant productVariant = new ProductVariant(productVariantId);

        CartItem cartItem = cartRepo.findByCustomerAndVariant(customer, productVariant);

        if (cartItem != null) {
            updatedQuantity = cartItem.getQuantity() + quantity;
        } else {
            cartItem = new CartItem();
            cartItem.setCustomer(customer);
            cartItem.setVariant(productVariant);
        }

        cartItem.setQuantity(updatedQuantity);

        cartRepo.save(cartItem);

        return updatedQuantity;
    }

    public List<CartItem> listCartItems(Customer customer) {
        return cartRepo.findByCustomer(customer);
    }

    public float updateQuantity(Integer productVariantId, Integer quantity, Customer customer) {
        cartRepo.updateQuantity(quantity, customer.getId(), productVariantId);
        ProductVariant productVariant = productVariantRepo.findById(productVariantId).get();
        float subtotal = productVariant.getDiscountPrice() * quantity;
        return subtotal;
    }

    public void removeProduct(Integer productId, Customer customer) {
        cartRepo.deleteByCustomerAndProductVariant(customer.getId(), productId);
    }

    public void deleteByCustomer(Customer customer) {
        cartRepo.deleteByCustomer(customer.getId());
    }

    public Integer getNumberOfProductVariants(Customer customer) {
        List<CartItem> cartItems = cartRepo.findByCustomer(customer);
        return cartItems.size();
    }

    public CartItem findCartItem(Integer productId, Customer customer) {
        return cartRepo.findByCustomerAndVariant(customer.getId(), productId);
    }
}
