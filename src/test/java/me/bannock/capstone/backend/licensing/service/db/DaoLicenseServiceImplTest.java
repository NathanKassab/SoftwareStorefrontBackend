package me.bannock.capstone.backend.licensing.service.db;

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
    @WithMockUser(authorities = {"PRIV_ACTIVATE_LICENSE"})
    void generateLicenseAndActivate(){
        assertDoesNotThrow(() -> {
            String license = licenseService.createLicense(TEST_PRODUCT_ID);
            assertNotNull(license);
            assertFalse(license.isBlank());
            licenseService.activateLicense(TEST_OWNER_ID, license);
        });
    }

}