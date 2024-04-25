package me.bannock.capstone.backend.accounts.service;

public class UserServiceException extends Exception {

    public UserServiceException(String errorMessage, long uid){
        super("%s, uid=%s".formatted(errorMessage, uid));
        this.errorMessage = errorMessage;
    }

    private final String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

}
