package me.bannock.capstone.backend.licensing.service.db;

import me.bannock.capstone.backend.keygen.KeyGenService;
import me.bannock.capstone.backend.licensing.service.LicenseDTO;
import me.bannock.capstone.backend.licensing.service.LicenseService;
import me.bannock.capstone.backend.licensing.service.LicenseServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
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
        // The below method already checks if the license is banned already. If the license is banned, we will get an
        // empty optional
        return getUsersLicenseForProduct(userId, productId).isPresent();
    }

    @Override
    public List<String> getUserLicenses(long userId) {
        return licenseRepo.findLicenseModelsByHolder(userId)
                .stream()
                .filter(l -> !l.isBanned()) // We filter out any banned licenses as they are no longer usable
                .map(LicenseModel::getLicense) // Now that we have all the user's active licenses, we grab all the license keys from them to return
                .toList();
    }

    @Override
    public Optional<String> getUsersLicenseForProduct(long userId, long productId) {
        Optional<LicenseModel> license = licenseRepo.findLicenseModelByHolderAndProductId(userId, productId);

        if (license.isPresent() && license.get().isBanned())
            return Optional.empty(); // License is banned so user is not allowed to use it

        return license.map(LicenseModel::getLicense);
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
        Objects.requireNonNull(license);

        Optional<LicenseModel> licenseModel = licenseRepo.findLicenseModelByLicense(license);
        if (licenseModel.isEmpty())
            throw new LicenseServiceException("License could not be found", -1);
        else if (licenseModel.get().getHolder() != null)
            throw new LicenseServiceException("License is already activated", licenseModel.get().getId());

        Optional<LicenseModel> checkForDupes = licenseRepo.findLicenseModelByHolderAndProductId(
                userId, licenseModel.get().getProductId());
        if (checkForDupes.isPresent())
            throw new LicenseServiceException("You already own this product", licenseModel.get().getId());

        licenseModel.get().setHolder(userId);
        licenseRepo.saveAndFlush(licenseModel.get());
    }

    @Override
    public void deactivateLicense(long userId, long productId) throws LicenseServiceException {
        Optional<LicenseModel> license = licenseRepo.findLicenseModelByHolderAndProductId(userId, productId);
        if (license.isEmpty())
            throw new LicenseServiceException("You do not own this product", productId);

        license.get().setHolder(null);
        licenseRepo.saveAndFlush(license.get());
    }

    @Override
    public void deleteLicense(String license) throws LicenseServiceException {
        Objects.requireNonNull(license);

        Optional<LicenseModel> licenseModel = licenseRepo.findLicenseModelByLicense(license);
        if (licenseModel.isEmpty())
            throw new LicenseServiceException("License does not exist", -1);

        licenseRepo.delete(licenseModel.get());
    }

    @Override
    public Optional<LicenseDTO> getLicense(String license) {
        Objects.requireNonNull(license);

        return licenseRepo.findLicenseModelByLicense(license).map(this::mapToDto);
    }

    @Override
    public void banLicense(String license) throws LicenseServiceException {
        Objects.requireNonNull(license);

        setLicenseBanned(license, true);
    }

    @Override
    public void unbanLicense(String license) throws LicenseServiceException {
        Objects.requireNonNull(license);

        setLicenseBanned(license, false);
    }

    @Override
    public boolean isLicenseBanned(String license) {
        Objects.requireNonNull(license);

        Optional<LicenseDTO> dto = getLicense(license);
        return dto.isPresent() && dto.get().isBanned();
    }

    /**
     * Sets the banned state of a license key
     * @param license The license to modify
     * @param banned Whether the license should be banned
     * @throws LicenseServiceException If something goes wrong while updating the license
     */
    private void setLicenseBanned(String license, boolean banned) throws LicenseServiceException{
        Objects.requireNonNull(license);

        Optional<LicenseModel> licenseModel = licenseRepo.findLicenseModelByLicense(license);
        if (licenseModel.isEmpty())
            throw new LicenseServiceException("License does not exist", -1);
        licenseModel.get().setBanned(banned);
        licenseRepo.saveAndFlush(licenseModel.get());
    }

    /**
     * Maps a model to a DTO
     * @param model The model to map
     * @return The DTO equivalent
     */
    private LicenseDTO mapToDto(LicenseModel model){
        Objects.requireNonNull(model);
        return new LicenseDTO(model.getId(), model.getHolder(),
                model.getProductId(), model.getLicense(), model.isBanned());
    }

}
