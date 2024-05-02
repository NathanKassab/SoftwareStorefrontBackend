package me.bannock.capstone.backend.licensing.service.db;

import me.bannock.capstone.backend.licensing.service.LicenseServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DaoLicenseServiceImplTest {

    @Autowired
    public DaoLicenseServiceImpl licenseService;

    private static final long TEST_OWNER_ID = 0;
    private static final long TEST_PRODUCT_ID = 0;

    @Test
    @WithMockUser(authorities = {"PRIV_BAN_ANY_LICENSE", "PRIV_BAN_OWN_PRODUCT_LICENSES", "PRIV_UNBAN_ANY_LICENSE",
            "PRIV_UNBAN_OWN_PRODUCT_LICENSES", "PRIV_DEACTIVATE_LICENSE", "PRIV_ACTIVATE_LICENSE",
            "PRIV_CREATE_LICENSE", "PRIV_USE_OWN_LICENSES"})
    void testBanAndUnbanLicense() throws LicenseServiceException {
        String license = licenseService.createLicense(TEST_PRODUCT_ID);
        assertNotNull(license);
        assertFalse(license.isBlank());
        deactivateLicense();
        licenseService.activateLicense(TEST_OWNER_ID, license);
        assertTrue(licenseService.ownsProduct(TEST_OWNER_ID, TEST_PRODUCT_ID));
        licenseService.banLicense(license);
        assertFalse(licenseService.ownsProduct(TEST_OWNER_ID, TEST_PRODUCT_ID));
        assertTrue(licenseService.isLicenseBanned(license));
        assertFalse(licenseService.isLicenseBanned(""));
        licenseService.unbanLicense(license);
        assertTrue(licenseService.ownsProduct(TEST_OWNER_ID, TEST_PRODUCT_ID));
    }

    @Test
    @WithMockUser(authorities = {"PRIV_DELETE_LICENSE", "PRIV_GET_LICENSES", "PRIV_USE_OWN_LICENSES",
            "PRIV_DEACTIVATE_LICENSE", "PRIV_ACTIVATE_LICENSE", "PRIV_CREATE_LICENSE"})
    void deactivateAndDeleteLicense() throws LicenseServiceException {
        String license = licenseService.createLicense(TEST_PRODUCT_ID);
        assertNotNull(license);
        assertFalse(license.isBlank());
        deactivateLicense();
        licenseService.activateLicense(TEST_OWNER_ID, license);
        assertTrue(licenseService.ownsProduct(TEST_OWNER_ID, TEST_PRODUCT_ID));
        licenseService.deactivateLicense(TEST_OWNER_ID, TEST_PRODUCT_ID);
        assertFalse(licenseService.ownsProduct(TEST_OWNER_ID, TEST_PRODUCT_ID));
        licenseService.activateLicense(TEST_OWNER_ID, license);
        assertTrue(licenseService.ownsProduct(TEST_OWNER_ID, TEST_PRODUCT_ID));
        licenseService.deleteLicense(license);
        assertFalse(licenseService.ownsProduct(TEST_OWNER_ID, TEST_PRODUCT_ID));
        assertThrows(LicenseServiceException.class, () -> licenseService.deleteLicense(""));
    }

    @Test
    @WithMockUser(authorities = {"PRIV_ACTIVATE_LICENSE", "PRIV_USE_OWN_LICENSES",
            "PRIV_DEACTIVATE_LICENSE", "PRIV_GET_LICENSES", "PRIV_CREATE_LICENSE"})
    void checkOwnsProduct() throws LicenseServiceException {
        deactivateLicense();
        String license = licenseService.createLicense(TEST_PRODUCT_ID);
        assertNotNull(license);
        assertFalse(license.isBlank());
        licenseService.activateLicense(TEST_OWNER_ID, license);
        assertTrue(licenseService.ownsProduct(TEST_OWNER_ID, TEST_PRODUCT_ID));
        assertTrue(licenseService.getUserLicenses(TEST_OWNER_ID).contains(license));
        assertEquals(licenseService.getUsersLicenseForProduct(TEST_OWNER_ID, TEST_PRODUCT_ID).get(),
                license);
    }

    @Test
    @WithMockUser(authorities = {"PRIV_ACTIVATE_LICENSE", "PRIV_DEACTIVATE_LICENSE"})
    void activateFakeLicense(){
        deactivateLicense();
        assertThrows(LicenseServiceException.class,
                () -> licenseService.activateLicense(TEST_OWNER_ID, ""));
    }

    @Test
    @WithMockUser(authorities = {"PRIV_ACTIVATE_LICENSE", "PRIV_DEACTIVATE_LICENSE", "PRIV_CREATE_LICENSE"})
    void generateLicenseAndActivate(){
        deactivateLicense();
        assertDoesNotThrow(() -> {
            String license = licenseService.createLicense(TEST_PRODUCT_ID);
            assertNotNull(license);
            assertFalse(license.isBlank());
            licenseService.activateLicense(TEST_OWNER_ID, license);
            assertThrows(LicenseServiceException.class,
                    () -> licenseService.activateLicense(TEST_OWNER_ID, license));
            assertTrue(licenseService.getLicense(license).isPresent());
        });
    }

    private void deactivateLicense(){
        try{
            licenseService.deactivateLicense(TEST_OWNER_ID, TEST_PRODUCT_ID);
        }catch (LicenseServiceException ignored){}
    }

}