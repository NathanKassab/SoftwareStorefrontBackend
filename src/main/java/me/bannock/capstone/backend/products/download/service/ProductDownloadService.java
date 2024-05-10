package me.bannock.capstone.backend.products.download.service;

import java.util.Optional;

public interface ProductDownloadService {

    /**
     * Gets the download for a product
     * @param productId The product id to get the download for
     * @return The product download data, if it exists
     */
    Optional<ProductDownloadDTO> getProductDownload(long productId);

}
