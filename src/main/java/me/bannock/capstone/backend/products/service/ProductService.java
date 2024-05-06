package me.bannock.capstone.backend.products.service;

import org.springframework.security.access.annotation.Secured;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    /**
     * Gets a product based on its ID
     * @param productId The product's id
     * @return The product, if it was found
     * @throws ProductServiceException If something goes wrong while getting the product's details
     */
    Optional<ProductDTO> getProductDetails(long productId) throws ProductServiceException;

    /**
     * Sets the details for a product
     * @param productDetails The details of the product
     * @throws ProductServiceException If something goes wrong while setting the product's details
     */
    @Secured("PRIV_MODIFY_PRODUCT_DETAILS")
    void setProductDetails(ProductDTO productDetails) throws ProductServiceException;

    /**
     * Registers a new product
     * @param ownerId The product owner's id
     * @return The product's id
     * @throws ProductServiceException If something goes wrong while registering the product
     */
    @Secured("PRIV_REGISTER_PRODUCT")
    long registerProduct(long ownerId) throws ProductServiceException;

    /**
     * Grabs all the products owned by a user
     * @param ownerId The id of the user
     * @return A list of products
     * @throws ProductServiceException If something goes wrong while getting the products
     */
    List<ProductDTO> getUsersProducts(long ownerId) throws ProductServiceException;

    /**
     * @return A list of products that are displayed to the user in different places
     */
    List<ProductDTO> getDisplayProducts();

}
