package com.cosmetics.admin.promotion;

public class PromotionAlreadyExistsException extends RuntimeException {
    public PromotionAlreadyExistsException(String message) {
        super(message);
    }
}
