package me.bannock.capstone.backend.accounts.service.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<AccountModel, Long> {

    Optional<AccountModel> findAccountModelById(long id);

    Optional<AccountModel> findAccountModelByUsername(String username);

    Optional<AccountModel> findAccountModelByEmail(String email);

    Optional<AccountModel> findAccountModelByEmailAndPassword(String email, String password);

    Optional<AccountModel> findAccountModelByApiKey(String apiKey);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByApiKey(String apiKey);

}
