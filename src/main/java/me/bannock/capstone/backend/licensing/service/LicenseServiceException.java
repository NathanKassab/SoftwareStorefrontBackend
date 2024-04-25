package me.bannock.capstone.backend.licensing.service;

import jakarta.annotation.Nullable;

public class LicenseServiceException extends Exception {

    /**
     * @param errorMessage The error message that's shown to the user
     * @param licenseDTO The license object
     * @param licenseId The license id
     */
    public LicenseServiceException(String errorMessage,
                                   @Nullable LicenseDTO licenseDTO,
                                   long licenseId){
        super("%s, licenseDto=%s, licenseId=%s".formatted(errorMessage, licenseDTO, licenseId));
        this.errorMessage = errorMessage;
    }

    /**
     * @param errorMessage The error message that's shown to the user
     * @param licenseId The license id
     */
    public LicenseServiceException(String errorMessage,
                                   long licenseId){
        this(errorMessage, null, licenseId);
    }

    private final String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

}
