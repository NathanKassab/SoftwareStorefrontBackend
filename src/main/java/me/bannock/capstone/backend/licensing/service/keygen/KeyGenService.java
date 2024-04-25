package me.bannock.capstone.backend.licensing.service.keygen;

public interface KeyGenService {

    /**
     * Generates a new key. May not be unique
     * @return A new key
     */
    String generateNewKey();

}
