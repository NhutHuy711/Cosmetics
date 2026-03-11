package com.cosmetics.common.exception;

public class ProductOutOfStockException extends Exception {
    public ProductOutOfStockException(String message) {
        super(message);
    }
}