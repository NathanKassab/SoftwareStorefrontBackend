package me.bannock.capstone.backend.licensing.service.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LicenseRepo extends JpaRepository<LicenseModel, Long> {

    Optional<LicenseModel> findLicenseModelByLicense(String license);

    Optional<LicenseModel> findLicenseModelByHolderAndProductId(long holder, long productId);

}
