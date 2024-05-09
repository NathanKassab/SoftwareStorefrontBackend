package me.bannock.capstone.backend.loader.prot.service;

import jakarta.annotation.Nullable;

public class LoaderProtServiceException extends RuntimeException {

    public LoaderProtServiceException(String errorMessage, @Nullable String jobId){
        super("%s, jobId=%s".formatted(errorMessage, jobId));
        this.errorMessage = errorMessage;
    }
    private final String errorMessage;
    public String getErrorMessage() {
        return errorMessage;
    }

}
