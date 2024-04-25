package me.bannock.capstone.backend.accounts.service.db;

import me.bannock.capstone.backend.accounts.service.AccountDTO;
import me.bannock.capstone.backend.accounts.service.UserService;
import me.bannock.capstone.backend.accounts.service.UserServiceException;
import me.bannock.capstone.backend.security.Privilege;
import me.bannock.capstone.backend.security.Role;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DaoUserServiceImpl implements UserService {

    @Autowired
    public DaoUserServiceImpl(PasswordEncoder passwordEncoder, UserRepo userRepo){
        this.passwordEncoder = passwordEncoder;
        this.userRepo = userRepo;
    }

    private final Logger logger = LogManager.getLogger();
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;

    @Value("${backend.userService.maxGenApiKeyTries}")
    private int MAX_GEN_API_KEY_TRIES;

    @Override
    public long login(String email, String password) throws UserServiceException {
        Objects.requireNonNull(email);
        Objects.requireNonNull(password);

        // We do an email search instead of an email and password search so our service provides
        // extra details about failed logins
        Optional<AccountModel> user = userRepo.findAccountModelByEmail(email);
        if (user.isEmpty())
            throw new UserServiceException("Could not find user account what matched email", -1);
        if (!passwordEncoder.matches(password, user.get().getPassword()))
            throw new UserServiceException("Password is incorrect", user.get().getId());
        return user.get().getId();
    }

    @Override
    public void register(String email, String username, String password) {
        Objects.requireNonNull(email);
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

        if (userRepo.existsByEmail(email))
            throw new IllegalArgumentException("Email already registered");
        if (userRepo.existsByUsername(username))
            throw new IllegalArgumentException("Username is already in use");
        AccountModel newUser = new AccountModel(email, username, passwordEncoder.encode(password));

        // New users should get all privileges from the user role
        List<String> privs = Role.ROLE_USER.getPrivileges().stream()
                .map(Privilege::getPrivilege).toList();
        newUser.setPrivileges(privs);

        logger.info("A new user registered an account: {}", newUser);
        userRepo.saveAndFlush(newUser);
    }

    @Override
    public Optional<String> getApiKey(long uid) {
        Optional<AccountModel> user = userRepo.findAccountModelById(uid);
        return user.map(AccountModel::getApiKey);
    }

    @Override
    public String genApiKey(long uid) throws UserServiceException {
        Optional<AccountModel> user = userRepo.findAccountModelById(uid);
        if (user.isEmpty())
            throw new IllegalArgumentException("No account exists with user id %s".formatted(uid));

        // We must generate a unique api key before saving
        String newApiKey;
        int attemptedTries = 0;
        do{
            newApiKey = UUID.randomUUID().toString();
            if (attemptedTries++ >= MAX_GEN_API_KEY_TRIES)
                throw new RuntimeException("Couldn't generate a unique api key");
        }while (userRepo.existsByApiKey(newApiKey));

        // Only now do we save the changes
        user.get().setApiKey(newApiKey);
        userRepo.saveAndFlush(user.get());

        logger.info("Genned a new API key for uid:{}, gennedAttempts:{}, maxGenAttempts={}, newApiKey={}",
                uid, attemptedTries, MAX_GEN_API_KEY_TRIES, newApiKey);

        return newApiKey;
    }

    @Override
    public Optional<Long> getWithApiKey(String apiKey) {
        Objects.requireNonNull(apiKey);

        Optional<AccountModel> user = userRepo.findAccountModelByApiKey(apiKey);
        return user.map(AccountModel::getId);
    }

    @Override
    public Optional<AccountDTO> getAccountWithEmail(String email) {
        Objects.requireNonNull(email);

        Optional<AccountModel> userOptional = userRepo.findAccountModelByEmail(email);
        if (userOptional.isEmpty())
            return Optional.empty();
        AccountModel user = userOptional.get();
        return Optional.of(new AccountDTO(
                user.getId(),
                user.getPrivileges().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()),
                user.getUsername(),
                user.getPassword(),
                !user.isEmailVerified(),
                false, false,
                user.isDisabled()
        ));
    }

    @Override
    public Optional<String> getAccountEmail(long uid) {
        Optional<AccountModel> user = userRepo.findAccountModelById(uid);
        return user.map(AccountModel::getEmail);
    }

    @Override
    public void grantPrivilege(long uid, String privilegeName) throws UserServiceException {
        Objects.requireNonNull(privilegeName);

        // We get the user first so we're able to pull and modify their privileges
        Optional<AccountModel> user = userRepo.findAccountModelById(uid);
        if (user.isEmpty())
            throw new UserServiceException("User does not exist", uid);

        ArrayList<String> privileges = user.get().getPrivileges();
        if (privileges == null)
            privileges = new ArrayList<>();
        privileges.add(privilegeName);
        user.get().setPrivileges(privileges);
        userRepo.saveAndFlush(user.get());
        logger.info("Granted privilege to user, privilege={}, user={}", privilegeName, user.get());
    }

    @Override
    public void revokePrivilege(long uid, String privilegeName) throws UserServiceException {
        Objects.requireNonNull(privilegeName);

        // We get the user first so we're able to pull and modify their privileges
        Optional<AccountModel> user = userRepo.findAccountModelById(uid);
        if (user.isEmpty())
            throw new UserServiceException("User does not exist", uid);

        ArrayList<String> privileges = user.get().getPrivileges();
        if (privileges == null)
            return;
        privileges.remove(privilegeName);
        user.get().setPrivileges(privileges);
        userRepo.saveAndFlush(user.get());
        logger.info("Revoked privilege from user, privilege={}, user={}", privilegeName, user.get());
    }

    @Override
    public boolean hasPrivilege(long uid, String privilegeName) throws UserServiceException {
        Objects.requireNonNull(privilegeName);

        // We get the user first so we're able to pull and modify their privileges
        Optional<AccountModel> user = userRepo.findAccountModelById(uid);
        if (user.isEmpty())
            throw new UserServiceException("User does not exist", uid);

        ArrayList<String> privileges = user.get().getPrivileges();
        if (privileges == null)
            return false;
        return privileges.contains(privilegeName);
    }

}
