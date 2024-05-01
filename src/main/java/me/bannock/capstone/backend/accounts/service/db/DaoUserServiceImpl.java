package me.bannock.capstone.backend.accounts.service.db;

import me.bannock.capstone.backend.accounts.service.AccountDTO;
import me.bannock.capstone.backend.accounts.service.UserService;
import me.bannock.capstone.backend.accounts.service.UserServiceException;
import me.bannock.capstone.backend.keygen.KeyGenService;
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
import java.util.stream.Collectors;

@Service
public class DaoUserServiceImpl implements UserService {

    @Autowired
    public DaoUserServiceImpl(PasswordEncoder passwordEncoder, UserRepo userRepo,
                              KeyGenService keyGenService){
        this.passwordEncoder = passwordEncoder;
        this.userRepo = userRepo;
        this.keyGenService = keyGenService;
    }

    private final Logger logger = LogManager.getLogger();
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final KeyGenService keyGenService;

    @Value("${backend.userService.maxGenApiKeyTries}")
    private int MAX_GEN_API_KEY_TRIES;

    @Value("${backend.userService.autoVerifyEmails}")
    private boolean autoVerifyEmails;

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

        checkAccountCanLogin(user);

        return user.get().getId();
    }

    @Override
    public long loginWithApiKey(String token) throws UserServiceException {
        Objects.requireNonNull(token);

        Optional<AccountModel> user = userRepo.findAccountModelByApiKey(token);
        if (user.isEmpty())
            throw new UserServiceException("Api key does not exist", -1);

        // Double check that the user is allowed to use the api for logins
        if (user.get().getPrivileges().stream().noneMatch(priv -> priv.equals(Privilege.PRIV_USE_API.getPrivilege())))
            throw new UserServiceException("Privilege \"PRIV_USE_API\" is needed to authenticate through api", user.get().getId());

        checkAccountCanLogin(user);

        return user.get().getId();
    }

    private void checkAccountCanLogin(Optional<AccountModel> user) throws UserServiceException{
        Objects.requireNonNull(user);
        if (user.isEmpty())
            throw new IllegalArgumentException();
        // If the user doesn't have the PRIV_LOGIN privilege, we must block their login
        if (user.get().getPrivileges().stream().noneMatch(priv -> priv.equals(Privilege.PRIV_LOGIN.getPrivilege())))
            throw new UserServiceException("You do not have permission to login, lol", user.get().getId());

        // Disabled accounts are unable to login as well
        if (user.get().isDisabled())
            throw new UserServiceException("Your account is disabled", user.get().getId());
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

        if (autoVerifyEmails)
            newUser.setEmailVerified(true);

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
            throw new UserServiceException("No account exists with user id %s".formatted(uid), uid);

        // We must generate a unique api key before saving
        String newApiKey;
        int attemptedTries = 0;
        do{
            newApiKey = this.keyGenService.generateNewKey();
            if (attemptedTries++ >= MAX_GEN_API_KEY_TRIES)
                throw new UserServiceException("Couldn't generate a unique api key", uid);
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
        return userOptional.map(this::createDto);
    }

    @Override
    public Optional<AccountDTO> getAccountWithUsername(String username) {
        Objects.requireNonNull(username);

        Optional<AccountModel> userOptional = userRepo.findAccountModelByUsername(username);
        return userOptional.map(this::createDto);
    }

    @Override
    public Optional<AccountDTO> getAccountWithUid(long uid) {
        Optional<AccountModel> user = userRepo.findAccountModelById(uid);
        return user.map(this::createDto);
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

    /**
     * Creates a DTO object with a model object
     * @param user The account model object
     * @return An account DTO
     */
    private AccountDTO createDto(AccountModel user){
        Objects.requireNonNull(user);
        return new AccountDTO(
                user.getId(),
                user.getPrivileges().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()),
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                user.getApiKey(),
                !user.isEmailVerified(),
                false, false,
                user.isDisabled()
        );
    }

}
