package me.bannock.capstone.backend.products.service;

import jakarta.annotation.Nullable;
import me.bannock.capstone.backend.products.service.db.ProductModel;

import java.util.Objects;

public class ProductServiceException extends Exception {

    /**
     * @param message The error message
     * @param productDto An instance of the product, could be null
     * @param productId The product's id
     */
    public ProductServiceException(String message,
                                   @Nullable ProductDTO productDto,
                                   long productId){
        super("%s, productObj=%s, productId=%s".formatted(message, productDto, productId));
        this.errorMessage = message;
    }

    private final String errorMessage;

    /**
     * @param message The error message
     * @param productDto An instance of the product
     */
    public ProductServiceException(String message, ProductDTO productDto){
        super("%s, productDto=%s, productId=%s".formatted(message, productDto, productDto.getId()));
        this.errorMessage = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
