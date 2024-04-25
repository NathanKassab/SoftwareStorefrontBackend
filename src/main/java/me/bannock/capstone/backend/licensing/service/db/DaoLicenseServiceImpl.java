package me.bannock.capstone.backend.licensing.service.db;

import me.bannock.capstone.backend.licensing.service.LicenseService;
import me.bannock.capstone.backend.licensing.service.LicenseServiceException;
import me.bannock.capstone.backend.licensing.service.keygen.KeyGenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DaoLicenseServiceImpl implements LicenseService {

    @Autowired
    public DaoLicenseServiceImpl(LicenseRepo licenseRepo, KeyGenService keyGenService){
        this.licenseRepo = licenseRepo;
        this.keyGenService = keyGenService;
    }

    private final LicenseRepo licenseRepo;
    private final KeyGenService keyGenService;

    @Value("${backend.license.maxKeyGenerationAttempts}")
    private int maxKeyGenAttempts;

    @Override
    public boolean ownsProduct(long userId, long productId) {
        return getUsersLicenseForProduct(userId, productId).isPresent();
    }

    @Override
    public List<String> getUserLicenses(long userId) {
        return null;
    }

    @Override
    public Optional<String> getUsersLicenseForProduct(long userId, long productId) {

        return Optional.empty();
    }

    @Override
    public String createLicense(long productId) throws LicenseServiceException {

        // We need to generate a unique key before saving it
        int genAttempts = 0;
        String license;
        do{
            license = this.keyGenService.generateNewKey();
            if (genAttempts++ >= maxKeyGenAttempts)
                throw new RuntimeException("Couldn't generate unique license key");
        }while(this.licenseRepo.findLicenseModelByLicense(license).isPresent());

        LicenseModel newLicense = new LicenseModel(productId, license);
        licenseRepo.saveAndFlush(newLicense);

        return license;
    }

    @Override
    public void activateLicense(long userId, String license) throws LicenseServiceException {
        Optional<LicenseModel> licenseModel = licenseRepo.findLicenseModelByLicense(license);
        if (licenseModel.isEmpty())
            throw new LicenseServiceException("License could not be found", userId);

        licenseModel.get().setHolder(userId);
        licenseRepo.saveAndFlush(licenseModel.get());
    }

}
