package me.bannock.capstone.backend.keygen;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UUIDKeyGenServiceImpl implements KeyGenService {

    @Override
    public String generateNewKey() {
        return UUID.randomUUID().toString();
    }

}
