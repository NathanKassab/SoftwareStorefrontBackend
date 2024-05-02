package me.bannock.capstone.backend.licensing.service;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;

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
    @Secured("PRIV_CREATE_LICENSE")
    String createLicense(long productId) throws LicenseServiceException;

    /**
     * Binds a license key to a user
     * @param userId The user's id
     * @param license The license, must be unclaimed
     * @throws LicenseServiceException If something goes wrong while activating the license
     */
    @Secured("PRIV_ACTIVATE_LICENSE")
    void activateLicense(long userId, String license) throws LicenseServiceException;

    /**
     * Deactivates a license key
     * @param userId The holder's id
     * @param productId The product's id
     * @throws LicenseServiceException If something goes wrong while deactivating the license
     */
    @Secured("PRIV_DEACTIVATE_LICENSE")
    void deactivateLicense(long userId, long productId) throws LicenseServiceException;

    /**
     * Deletes a license key
     * @param license The license to delete
     * @throws LicenseServiceException If something goes wrong while deleting the license
     */
    @Secured("PRIV_DELETE_LICENSE")
    void deleteLicense(String license) throws LicenseServiceException;

    /**
     * Gets a license object with its code
     * @param license The license code
     * @return The license, if it could be found
     */
    Optional<LicenseDTO> getLicense(String license);

    /**
     * Bans a license key and prevents it from being used
     * @param license The license key
     * @throws LicenseServiceException If something goes wrong while banning the license
     */
    @PreAuthorize("hasAnyAuthority('PRIV_BAN_ANY_LICENSE', 'PRIV_BAN_OWN_PRODUCT_LICENSES')")
    void banLicense(String license) throws LicenseServiceException;

    /**
     * Unbans a license key so that it could be used again
     * @param license The license key
     * @throws LicenseServiceException If something goes wrong while unbanning the license
     */
    @PreAuthorize("hasAnyAuthority('PRIV_UNBAN_ANY_LICENSE', 'PRIV_UNBAN_OWN_PRODUCT_LICENSES')")
    void unbanLicense(String license) throws LicenseServiceException;

    /**
     * Checks if a license key is banned
     * @param license The license key
     * @return Whether the license is banned
     */
    boolean isLicenseBanned(String license);

}
