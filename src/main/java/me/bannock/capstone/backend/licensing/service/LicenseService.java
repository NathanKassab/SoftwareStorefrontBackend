package me.bannock.capstone.backend.licensing.service;

import org.springframework.security.access.annotation.Secured;

import java.util.List;
import java.util.Optional;

public interface LicenseService {

    /**
     * Checks if a user owns a product
     * @param userId The user id
     * @param productId The product id
     * @return True if the user has a license for the product
     */
    @Secured("PRIV_USE_OWN_LICENSES")
    boolean ownsProduct(long userId, long productId);

    /**
     * Gets a list containing all the user's licenses
     * @param userId The user id
     * @return A list containing all the user's licenses
     */
    @Secured("PRIV_GET_LICENSES")
    List<String> getUserLicenses(long userId);

    /**
     * Gets a user's license key for a specific product
     * @param userId The user id
     * @param productId The product id
     * @return The license key, if one could be found
     */
    @Secured("PRIV_GET_LICENSES")
    Optional<String> getUsersLicenseForProduct(long userId, long productId);

    /**
     * Creates a new license key for a product
     * @param productId The product id to create the license for
     * @return The newly generated license key
     * @throws LicenseServiceException If something goes wrong while generating the key
     */
    String createLicense(long productId) throws LicenseServiceException;

    /**
     * Binds a license key to a user
     * @param userId The user's id
     * @param license The license, must be unclaimed
     * @throws LicenseServiceException If something goes wrong while activating the license
     */
    @Secured("PRIV_ACTIVATE_LICENSE")
    void activateLicense(long userId, String license) throws LicenseServiceException;

}
